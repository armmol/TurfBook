package com.sports.turfbook.database.tables

import com.sports.turfbook.domain.enums.PaymentGateway
import com.sports.turfbook.domain.enums.PaymentStatus
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PaymentsTable : UUIDTable("payments") {
    val bookingId = uuid("booking_id").references(BookingsTable.id).uniqueIndex()
    val amountInPaise = long("amount_in_paise")
    val gateway = enumerationByName<PaymentGateway>("gateway", 20)
    val gatewayOrderId = varchar("gateway_order_id", 100).nullable()
    val gatewayPaymentId = varchar("gateway_payment_id", 100).nullable()
    val gatewaySignature = text("gateway_signature").nullable()
    val status = enumerationByName<PaymentStatus>("status", 30)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}
