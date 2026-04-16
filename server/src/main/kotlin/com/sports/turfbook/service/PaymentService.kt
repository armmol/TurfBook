package com.sports.turfbook.service

import com.sports.turfbook.api.dto.payment.InitiatePaymentDto
import com.sports.turfbook.api.dto.payment.PaymentOrderDto
import com.sports.turfbook.api.dto.payment.PaymentVerifyDto
import com.sports.turfbook.database.tables.BookingsTable
import com.sports.turfbook.database.tables.PaymentsTable
import com.sports.turfbook.domain.enums.BookingStatus
import com.sports.turfbook.domain.enums.PaymentGateway
import com.sports.turfbook.domain.enums.PaymentStatus
import com.sports.turfbook.plugins.ConflictException
import com.sports.turfbook.plugins.NotFoundException
import com.sports.turfbook.plugins.UnauthorizedException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class PaymentService(private val bookingService: BookingService) {

    /**
     * Creates a gateway order and a local payment record.
     * Returns the order details the mobile SDK needs to launch the payment sheet.
     */
    fun initiatePayment(userId: String, dto: InitiatePaymentDto): PaymentOrderDto = transaction {
        val bookingUuid = UUID.fromString(dto.bookingId)

        val bookingRow = BookingsTable.selectAll()
            .where { BookingsTable.id eq bookingUuid }
            .singleOrNull() ?: throw NotFoundException("Booking not found")

        if (bookingRow[BookingsTable.userId].toString() != userId) {
            throw UnauthorizedException("Not your booking")
        }
        if (bookingRow[BookingsTable.status] != BookingStatus.PENDING_PAYMENT) {
            throw ConflictException("Booking is already ${bookingRow[BookingsTable.status].name.lowercase()}")
        }

        val amount = bookingRow[BookingsTable.amountInPaise]
        val provider = getProvider(dto.gateway)
        val orderId = provider.createOrder(amount, dto.bookingId)
        val now: Instant = Clock.System.now()

        PaymentsTable.insert {
            it[id] = UUID.randomUUID()
            it[bookingId] = bookingUuid
            it[amountInPaise] = amount
            it[gateway] = dto.gateway
            it[gatewayOrderId] = orderId
            it[status] = PaymentStatus.INITIATED
            it[createdAt] = now
            it[updatedAt] = now
        }

        PaymentOrderDto(
            orderId = orderId,
            amountInPaise = amount,
            gateway = dto.gateway,
            gatewayKeyId = provider.getKeyId()
        )
    }

    /**
     * Verifies gateway signature, marks payment SUCCESS, confirms booking.
     */
    fun verifyPayment(userId: String, dto: PaymentVerifyDto): com.sports.turfbook.api.dto.booking.BookingDto =
        transaction {
            val bookingUuid = UUID.fromString(dto.bookingId)

            val bookingRow = BookingsTable.selectAll()
                .where { BookingsTable.id eq bookingUuid }
                .singleOrNull() ?: throw NotFoundException("Booking not found")

            if (bookingRow[BookingsTable.userId].toString() != userId) {
                throw UnauthorizedException("Not your booking")
            }

            val paymentRow = PaymentsTable.selectAll()
                .where { PaymentsTable.bookingId eq bookingUuid }
                .singleOrNull() ?: throw NotFoundException("Payment record not found")

            val gateway = paymentRow[PaymentsTable.gateway]
            val provider = getProvider(gateway)

            if (!provider.verifySignature(dto.gatewayOrderId, dto.gatewayPaymentId, dto.gatewaySignature)) {
                throw UnauthorizedException("Payment signature verification failed")
            }

            val now: Instant = Clock.System.now()
            PaymentsTable.update({ PaymentsTable.id eq paymentRow[PaymentsTable.id] }) {
                it[gatewayPaymentId] = dto.gatewayPaymentId
                it[gatewaySignature] = dto.gatewaySignature
                it[status] = PaymentStatus.SUCCESS
                it[updatedAt] = now
            }

            bookingService.confirmBooking(dto.bookingId, dto.gatewayPaymentId)
        }

    private fun getProvider(gateway: PaymentGateway): PaymentProvider = when (gateway) {
        PaymentGateway.RAZORPAY -> RazorpayProvider()
        PaymentGateway.PHONEPE -> PhonePeProvider()
        PaymentGateway.PAYTM -> PaytmProvider()
    }
}

// ── Gateway provider interface + stubs ──────────────────────────────────────

interface PaymentProvider {
    fun createOrder(amountInPaise: Long, receiptId: String): String
    fun verifySignature(orderId: String, paymentId: String, signature: String): Boolean
    fun getKeyId(): String
}

class RazorpayProvider : PaymentProvider {
    override fun createOrder(amountInPaise: Long, receiptId: String): String =
        "order_${UUID.randomUUID().toString().replace("-", "").take(14)}"

    override fun verifySignature(orderId: String, paymentId: String, signature: String): Boolean {
        // In DEV_MODE skip verification; production: HMAC-SHA256(secret, orderId|paymentId)
        if (System.getenv("DEV_MODE") == "true") return true
        // TODO: implement Razorpay HMAC verification
        return false
    }

    override fun getKeyId(): String =
        System.getenv("RAZORPAY_KEY_ID") ?: "rzp_test_placeholder"
}

class PhonePeProvider : PaymentProvider {
    override fun createOrder(amountInPaise: Long, receiptId: String) = "PP_${UUID.randomUUID()}"
    override fun verifySignature(orderId: String, paymentId: String, signature: String) = true
    override fun getKeyId() = System.getenv("PHONEPE_MERCHANT_ID") ?: "TURFBOOKDEV"
}

class PaytmProvider : PaymentProvider {
    override fun createOrder(amountInPaise: Long, receiptId: String) = "PT_${UUID.randomUUID()}"
    override fun verifySignature(orderId: String, paymentId: String, signature: String) = true
    override fun getKeyId() = System.getenv("PAYTM_MID") ?: "TURFBOOKDEV"
}
