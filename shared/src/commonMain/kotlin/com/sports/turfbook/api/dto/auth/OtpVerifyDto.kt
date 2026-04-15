package com.sports.turfbook.api.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class OtpVerifyDto(
    val phone: String,
    /** 6-digit OTP sent via SMS */
    val otp: String
)
