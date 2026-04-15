package com.sports.turfbook.api.dto.payment

import com.sports.turfbook.domain.enums.PaymentGateway
import kotlinx.serialization.Serializable

/** Returned by POST /payments/initiate — used to initialise the payment SDK on device */
@Serializable
data class PaymentOrderDto(
    /** Gateway-specific order ID (e.g. Razorpay order_xxx) */
    val orderId: String,
    /** Amount in paise */
    val amountInPaise: Long,
    val currency: String = "INR",
    val gateway: PaymentGateway,
    /** Public/key ID to initialise the gateway SDK — never the secret key */
    val gatewayKeyId: String
)
