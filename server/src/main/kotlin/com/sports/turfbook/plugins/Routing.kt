package com.sports.turfbook.plugins

import com.sports.turfbook.api.dto.common.ApiResponse
import com.sports.turfbook.routes.*
import com.sports.turfbook.service.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // ── Dependency construction ──────────────────────────────────────────────
    // Swap ConsoleSmsProvider for Msg91SmsProvider in production
    val smsProvider = ConsoleSmsProvider()
    val otpService = OtpService(smsProvider)
    val userService = UserService()
    val authService = AuthService(userService)

    val turfService = TurfService()
    val slotService = SlotService()
    val bookingService = BookingService()
    val paymentService = PaymentService(bookingService)
    val reviewService = ReviewService()
    val sportsRegistryService = SportsRegistryService()

    routing {
        // ── Health check ──────────────────────────────────────────────────
        get("/health") {
            call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "TurfBook API is running"))
        }

        route("/api/v1") {
            // Auth — OTP login/register + token refresh
            authRoutes(otpService, authService)

            // User profile
            userRoutes(userService)

            // Sports registry — list / lookup / register custom sports
            sportsRoutes(sportsRegistryService)

            // Turfs — search, details, slots, reviews
            turfRoutes(turfService, slotService, reviewService)

            // Bookings — create, list, get, cancel
            bookingRoutes(bookingService)

            // Payments — initiate & verify
            paymentRoutes(paymentService)
        }
    }
}
