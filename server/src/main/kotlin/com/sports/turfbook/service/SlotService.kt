package com.sports.turfbook.service

import com.sports.turfbook.api.dto.slot.DayAvailabilityDto
import com.sports.turfbook.api.dto.slot.SlotDto
import com.sports.turfbook.database.tables.BookingsTable
import com.sports.turfbook.database.tables.CourtPricingRulesTable
import com.sports.turfbook.database.tables.CourtSlotPricingTable
import com.sports.turfbook.database.tables.CourtsTable
import com.sports.turfbook.database.tables.TurfsTable
import com.sports.turfbook.domain.enums.BookingStatus
import com.sports.turfbook.domain.enums.SlotStatus
import com.sports.turfbook.plugins.NotFoundException
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SlotService {

    /**
     * Returns all time slots for [courtId] on [date].
     * Opening/closing times come from the court if overridden, otherwise from the parent turf.
     */
    fun getAvailability(turfId: String, courtId: String, date: String): DayAvailabilityDto {
        val courtUuid = UUID.fromString(courtId)
        val turfUuid = UUID.fromString(turfId)

        return transaction {
            val turfRow = TurfsTable.selectAll()
                .where { TurfsTable.id eq turfUuid }
                .singleOrNull() ?: throw NotFoundException("Turf $turfId not found")

            val courtRow = CourtsTable.selectAll()
                .where { (CourtsTable.id eq courtUuid) and (CourtsTable.turfId eq turfUuid) }
                .singleOrNull() ?: throw NotFoundException("Court $courtId not found on turf $turfId")

            // Court-level override takes priority; fall back to turf defaults
            val openingTime = courtRow[CourtsTable.openingTime] ?: turfRow[TurfsTable.openingTime]
            val closingTime = courtRow[CourtsTable.closingTime] ?: turfRow[TurfsTable.closingTime]
            val sport = courtRow[CourtsTable.sport]

            val basePricingRows = CourtSlotPricingTable.selectAll()
                .where { CourtSlotPricingTable.courtId eq courtUuid }

            // Load all active rules for this court once — avoids N queries inside the loop
            val activeRules = CourtPricingRulesTable.selectAll()
                .where {
                    (CourtPricingRulesTable.courtId eq courtUuid) and
                    (CourtPricingRulesTable.isActive eq true)
                }

            val dayOfWeek = LocalDate.parse(date).dayOfWeek.value

            // Occupied: "startTime:durationMinutes"
            val occupiedKeys = BookingsTable.selectAll()
                .where {
                    (BookingsTable.courtId eq courtUuid) and
                    (BookingsTable.date eq date) and
                    (BookingsTable.status inList listOf(
                        BookingStatus.CONFIRMED,
                        BookingStatus.PENDING_PAYMENT
                    ))
                }
                .map { "${it[BookingsTable.startTime]}:${it[BookingsTable.durationMinutes]}" }
                .toSet()

            val slots = mutableListOf<SlotDto>()

            for (pricingRow in basePricingRows) {
                val duration = pricingRow[CourtSlotPricingTable.durationMinutes]
                val basePrice = pricingRow[CourtSlotPricingTable.priceInPaise]

                generateTimeSlots(openingTime, closingTime, duration).forEach { (start, end) ->
                    val isBooked = "${start}:${duration}" in occupiedKeys
                    val isLocked = RedisService.isSlotLocked(courtId, date, start, duration)

                    val status = when {
                        isBooked -> SlotStatus.BOOKED
                        isLocked -> SlotStatus.LOCKED
                        else -> SlotStatus.AVAILABLE
                    }

                    // Resolve price: find highest-priority rule that matches this slot
                    val resolvedPrice = activeRules
                        .filter { rule ->
                            rule[CourtPricingRulesTable.durationMinutes] == duration &&
                            (rule[CourtPricingRulesTable.specificDate]?.let { it == date } ?: true) &&
                            (rule[CourtPricingRulesTable.dayOfWeek]?.let { it == dayOfWeek } ?: true) &&
                            (rule[CourtPricingRulesTable.startTime]?.let { start >= it } ?: true) &&
                            (rule[CourtPricingRulesTable.endTime]?.let { start < it } ?: true)
                        }
                        .maxByOrNull { it[CourtPricingRulesTable.priority] }
                        ?.get(CourtPricingRulesTable.priceInPaise)
                        ?: basePrice

                    slots += SlotDto(
                        id = "$courtId:$date:$start:$duration",
                        startTime = start,
                        endTime = end,
                        durationMinutes = duration,
                        priceInPaise = resolvedPrice,
                        status = status
                    )
                }
            }

            DayAvailabilityDto(
                turfId = turfId,
                courtId = courtId,
                courtName = courtRow[CourtsTable.name],
                sport = sport,
                date = date,
                slots = slots.sortedBy { it.startTime }
            )
        }
    }

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

    private fun Int.toTimeString(): String = "%02d:%02d".format(this / 60, this % 60)
}
