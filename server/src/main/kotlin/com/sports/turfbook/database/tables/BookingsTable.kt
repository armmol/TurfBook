package com.sports.turfbook.database.tables

import com.sports.turfbook.domain.enums.BookingStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object BookingsTable : UUIDTable("bookings") {
    val userId = uuid("user_id").references(UsersTable.id).index()
    val turfId = uuid("turf_id").references(TurfsTable.id).index()
    val courtId = uuid("court_id").references(CourtsTable.id).index()
    /** Sport code — denormalised from the court for fast querying */
    val sport = varchar("sport", 50)
    /** "2024-01-15" */
    val date = varchar("date", 10).index()
    /** "10:00" */
    val startTime = varchar("start_time", 5)
    /** "11:00" */
    val endTime = varchar("end_time", 5)
    val durationMinutes = integer("duration_minutes")
    val amountInPaise = long("amount_in_paise")
    val status = enumerationByName<BookingStatus>("status", 30)
    val qrCode = text("qr_code").nullable()
    val cancellationReason = varchar("cancellation_reason", 500).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
