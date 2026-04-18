package com.sports.turfbook.api.dto.court

import kotlinx.serialization.Serializable

/**
 * A single bookable court within a turf (e.g. "Badminton Court 2",
 * "Football Field A"). Each court has its own sport, schedule override,
 * and pricing — and can be booked independently of sibling courts.
 */
@Serializable
data class CourtDto(
    val id: String,
    val turfId: String,
    /** Display name, e.g. "Court A" or "Badminton Court 1" */
    val name: String,
    /** Sport code, e.g. "FOOTBALL". Use KnownSports.fromCode() on client. */
    val sport: String,
    val description: String? = null,
    /**
     * Null means this court uses the parent turf's opening/closing times.
     * Set to override for courts with different hours (e.g. indoor courts open later).
     */
    val openingTime: String? = null,
    val closingTime: String? = null,
    /** Map of durationMinutes -> priceInPaise, e.g. {30: 50000, 60: 90000} */
    val pricingByDuration: Map<Int, Long>,
    val isActive: Boolean
)

/** POST /turfs/{id}/courts */
@Serializable
data class CreateCourtDto(
    val name: String,
    /** Sport code, e.g. "FOOTBALL" */
    val sport: String,
    val description: String? = null,
    /** Null = inherit turf opening time */
    val openingTime: String? = null,
    /** Null = inherit turf closing time */
    val closingTime: String? = null,
    val pricing: List<CourtPricingDto>
)

@Serializable
data class CourtPricingDto(
    /** 30 or 60 */
    val durationMinutes: Int,
    val priceInPaise: Long
)

/** PATCH /turfs/{id}/courts/{courtId} */
@Serializable
data class UpdateCourtDto(
    val name: String? = null,
    val description: String? = null,
    val openingTime: String? = null,
    val closingTime: String? = null,
    val pricing: List<CourtPricingDto>? = null,
    val isActive: Boolean? = null
)
