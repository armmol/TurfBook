package com.sports.turfbook.routes

import com.sports.turfbook.api.dto.common.ApiResponse
import com.sports.turfbook.domain.KnownSports
import com.sports.turfbook.domain.Sport
import com.sports.turfbook.plugins.userId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.sports.turfbook.service.SportsRegistryService

fun Route.sportsRoutes(sportsService: SportsRegistryService) {

    route("/sports") {

        /** GET /sports — list all active registered sports */
        get {
            val sports = KnownSports.all.sortedBy { it.displayName }
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = sports))
        }

        /** GET /sports/{code} — get a single sport by code */
        get("/{code}") {
            val code = call.parameters["code"]?.uppercase()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing code"))

            if (!KnownSports.isKnown(code)) {
                return@get call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, message = "Sport not found"))
            }

            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = KnownSports.fromCode(code)))
        }

        /** POST /sports — admin: register a new custom sport */
        authenticate("jwt-auth") {
            post {
                val dto = call.receive<Sport>()
                val sport = dto.copy(code = dto.code.uppercase())
                sportsService.registerSport(sport)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = sport, message = "Sport registered"))
            }
        }
    }
}
