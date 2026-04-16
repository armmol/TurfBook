package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TurfsTable : UUIDTable("turfs") {
    val ownerId = uuid("owner_id").references(UsersTable.id)
    val name = varchar("name", 200)
    val description = text("description").default("")
    val address = text("address")
    val city = varchar("city", 100).index()
    val latitude = double("latitude")
    val longitude = double("longitude")
    /** "06:00" 24h format */
    val openingTime = varchar("opening_time", 5)
    /** "23:00" 24h format */
    val closingTime = varchar("closing_time", 5)
    val rating = float("rating").default(0f)
    val reviewCount = integer("review_count").default(0)
    val isVerified = bool("is_verified").default(false)
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
