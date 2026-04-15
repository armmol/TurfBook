package com.sports.turfbook.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class SportType(val displayName: String) {
    FOOTBALL("Football"),
    CRICKET("Cricket"),
    TENNIS("Tennis"),
    BADMINTON("Badminton"),
    BASKETBALL("Basketball"),
    VOLLEYBALL("Volleyball"),
    HOCKEY("Hockey"),
    KABADDI("Kabaddi"),
    OTHER("Other")
}
