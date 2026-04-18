package com.sports.turfbook.api.dto.turf

import com.sports.turfbook.api.dto.court.CreateCourtDto
import kotlinx.serialization.Serializable

/** POST /turfs */
@Serializable
data class CreateTurfDto(
    val name: String,
    val description: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    /** "06:00" 24h — default for all courts that don't override */
    val openingTime: String,
    /** "23:00" 24h */
    val closingTime: String,
    val amenities: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList(),
    /**
     * Courts to create alongside the turf. Each court specifies its own sport,
     * name, and pricing. A turf must have at least one court.
     * Example: 2 football fields + 3 badminton courts in a single CreateTurfDto.
     */
    val courts: List<CreateCourtDto>,
    /** Optional: URL of the turf's existing local booking system REST API */
    val externalSystemUrl: String? = null,
    /** "REST_API" or "WEBHOOK" */
    val externalSystemType: String? = null,
    val externalSystemApiKey: String? = null
)
