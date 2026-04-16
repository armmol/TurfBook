package com.sports.turfbook.database.tables

import org.jetbrains.exposed.sql.Table

object TurfSportsTable : Table("turf_sports") {
    val turfId = uuid("turf_id").references(TurfsTable.id)
    /** Sport code, e.g. "FOOTBALL". Stored as varchar for extensibility. */
    val sport = varchar("sport", 50)
    override val primaryKey = PrimaryKey(turfId, sport)
}
