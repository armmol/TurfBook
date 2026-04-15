package com.sports.turfbook.api.dto.payment

import kotlinx.serialization.Serializable

/** POST /payments/verify — sent after the gateway SDK returns success on device */
@Serializable
data class PaymentVerifyDto(
    val bookingId: String,
    val gatewayOrderId: String,
    val gatewayPaymentId: String,
    /** HMAC signature from the gateway, verified server-side */
    val gatewaySignature: String
)
