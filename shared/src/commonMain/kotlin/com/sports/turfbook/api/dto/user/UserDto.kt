package com.sports.turfbook.api.dto.user

import com.sports.turfbook.domain.enums.SportType
import kotlinx.serialization.Serializable

/** Returned in API responses wherever user data is needed */
@Serializable
data class UserDto(
    val id: String,
    val phone: String,
    val name: String,
    val email: String? = null,
    val profilePhotoUrl: String? = null,
    val city: String,
    val preferredSports: List<SportType> = emptyList(),
    val totalBookings: Int = 0
)
