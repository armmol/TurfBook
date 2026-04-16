package com.sports.turfbook.api.dto.user

import kotlinx.serialization.Serializable

/** PATCH /users/me — all fields optional, only provided fields are updated */
@Serializable
data class UpdateProfileDto(
    val name: String? = null,
    val email: String? = null,
    val city: String? = null,
    /** Sport codes, e.g. ["FOOTBALL", "CRICKET"] */
    val preferredSports: List<String>? = null,
    /** Signed upload URL returned after uploading photo separately */
    val profilePhotoUrl: String? = null
)
