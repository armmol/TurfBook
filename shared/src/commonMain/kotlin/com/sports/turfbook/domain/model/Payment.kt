package com.sports.turfbook.domain.model

import com.sports.turfbook.domain.enums.PaymentGateway
import com.sports.turfbook.domain.enums.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String,
    val bookingId: String,
    /** Amount in paise */
    val amountInPaise: Long,
    val gateway: PaymentGateway,
    /** Order ID created on the gateway (e.g. Razorpay order_xxx) */
    val gatewayOrderId: String? = null,
    /** Payment ID returned by gateway on success */
    val gatewayPaymentId: String? = null,
    /** Gateway signature for verification */
    val gatewaySignature: String? = null,
    val status: PaymentStatus,
    val createdAt: String,
    val updatedAt: String
)
