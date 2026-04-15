package com.sports.turfbook

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform