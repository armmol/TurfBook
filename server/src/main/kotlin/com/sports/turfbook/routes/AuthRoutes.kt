package com.sports.turfbook.routes

import com.sports.turfbook.api.dto.auth.OtpRequestDto
import com.sports.turfbook.api.dto.auth.OtpVerifyDto
import com.sports.turfbook.api.dto.auth.RefreshTokenDto
import com.sports.turfbook.api.dto.common.ApiResponse
import com.sports.turfbook.plugins.UnauthorizedException
import com.sports.turfbook.plugins.userId
import com.sports.turfbook.service.AuthService
import com.sports.turfbook.service.OtpService
import com.sports.turfbook.util.normalisePhone
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(otpService: OtpService, authService: AuthService) {

    route("/auth") {

        /**
         * POST /auth/otp/request
         * Body: { "phone": "9876543210" }
         * Sends a 6-digit OTP via SMS.
         * In DEV_MODE, returns the OTP in the response for testing.
         */
        post("/otp/request") {
            val body = call.receive<OtpRequestDto>()
            val phone = normalisePhone(body.phone)
                ?: throw IllegalArgumentException("Invalid Indian mobile number: ${body.phone}")

            val devOtp = otpService.sendOtp(phone)

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    message = "OTP sent to $phone",
                    data = if (devOtp != null) mapOf("otp" to devOtp) else null
                )
            )
        }

        /**
         * POST /auth/otp/verify
         * Body: { "phone": "9876543210", "otp": "123456" }
         * Returns JWT access + refresh tokens.
         */
        post("/otp/verify") {
            val body = call.receive<OtpVerifyDto>()
            val phone = normalisePhone(body.phone)
                ?: throw IllegalArgumentException("Invalid Indian mobile number: ${body.phone}")

            if (!otpService.verifyOtp(phone, body.otp)) {
                throw UnauthorizedException("Invalid or expired OTP")
            }

            val authResponse = authService.loginOrRegister(phone)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = authResponse))
        }

        /**
         * POST /auth/refresh
         * Body: { "refreshToken": "uuid" }
         * Rotates the refresh token and returns a new token pair.
         */
        post("/refresh") {
            val body = call.receive<RefreshTokenDto>()
            val (newAccess, newRefresh) = authService.refreshTokens(body.refreshToken)
                ?: throw UnauthorizedException("Refresh token is invalid or expired")

            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = mapOf("accessToken" to newAccess, "refreshToken" to newRefresh)
                )
            )
        }

        /**
         * POST /auth/logout
         * Requires: JWT bearer token
         * Body: { "refreshToken": "uuid" }
         */
        authenticate("jwt-auth") {
            post("/logout") {
                val body = call.receive<RefreshTokenDto>()
                authService.revokeRefreshToken(body.refreshToken)
                call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Logged out"))
            }
        }
    }
}
