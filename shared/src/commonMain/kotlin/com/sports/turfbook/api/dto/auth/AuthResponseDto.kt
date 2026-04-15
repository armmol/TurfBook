package com.sports.turfbook.api.dto.auth

import com.sports.turfbook.api.dto.user.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto,
    /** True when the user is signing in for the first time (prompt profile setup) */
    val isNewUser: Boolean
)
