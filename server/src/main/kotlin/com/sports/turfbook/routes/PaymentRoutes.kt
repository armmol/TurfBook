package com.sports.turfbook.routes

import com.sports.turfbook.api.dto.common.ApiResponse
import com.sports.turfbook.api.dto.payment.InitiatePaymentDto
import com.sports.turfbook.api.dto.payment.PaymentVerifyDto
import com.sports.turfbook.plugins.userId
import com.sports.turfbook.service.BookingService
import com.sports.turfbook.service.PaymentService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.paymentRoutes(paymentService: PaymentService) {

    authenticate("jwt-auth") {

        route("/payments") {

            /**
             * POST /payments/initiate
             * Creates a gateway order and a local payment record.
             * Returns the order ID + amount the mobile SDK needs to launch the payment sheet.
             */
            post("/initiate") {
                val userId = call.userId
                val dto = call.receive<InitiatePaymentDto>()
                val order = paymentService.initiatePayment(userId, dto)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = order))
            }

            /**
             * POST /payments/verify
             * Verifies the gateway signature, marks the payment as SUCCESS,
             * and confirms the booking. Returns the confirmed BookingDto.
             */
            post("/verify") {
                val userId = call.userId
                val dto = call.receive<PaymentVerifyDto>()
                val booking = paymentService.verifyPayment(userId, dto)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = booking))
            }
        }
    }
}
