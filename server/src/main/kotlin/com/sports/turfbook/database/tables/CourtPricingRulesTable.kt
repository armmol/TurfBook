package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object CourtPricingRulesTable : UUIDTable("court_pricing_rules") {
    val courtId = uuid("court_id").references(CourtsTable.id).index()
    val durationMinutes = integer("duration_minutes")
    val priceInPaise = long("price_in_paise")
    /** 1=Mon … 7=Sun (ISO). Null = any day of week. */
    val dayOfWeek = integer("day_of_week").nullable()
    /** Slot startTime must be ≥ this value. "18:00". Null = no lower bound. */
    val startTime = varchar("start_time", 5).nullable()
    /** Slot startTime must be < this value. "22:00". Null = no upper bound. */
    val endTime = varchar("end_time", 5).nullable()
    /** "2024-12-25" — one-off date override. Null = recurring rule. */
    val specificDate = varchar("specific_date", 10).nullable()
    /** Higher priority wins when multiple rules match the same slot. */
    val priority = integer("priority").default(0)
    val isActive = bool("is_active").default(true)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
