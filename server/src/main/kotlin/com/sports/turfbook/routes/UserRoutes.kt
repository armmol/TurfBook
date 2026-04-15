package com.sports.turfbook.routes

import com.sports.turfbook.api.dto.common.ApiResponse
import com.sports.turfbook.api.dto.user.UpdateProfileDto
import com.sports.turfbook.plugins.NotFoundException
import com.sports.turfbook.plugins.userId
import com.sports.turfbook.service.UserService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {

    authenticate("jwt-auth") {
        route("/users") {

            /**
             * GET /users/me
             * Returns the authenticated user's profile.
             */
            get("/me") {
                val user = userService.findById(call.userId)
                    ?: throw NotFoundException("User not found")
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = user))
            }

            /**
             * PATCH /users/me
             * Updates name, email, city, preferred sports, or profile photo URL.
             * All fields are optional — only provided fields are updated.
             */
            patch("/me") {
                val dto = call.receive<UpdateProfileDto>()

                val name = dto.name
                if (name != null && name.isBlank()) {
                    throw IllegalArgumentException("Name cannot be blank")
                }
                val email = dto.email
                if (email != null && !email.contains("@")) {
                    throw IllegalArgumentException("Invalid email address")
                }

                val updated = userService.updateProfile(call.userId, dto)
                    ?: throw NotFoundException("User not found")

                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = updated))
            }
        }
    }
}
