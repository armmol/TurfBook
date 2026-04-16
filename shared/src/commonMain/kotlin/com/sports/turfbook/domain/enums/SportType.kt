package com.sports.turfbook.domain.enums

/**
 * Sport codes are now plain Strings (e.g. "FOOTBALL") looked up via
 * [com.sports.turfbook.domain.KnownSports].
 * This typealias keeps references in older code compiling during migration.
 */
@Deprecated(
    message = "Use String sport codes with KnownSports registry instead of SportType enum.",
    replaceWith = ReplaceWith("String")
)
typealias SportType = String
