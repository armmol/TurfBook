package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ReviewsTable : UUIDTable("reviews") {
    val userId = uuid("user_id").references(UsersTable.id).index()
    val turfId = uuid("turf_id").references(TurfsTable.id).index()
    /** One review per booking */
    val bookingId = uuid("booking_id").references(BookingsTable.id).uniqueIndex()
    val rating = integer("rating")
    val comment = text("comment").nullable()
    val createdAt = timestamp("created_at")
}
