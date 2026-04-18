package com.sports.turfbook.database.tables

import org.jetbrains.exposed.sql.Table

object CourtSlotPricingTable : Table("court_slot_pricing") {
    val courtId = uuid("court_id").references(CourtsTable.id)
    /** 30 or 60 minutes */
    val durationMinutes = integer("duration_minutes")
    val priceInPaise = long("price_in_paise")

    override val primaryKey = PrimaryKey(courtId, durationMinutes)
}
