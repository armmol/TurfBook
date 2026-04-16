package com.sports.turfbook.database.tables

import org.jetbrains.exposed.sql.Table

object TurfSlotPricingTable : Table("turf_slot_pricing") {
    val turfId = uuid("turf_id").references(TurfsTable.id)
    /** Sport code, e.g. "FOOTBALL". Stored as varchar for extensibility. */
    val sport = varchar("sport", 50)
    /** 30 or 60 */
    val durationMinutes = integer("duration_minutes")
    /** Price in paise */
    val priceInPaise = long("price_in_paise")
    override val primaryKey = PrimaryKey(turfId, sport, durationMinutes)
}
