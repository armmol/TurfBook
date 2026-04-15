plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = "com.sports.turfbook"
version = "1.0.0"

application {
    mainClass.set("com.sports.turfbook.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)

    // Ktor server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.hikaricp)
    implementation(libs.postgresql)

    // Serialization & logging
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logback)

    // Test
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.testJunit)
}
