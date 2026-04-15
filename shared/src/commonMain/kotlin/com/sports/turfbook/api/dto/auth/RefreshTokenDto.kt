package com.sports.turfbook.api.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenDto(
    val refreshToken: String
)

@Serializable
data class TokenPairDto(
    val accessToken: String,
    val refreshToken: String
)
