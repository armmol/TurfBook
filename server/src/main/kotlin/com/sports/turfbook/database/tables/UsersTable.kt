package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UsersTable : UUIDTable("users") {
    val phone = varchar("phone", 15).uniqueIndex()
    val name = varchar("name", 100).nullable()
    val email = varchar("email", 255).nullable()
    val profilePhotoUrl = text("profile_photo_url").nullable()
    val city = varchar("city", 100).nullable()
    val totalBookings = integer("total_bookings").default(0)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
