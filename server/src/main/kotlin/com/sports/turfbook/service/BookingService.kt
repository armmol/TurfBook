package com.sports.turfbook.service

import com.sports.turfbook.api.dto.booking.BookingDto
import com.sports.turfbook.api.dto.booking.CreateBookingDto
import com.sports.turfbook.connector.ConnectorFactory
import com.sports.turfbook.connector.ExternalBookingPayload
import com.sports.turfbook.connector.ExternalCancelPayload
import com.sports.turfbook.connector.ExternalConfirmPayload
import com.sports.turfbook.database.tables.BookingsTable
import com.sports.turfbook.database.tables.TurfExternalSystemTable
import com.sports.turfbook.database.tables.TurfSlotPricingTable
import com.sports.turfbook.database.tables.TurfsTable
import com.sports.turfbook.database.tables.UsersTable
import com.sports.turfbook.domain.KnownSports
import com.sports.turfbook.domain.enums.BookingStatus
import com.sports.turfbook.plugins.ConflictException
import com.sports.turfbook.plugins.NotFoundException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class BookingService {

    /**
     * Validates the slot, acquires a Redis lock, and creates a PENDING_PAYMENT booking.
     * If the turf has an external booking system configured, it is notified after creation.
     * Throws [ConflictException] if the slot is already taken.
     */
    fun createBooking(userId: String, dto: CreateBookingDto): BookingDto = transaction {
        val turfUuid = UUID.fromString(dto.turfId)
        val sportCode = dto.sport.uppercase()

        // Validate sport is known
        if (!KnownSports.isKnown(sportCode)) {
            throw IllegalArgumentException("Unknown sport: $sportCode")
        }

        // Validate turf exists
        val turfRow = TurfsTable.selectAll()
            .where { (TurfsTable.id eq turfUuid) and (TurfsTable.isActive eq true) }
            .singleOrNull() ?: throw NotFoundException("Turf not found")

        // Validate pricing exists for this sport + duration
        val pricingRow = TurfSlotPricingTable.selectAll()
            .where {
                (TurfSlotPricingTable.turfId eq turfUuid) and
                (TurfSlotPricingTable.sport eq sportCode) and
                (TurfSlotPricingTable.durationMinutes eq dto.durationMinutes)
            }
            .singleOrNull() ?: throw IllegalArgumentException(
                "This turf does not offer $sportCode for ${dto.durationMinutes}-minute slots"
            )

        // Validate slot is within turf hours
        val openMins = turfRow[TurfsTable.openingTime].toMinutes()
        val closeMins = turfRow[TurfsTable.closingTime].toMinutes()
        val startMins = dto.startTime.toMinutes()
        if (startMins < openMins || startMins + dto.durationMinutes > closeMins) {
            throw IllegalArgumentException("Slot is outside turf operating hours")
        }

        val endTime = (startMins + dto.durationMinutes).toTimeString()
        val price = pricingRow[TurfSlotPricingTable.priceInPaise]

        // Check with external system if configured
        val externalRow = TurfExternalSystemTable.selectAll()
            .where { TurfExternalSystemTable.turfId eq turfUuid }
            .singleOrNull()
        val connector = ConnectorFactory.create(
            externalRow?.get(TurfExternalSystemTable.systemType),
            externalRow?.get(TurfExternalSystemTable.baseUrl),
            externalRow?.get(TurfExternalSystemTable.apiKey)
        )
        if (!connector.isSlotAvailable(dto.turfId, dto.date, dto.startTime, sportCode)) {
            throw ConflictException("Slot is not available in the turf's booking system")
        }

        // Try acquiring Redis lock before touching the DB
        val locked = RedisService.lockSlot(
            dto.turfId, dto.date, dto.startTime, dto.durationMinutes, sportCode, userId
        )
        if (!locked) throw ConflictException("This slot is currently being booked by someone else. Try again.")

        // Double-check DB for confirmed/pending bookings (guard against Redis eviction)
        val alreadyBooked = BookingsTable.selectAll()
            .where {
                (BookingsTable.turfId eq turfUuid) and
                (BookingsTable.date eq dto.date) and
                (BookingsTable.startTime eq dto.startTime) and
                (BookingsTable.durationMinutes eq dto.durationMinutes) and
                (BookingsTable.sport eq sportCode) and
                (BookingsTable.status inList listOf(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT))
            }
            .count() > 0

        if (alreadyBooked) {
            RedisService.releaseSlot(dto.turfId, dto.date, dto.startTime, dto.durationMinutes, sportCode)
            throw ConflictException("Slot is already booked")
        }

        val now: Instant = Clock.System.now()
        val bookingId = UUID.randomUUID()

        BookingsTable.insert {
            it[id] = bookingId
            it[BookingsTable.userId] = UUID.fromString(userId)
            it[BookingsTable.turfId] = turfUuid
            it[BookingsTable.sport] = sportCode
            it[BookingsTable.date] = dto.date
            it[BookingsTable.startTime] = dto.startTime
            it[BookingsTable.endTime] = endTime
            it[BookingsTable.durationMinutes] = dto.durationMinutes
            it[BookingsTable.amountInPaise] = price
            it[BookingsTable.status] = BookingStatus.PENDING_PAYMENT
            it[BookingsTable.createdAt] = now
            it[BookingsTable.updatedAt] = now
        }

        val bookingDto = BookingDto(
            id = bookingId.toString(),
            turfId = dto.turfId,
            turfName = turfRow[TurfsTable.name],
            turfAddress = turfRow[TurfsTable.address],
            sport = sportCode,
            date = dto.date,
            startTime = dto.startTime,
            endTime = endTime,
            amountInPaise = price,
            status = BookingStatus.PENDING_PAYMENT,
            createdAt = now.toString()
        )

        // Notify external system (fire-and-forget, failures are logged not thrown)
        connector.onBookingCreated(
            ExternalBookingPayload(
                bookingId = bookingId.toString(),
                turfId = dto.turfId,
                userId = userId,
                sport = sportCode,
                date = dto.date,
                startTime = dto.startTime,
                endTime = endTime,
                durationMinutes = dto.durationMinutes,
                amountInPaise = price,
                status = "PENDING_PAYMENT"
            )
        )

        bookingDto
    }

    fun getUserBookings(userId: String): List<BookingDto> = transaction {
        BookingsTable
            .join(TurfsTable, JoinType.INNER, BookingsTable.turfId, TurfsTable.id)
            .selectAll()
            .where { BookingsTable.userId eq UUID.fromString(userId) }
            .orderBy(BookingsTable.createdAt, SortOrder.DESC)
            .map { it.toBookingDto() }
    }

    fun getBookingById(bookingId: String, userId: String): BookingDto = transaction {
        BookingsTable
            .join(TurfsTable, JoinType.INNER, BookingsTable.turfId, TurfsTable.id)
            .selectAll()
            .where {
                (BookingsTable.id eq UUID.fromString(bookingId)) and
                (BookingsTable.userId eq UUID.fromString(userId))
            }
            .singleOrNull()
            ?.toBookingDto()
            ?: throw NotFoundException("Booking not found")
    }

    fun cancelBooking(bookingId: String, userId: String, reason: String?): BookingDto = transaction {
        val uuid = UUID.fromString(bookingId)
        val row = BookingsTable.selectAll()
            .where {
                (BookingsTable.id eq uuid) and
                (BookingsTable.userId eq UUID.fromString(userId))
            }
            .singleOrNull() ?: throw NotFoundException("Booking not found")

        val currentStatus = row[BookingsTable.status]
        if (currentStatus !in listOf(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT)) {
            throw IllegalArgumentException("Cannot cancel a ${currentStatus.name.lowercase()} booking")
        }

        val now: Instant = Clock.System.now()
        BookingsTable.update({ BookingsTable.id eq uuid }) {
            it[status] = BookingStatus.CANCELLED
            it[cancellationReason] = reason
            it[updatedAt] = now
        }

        val turfId = row[BookingsTable.turfId]
        val sport = row[BookingsTable.sport]

        // Release Redis lock if still held
        RedisService.releaseSlot(
            turfId.toString(),
            row[BookingsTable.date],
            row[BookingsTable.startTime],
            row[BookingsTable.durationMinutes],
            sport
        )

        // Notify external system
        val externalRow = TurfExternalSystemTable.selectAll()
            .where { TurfExternalSystemTable.turfId eq turfId }
            .singleOrNull()
        ConnectorFactory.create(
            externalRow?.get(TurfExternalSystemTable.systemType),
            externalRow?.get(TurfExternalSystemTable.baseUrl),
            externalRow?.get(TurfExternalSystemTable.apiKey)
        ).onBookingCancelled(ExternalCancelPayload(bookingId = bookingId, reason = reason))

        getBookingById(bookingId, userId)
    }

    /** Called by PaymentService after successful payment verification */
    fun confirmBooking(bookingId: String, paymentId: String): BookingDto = transaction {
        val uuid = UUID.fromString(bookingId)
        val row = BookingsTable.selectAll()
            .where { BookingsTable.id eq uuid }
            .singleOrNull() ?: throw NotFoundException("Booking not found")

        val qr = QrService.generate(
            bookingId,
            row[BookingsTable.turfId].toString(),
            row[BookingsTable.date],
            row[BookingsTable.startTime]
        )

        val now: Instant = Clock.System.now()
        BookingsTable.update({ BookingsTable.id eq uuid }) {
            it[status] = BookingStatus.CONFIRMED
            it[qrCode] = qr
            it[updatedAt] = now
        }

        // Increment user's total bookings counter
        UsersTable.update({ UsersTable.id eq row[BookingsTable.userId] }) {
            with(SqlExpressionBuilder) { it.update(totalBookings, totalBookings + 1) }
        }

        val turfId = row[BookingsTable.turfId]
        val turfRow = TurfsTable.selectAll()
            .where { TurfsTable.id eq turfId }
            .single()

        // Notify external system
        val externalRow = TurfExternalSystemTable.selectAll()
            .where { TurfExternalSystemTable.turfId eq turfId }
            .singleOrNull()
        ConnectorFactory.create(
            externalRow?.get(TurfExternalSystemTable.systemType),
            externalRow?.get(TurfExternalSystemTable.baseUrl),
            externalRow?.get(TurfExternalSystemTable.apiKey)
        ).onBookingConfirmed(ExternalConfirmPayload(bookingId = bookingId, gatewayPaymentId = paymentId))

        BookingDto(
            id = bookingId,
            turfId = turfId.toString(),
            turfName = turfRow[TurfsTable.name],
            turfAddress = turfRow[TurfsTable.address],
            sport = row[BookingsTable.sport],
            date = row[BookingsTable.date],
            startTime = row[BookingsTable.startTime],
            endTime = row[BookingsTable.endTime],
            amountInPaise = row[BookingsTable.amountInPaise],
            status = BookingStatus.CONFIRMED,
            qrCode = qr,
            createdAt = row[BookingsTable.createdAt].toString()
        )
    }

    private fun ResultRow.toBookingDto(): BookingDto = BookingDto(
        id = this[BookingsTable.id].toString(),
        turfId = this[BookingsTable.turfId].toString(),
        turfName = this[TurfsTable.name],
        turfAddress = this[TurfsTable.address],
        sport = this[BookingsTable.sport],
        date = this[BookingsTable.date],
        startTime = this[BookingsTable.startTime],
        endTime = this[BookingsTable.endTime],
        amountInPaise = this[BookingsTable.amountInPaise],
        status = this[BookingsTable.status],
        qrCode = this[BookingsTable.qrCode],
        cancellationReason = this[BookingsTable.cancellationReason],
        createdAt = this[BookingsTable.createdAt].toString()
    )
}

private fun String.toMinutes(): Int {
    val (h, m) = split(":").map { it.toInt() }
    return h * 60 + m
}

private fun Int.toTimeString(): String = "%02d:%02d".format(this / 60, this % 60)
