package com.sports.turfbook.domain

/**
 * Central registry of sports supported by TurfBook.
 *
 * - Well-known sports are defined as constants below.
 * - New sports (indoor, niche) can be added at runtime via [register] —
 *   no code or DB migration needed.
 * - Turfs declare which sports they support; the list here drives validation
 *   and UI display.
 *
 * Usage:
 * ```
 * // Lookup by code (from API / DB)
 * val sport = KnownSports.fromCode("FOOTBALL")  // → Sport("FOOTBALL", "Football", ...)
 *
 * // Add a custom sport (e.g., from a DB-backed admin panel)
 * KnownSports.register(Sport("GATKA", "Gatka", "ic_gatka", 5, 10))
 * ```
 */
object KnownSports {

    // ── Predefined sports ──────────────────────────────────────────────────

    val FOOTBALL    = Sport("FOOTBALL",    "Football",    "ic_football",    5, 11)
    val CRICKET     = Sport("CRICKET",     "Cricket",     "ic_cricket",     6, 11)
    val TENNIS      = Sport("TENNIS",      "Tennis",      "ic_tennis",      1, 2)
    val BADMINTON   = Sport("BADMINTON",   "Badminton",   "ic_badminton",   1, 2)
    val BASKETBALL  = Sport("BASKETBALL",  "Basketball",  "ic_basketball",  5, 5)
    val VOLLEYBALL  = Sport("VOLLEYBALL",  "Volleyball",  "ic_volleyball",  6, 6)
    val HOCKEY      = Sport("HOCKEY",      "Hockey",      "ic_hockey",      6, 11)
    val KABADDI     = Sport("KABADDI",     "Kabaddi",     "ic_kabaddi",     7, 7)
    val THROWBALL   = Sport("THROWBALL",   "Throwball",   "ic_throwball",   9, 9)
    val BOX_CRICKET = Sport("BOX_CRICKET", "Box Cricket", "ic_box_cricket", 4, 8)

    // ── Registry ────────────────────────────────────────────────────────────

    private val registry: MutableMap<String, Sport> = mutableMapOf(
        FOOTBALL.code    to FOOTBALL,
        CRICKET.code     to CRICKET,
        TENNIS.code      to TENNIS,
        BADMINTON.code   to BADMINTON,
        BASKETBALL.code  to BASKETBALL,
        VOLLEYBALL.code  to VOLLEYBALL,
        HOCKEY.code      to HOCKEY,
        KABADDI.code     to KABADDI,
        THROWBALL.code   to THROWBALL,
        BOX_CRICKET.code to BOX_CRICKET
    )

    /** All currently registered sports. */
    val all: List<Sport> get() = registry.values.toList()

    /**
     * Look up a sport by code (case-insensitive).
     * Returns a generic [Sport] placeholder if the code is unknown — this
     * allows the app to handle future sports gracefully without crashing.
     */
    fun fromCode(code: String): Sport =
        registry[code.uppercase()] ?: Sport(code.uppercase(), code, "ic_default")

    /** Returns true if [code] corresponds to a registered sport. */
    fun isKnown(code: String): Boolean = code.uppercase() in registry

    /**
     * Register a new sport at runtime (e.g., loaded from an admin DB table).
     * Silently overwrites an existing entry with the same code.
     */
    fun register(sport: Sport) {
        registry[sport.code.uppercase()] = sport.copy(code = sport.code.uppercase())
    }

    /**
     * Bulk-register sports (e.g., on server startup from [SportsTable]).
     */
    fun registerAll(sports: List<Sport>) = sports.forEach { register(it) }
}
