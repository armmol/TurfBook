package com.sports.turfbook.plugins

import com.sports.turfbook.api.dto.common.ApiResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("StatusPages")

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = cause.message)
            )
        }

        exception<UnauthorizedException> { call, cause ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse<Unit>(success = false, error = cause.message)
            )
        }

        exception<NotFoundException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Unit>(success = false, error = cause.message)
            )
        }

        exception<ConflictException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiResponse<Unit>(success = false, error = cause.message)
            )
        }

        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<Unit>(success = false, error = "An unexpected error occurred")
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiResponse<Unit>(success = false, error = "Resource not found")
            )
        }

        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiResponse<Unit>(success = false, error = "Authentication required")
            )
        }
    }
}

class UnauthorizedException(message: String = "Unauthorized") : Exception(message)
class NotFoundException(message: String = "Not found") : Exception(message)
class ConflictException(message: String = "Conflict") : Exception(message)
