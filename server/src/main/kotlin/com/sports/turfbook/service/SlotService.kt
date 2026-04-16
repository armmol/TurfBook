package com.sports.turfbook.service

import com.sports.turfbook.api.dto.slot.DayAvailabilityDto
import com.sports.turfbook.api.dto.slot.SlotDto
import com.sports.turfbook.database.tables.BookingsTable
import com.sports.turfbook.database.tables.TurfSlotPricingTable
import com.sports.turfbook.database.tables.TurfsTable
import com.sports.turfbook.domain.enums.BookingStatus
import com.sports.turfbook.domain.enums.SlotStatus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SlotService {

    /**
     * Returns all time slots for [turfId] on [date] for the given [sport] code.
     * Slot status is computed from confirmed/pending bookings and Redis locks.
     */
    fun getAvailability(turfId: String, date: String, sport: String): DayAvailabilityDto {
        val uuid = UUID.fromString(turfId)
        val sportCode = sport.uppercase()

        return transaction {
            val turfRow = TurfsTable.selectAll()
                .where { TurfsTable.id eq uuid }
                .singleOrNull() ?: error("Turf $turfId not found")

            val openingTime = turfRow[TurfsTable.openingTime]
            val closingTime = turfRow[TurfsTable.closingTime]

            // All pricing configs for this sport at this turf
            val pricingRows = TurfSlotPricingTable.selectAll()
                .where {
                    (TurfSlotPricingTable.turfId eq uuid) and
                    (TurfSlotPricingTable.sport eq sportCode)
                }

            // Booked/pending slot keys: "startTime:durationMinutes"
            val occupiedKeys = BookingsTable.selectAll()
                .where {
                    (BookingsTable.turfId eq uuid) and
                    (BookingsTable.date eq date) and
                    (BookingsTable.sport eq sportCode) and
                    (BookingsTable.status inList listOf(
                        BookingStatus.CONFIRMED,
                        BookingStatus.PENDING_PAYMENT
                    ))
                }
                .map { "${it[BookingsTable.startTime]}:${it[BookingsTable.durationMinutes]}" }
                .toSet()

            val slots = mutableListOf<SlotDto>()

            for (pricingRow in pricingRows) {
                val duration = pricingRow[TurfSlotPricingTable.durationMinutes]
                val price = pricingRow[TurfSlotPricingTable.priceInPaise]

                generateTimeSlots(openingTime, closingTime, duration).forEach { (start, end) ->
                    val isBooked = "${start}:${duration}" in occupiedKeys
                    val isLocked = RedisService.isSlotLocked(turfId, date, start, duration, sportCode)

                    val status = when {
                        isBooked -> SlotStatus.BOOKED
                        isLocked -> SlotStatus.LOCKED
                        else -> SlotStatus.AVAILABLE
                    }

                    slots += SlotDto(
                        id = "$turfId:$date:$start:$duration:$sportCode",
                        sport = sportCode,
                        startTime = start,
                        endTime = end,
                        durationMinutes = duration,
                        priceInPaise = price,
                        status = status
                    )
                }
            }

            DayAvailabilityDto(turfId = turfId, date = date, slots = slots.sortedBy { it.startTime })
        }
    }

    /**
     * Generates (startTime, endTime) pairs for a day given opening/closing hours.
     * E.g. "06:00", "23:00", 60 → [("06:00","07:00"), ("07:00","08:00"), ...]
     */
    private fun generateTimeSlots(opening: String, closing: String, durationMinutes: Int): List<Pair<String, String>> {
        val openMins = opening.toMinutes()
        val closeMins = closing.toMinutes()
        val slots = mutableListOf<Pair<String, String>>()
        var current = openMins
        while (current + durationMinutes <= closeMins) {
            slots += current.toTimeString() to (current + durationMinutes).toTimeString()
            current += durationMinutes
        }
        return slots
    }

    private fun String.toMinutes(): Int {
        val (h, m) = split(":").map { it.toInt() }
        return h * 60 + m
    }

    private fun Int.toTimeString(): String {
        val h = this / 60
        val m = this % 60
        return "%02d:%02d".format(h, m)
    }
}
