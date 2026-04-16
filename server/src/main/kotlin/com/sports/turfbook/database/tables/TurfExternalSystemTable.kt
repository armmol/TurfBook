package com.sports.turfbook.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Optional external booking system configuration per turf.
 *
 * Some turfs already run their own local booking software (e.g. a desktop app
 * used by the manager on-site). This table stores the integration config so
 * TurfBook can sync bookings bidirectionally.
 *
 * Supported [systemType] values:
 *  - "REST_API"  — TurfBook calls the turf's HTTP REST API
 *  - "WEBHOOK"   — TurfBook pushes events to a webhook URL
 *  - "NONE"      — No integration (default for most turfs)
 */
object TurfExternalSystemTable : Table("turf_external_systems") {
    val turfId = uuid("turf_id").references(TurfsTable.id).uniqueIndex()
    /** Integration type: "REST_API" or "WEBHOOK" */
    val systemType = varchar("system_type", 50)
    /** Base URL of the external system, e.g. "https://mybooking.local/api" */
    val baseUrl = varchar("base_url", 500)
    /** Optional API key / bearer token sent in Authorization header */
    val apiKey = varchar("api_key", 300).nullable()
    /** When false, the connector is skipped without removing the record */
    val isActive = bool("is_active").default(true)
}
