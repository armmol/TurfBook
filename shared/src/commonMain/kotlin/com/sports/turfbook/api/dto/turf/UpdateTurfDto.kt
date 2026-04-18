package com.sports.turfbook.api.dto.turf

import kotlinx.serialization.Serializable

/** PATCH /turfs/{id} — all fields optional. Courts are managed via /courts sub-resource. */
@Serializable
data class UpdateTurfDto(
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val openingTime: String? = null,
    val closingTime: String? = null,
    val amenities: List<String>? = null,
    val photoUrls: List<String>? = null,
    val isActive: Boolean? = null
)
