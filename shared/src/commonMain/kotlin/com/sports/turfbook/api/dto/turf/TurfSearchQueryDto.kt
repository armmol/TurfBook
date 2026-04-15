package com.sports.turfbook.api.dto.turf

import com.sports.turfbook.domain.enums.SportType
import kotlinx.serialization.Serializable

/** Query parameters for GET /turfs */
@Serializable
data class TurfSearchQueryDto(
    val city: String? = null,
    val sport: SportType? = null,
    /** "2024-01-15" */
    val date: String? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    /** Radius in km for location-based search */
    val radiusKm: Float? = null,
    val page: Int = 1,
    val pageSize: Int = 20
)
