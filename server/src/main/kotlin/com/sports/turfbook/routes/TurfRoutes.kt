package com.sports.turfbook.routes

import com.sports.turfbook.api.dto.common.ApiResponse
import com.sports.turfbook.api.dto.court.CreateCourtDto
import com.sports.turfbook.api.dto.court.UpdateCourtDto
import com.sports.turfbook.api.dto.turf.CreateTurfDto
import com.sports.turfbook.api.dto.turf.TurfSearchQueryDto
import com.sports.turfbook.api.dto.turf.UpdateTurfDto
import com.sports.turfbook.plugins.NotFoundException
import com.sports.turfbook.plugins.userId
import com.sports.turfbook.service.CourtService
import com.sports.turfbook.service.ReviewService
import com.sports.turfbook.service.SlotService
import com.sports.turfbook.service.TurfService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.turfRoutes(
    turfService: TurfService,
    slotService: SlotService,
    courtService: CourtService,
    reviewService: ReviewService
) {

    route("/turfs") {

        /** GET /turfs?city=Mumbai&sport=FOOTBALL&page=1&pageSize=20 */
        get {
            val dto = TurfSearchQueryDto(
                city = call.request.queryParameters["city"],
                sport = call.request.queryParameters["sport"],
                date = call.request.queryParameters["date"],
                userLatitude = call.request.queryParameters["userLatitude"]?.toDoubleOrNull(),
                userLongitude = call.request.queryParameters["userLongitude"]?.toDoubleOrNull(),
                radiusKm = call.request.queryParameters["radiusKm"]?.toFloatOrNull(),
                page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            )
            val results = turfService.searchTurfs(dto)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = results))
        }

        /** GET /turfs/{id} */
        get("/{id}") {
            val id = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
            val turf = turfService.getTurfById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, message = "Turf not found"))
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = turf))
        }

        /** GET /turfs/{id}/courts */
        get("/{id}/courts") {
            val turfId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
            val courts = courtService.getCourtsForTurf(turfId)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = courts))
        }

        /**
         * GET /turfs/{id}/courts/{courtId}/slots?date=2024-01-15
         */
        get("/{id}/courts/{courtId}/slots") {
            val turfId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
            val courtId = call.parameters["courtId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing courtId"))
            val date = call.request.queryParameters["date"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing date"))

            val availability = slotService.getAvailability(turfId, courtId, date)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = availability))
        }

        /** GET /turfs/{id}/reviews?page=1&pageSize=20 */
        get("/{id}/reviews") {
            val turfId = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val reviews = reviewService.getTurfReviews(turfId, page, pageSize)
            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = reviews))
        }

        // ── Auth-required endpoints ───────────────────────────────────────────

        authenticate("jwt-auth") {

            /** POST /turfs — turf owner creates a new turf with initial courts */
            post {
                val ownerId = call.userId
                val dto = call.receive<CreateTurfDto>()
                val turf = turfService.createTurf(ownerId, dto)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = turf))
            }

            /** PATCH /turfs/{id} */
            patch("/{id}") {
                val turfId = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
                val ownerId = call.userId
                val dto = call.receive<UpdateTurfDto>()
                val turf = turfService.updateTurf(turfId, ownerId, dto)
                    ?: return@patch call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, message = "Turf not found or not yours"))
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = turf))
            }

            /** POST /turfs/{id}/courts — add a court to an existing turf */
            post("/{id}/courts") {
                val turfId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
                val ownerId = call.userId
                val dto = call.receive<CreateCourtDto>()
                val court = courtService.createCourt(turfId, ownerId, dto)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = court))
            }

            /** PATCH /turfs/{id}/courts/{courtId} */
            patch("/{id}/courts/{courtId}") {
                val turfId = call.parameters["id"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
                val courtId = call.parameters["courtId"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing courtId"))
                val ownerId = call.userId
                val dto = call.receive<UpdateCourtDto>()
                val court = courtService.updateCourt(turfId, courtId, ownerId, dto)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = court))
            }

            /** DELETE /turfs/{id}/courts/{courtId} — soft-deactivates the court */
            delete("/{id}/courts/{courtId}") {
                val turfId = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
                val courtId = call.parameters["courtId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing courtId"))
                val ownerId = call.userId
                courtService.deactivateCourt(turfId, courtId, ownerId)
                call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Court deactivated"))
            }

            /** POST /turfs/{id}/reviews */
            post("/{id}/reviews") {
                val turfId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, message = "Missing id"))
                val userId = call.userId
                val dto = call.receive<com.sports.turfbook.api.dto.review.CreateReviewDto>()
                val review = reviewService.createReview(userId, turfId, dto)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = review))
            }
        }
    }
}
