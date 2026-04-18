package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object CourtsTable : UUIDTable("courts") {
    val turfId = uuid("turf_id").references(TurfsTable.id).index()
    val name = varchar("name", 100)
    /** Sport code, e.g. "FOOTBALL" */
    val sport = varchar("sport", 50)
    val description = text("description").nullable()
    /** Null = inherit from parent turf */
    val openingTime = varchar("opening_time", 5).nullable()
    val closingTime = varchar("closing_time", 5).nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
