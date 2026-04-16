package com.sports.turfbook.api.dto.turf

import kotlinx.serialization.Serializable

@Serializable
data class SlotPricingDto(
    /** Sport code, e.g. "FOOTBALL". Must be a registered sport. */
    val sport: String,
    /** 30 or 60 */
    val durationMinutes: Int,
    /** Price in paise */
    val priceInPaise: Long
)

/** POST /turfs */
@Serializable
data class CreateTurfDto(
    val name: String,
    val description: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val openingTime: String,
    val closingTime: String,
    val amenities: List<String> = emptyList(),
    val photoUrls: List<String> = emptyList(),
    /** Prices per sport × duration combination */
    val slotPricing: List<SlotPricingDto>,
    /** Optional: URL of the turf's existing local booking system REST API */
    val externalSystemUrl: String? = null,
    /** "REST_API", "WEBHOOK", or null */
    val externalSystemType: String? = null,
    /** API key for the external system, if required */
    val externalSystemApiKey: String? = null
)
