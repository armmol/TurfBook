package com.sports.turfbook.routes

import com.sports.turfbook.api.dto.booking.CreateBookingDto
import com.sports.turfbook.api.dto.common.ApiResponse
import com.sports.turfbook.plugins.userId
import com.sports.turfbook.service.BookingService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookingRoutes(bookingService: BookingService) {

    authenticate("jwt-auth") {

        route("/bookings") {

            /**
             * POST /bookings
             * Creates a booking and holds the slot for 5 minutes via Redis.
             * Returns the booking in PENDING_PAYMENT status.
             */
            post {
                val userId = call.userId
                val dto = call.receive<CreateBookingDto>()
                val booking = bookingService.createBooking(userId, dto)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = booking))
            }

            /** GET /bookings — list the caller's bookings */
            get {
                val userId = call.userId
                val bookings = bookingService.getUserBookings(userId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = bookings))
            }

            /** GET /bookings/{id} */
            get("/{id}") {
                val bookingId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
                val userId = call.userId
                val booking = bookingService.getBookingById(bookingId, userId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = booking))
            }

            /**
             * POST /bookings/{id}/cancel
             * Body (optional): { "reason": "Change of plan" }
             */
            post("/{id}/cancel") {
                val bookingId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
                val userId = call.userId

                @kotlinx.serialization.Serializable
                data class CancelRequest(val reason: String? = null)

                val body = runCatching { call.receive<CancelRequest>() }.getOrNull()
                val booking = bookingService.cancelBooking(bookingId, userId, body?.reason)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = booking))
            }
        }
    }
}
