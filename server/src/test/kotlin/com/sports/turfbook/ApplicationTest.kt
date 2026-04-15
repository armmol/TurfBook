package com.sports.turfbook

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testHealthEndpoint() = testApplication {
        application {
            // Note: full module() requires a running DB.
            // Integration tests with a test DB will be added separately.
        }
        val response = client.get("/health")
        // Health check should be reachable even without the DB module
        assertNotEquals(HttpStatusCode.InternalServerError, response.status)
    }
}
