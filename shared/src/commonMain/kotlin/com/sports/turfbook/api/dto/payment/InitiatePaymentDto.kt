package com.sports.turfbook.api.dto.payment

import com.sports.turfbook.domain.enums.PaymentGateway
import kotlinx.serialization.Serializable

/** POST /payments/initiate */
@Serializable
data class InitiatePaymentDto(
    val bookingId: String,
    val gateway: PaymentGateway
)
