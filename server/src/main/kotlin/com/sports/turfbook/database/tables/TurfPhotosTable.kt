package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable

object TurfPhotosTable : UUIDTable("turf_photos") {
    val turfId = uuid("turf_id").references(TurfsTable.id).index()
    val url = text("url")
    val displayOrder = integer("display_order").default(0)
}
