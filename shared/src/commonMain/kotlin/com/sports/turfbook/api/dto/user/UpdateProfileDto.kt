package com.sports.turfbook.api.dto.user

import com.sports.turfbook.domain.enums.SportType
import kotlinx.serialization.Serializable

/** PATCH /users/me — all fields optional, only provided fields are updated */
@Serializable
data class UpdateProfileDto(
    val name: String? = null,
    val email: String? = null,
    val city: String? = null,
    val preferredSports: List<SportType>? = null,
    /** Signed upload URL returned after uploading photo separately */
    val profilePhotoUrl: String? = null
)
