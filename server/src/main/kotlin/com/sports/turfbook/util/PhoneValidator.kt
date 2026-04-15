package com.sports.turfbook.util

private val INDIAN_PHONE_REGEX = Regex("""^\+91[6-9]\d{9}$""")

/**
 * Validates and normalises an Indian mobile number.
 * Accepts:
 *  - "+919876543210"  (already normalised)
 *  - "9876543210"     (10-digit, prepends +91)
 *  - "09876543210"    (with leading 0, strips and prepends +91)
 */
fun normalisePhone(raw: String): String? {
    val stripped = raw.trim()
    val normalised = when {
        stripped.startsWith("+91") -> stripped
        stripped.startsWith("91") && stripped.length == 12 -> "+$stripped"
        stripped.startsWith("0") && stripped.length == 11 -> "+91${stripped.drop(1)}"
        stripped.length == 10 -> "+91$stripped"
        else -> return null
    }
    return if (INDIAN_PHONE_REGEX.matches(normalised)) normalised else null
}
