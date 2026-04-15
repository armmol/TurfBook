package com.sports.turfbook

import com.sports.turfbook.database.DatabaseFactory
import com.sports.turfbook.plugins.configureAuthentication
import com.sports.turfbook.plugins.configureCORS
import com.sports.turfbook.plugins.configureRouting
import com.sports.turfbook.plugins.configureSerialization
import com.sports.turfbook.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import org.slf4j.event.Level

fun main() {
    embeddedServer(
        factory = Netty,
        port = System.getenv("PORT")?.toInt() ?: SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureCORS()
    configureStatusPages()
    configureAuthentication()
    install(CallLogging) { level = Level.INFO }
    configureRouting()
}
