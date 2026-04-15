package com.sports.turfbook.plugins

import com.sports.turfbook.api.dto.common.ApiResponse
import com.sports.turfbook.routes.authRoutes
import com.sports.turfbook.routes.userRoutes
import com.sports.turfbook.service.AuthService
import com.sports.turfbook.service.ConsoleSmsProvider
import com.sports.turfbook.service.OtpService
import com.sports.turfbook.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // Dependency construction — swap ConsoleSmsProvider for Msg91SmsProvider in production
    val smsProvider = ConsoleSmsProvider()
    val otpService = OtpService(smsProvider)
    val userService = UserService()
    val authService = AuthService(userService)

    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, message = "TurfBook API is running"))
        }

        route("/api/v1") {
            authRoutes(otpService, authService)
            userRoutes(userService)
        }
    }
}
