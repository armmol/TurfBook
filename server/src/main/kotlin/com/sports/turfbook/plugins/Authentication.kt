package com.sports.turfbook.plugins

import com.sports.turfbook.util.JwtUtil
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("jwt-auth") {
            realm = JwtUtil.realm
            verifier(JwtUtil.verifier())
            validate { credential ->
                val userId = credential.payload.subject
                val phone = credential.payload.getClaim("phone").asString()
                if (!userId.isNullOrBlank() && !phone.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}

/** Extracts the authenticated user's ID from the JWT principal */
val ApplicationCall.userId: String
    get() = principal<JWTPrincipal>()!!.payload.subject

/** Extracts the authenticated user's phone from the JWT principal */
val ApplicationCall.userPhone: String
    get() = principal<JWTPrincipal>()!!.payload.getClaim("phone").asString()
