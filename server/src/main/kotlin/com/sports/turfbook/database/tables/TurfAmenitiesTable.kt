package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object TurfAmenitiesTable : UUIDTable("turf_amenities") {
    val turfId = uuid("turf_id").references(TurfsTable.id).index()
    val amenity = varchar("amenity", 100)
}
