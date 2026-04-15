package com.sports.turfbook.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        // In production, replace with your actual domain(s)
        if (System.getenv("DEV_MODE") == "true") {
            anyHost()
        } else {
            allowHost("turfbook.in", schemes = listOf("https"))
            allowHost("api.turfbook.in", schemes = listOf("https"))
        }
    }
}
