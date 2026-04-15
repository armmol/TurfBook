package com.sports.turfbook.api.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequestDto(
    /** Indian mobile number with country code, e.g. "+919876543210" */
    val phone: String
)
