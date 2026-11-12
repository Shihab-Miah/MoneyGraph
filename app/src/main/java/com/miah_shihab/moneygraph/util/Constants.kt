package com.miah_shihab.moneygraph.util

// ---------------------------------------------------------------------------
// Backup
// ---------------------------------------------------------------------------
const val BACKUP_FILE_EXTENSION = ".mgbak"
const val BACKUP_AES_KEY = "money_graph_key_2024"
const val BACKUP_SALT = "money_graph_salt"
const val DRIVE_FOLDER_NAME = "MoneyGraph Backups"

// ---------------------------------------------------------------------------
// DataStore / SharedPreferences keys
// ---------------------------------------------------------------------------
const val PREFS_THEME_COLOR = "theme_color"
const val PREFS_DARK_MODE = "dark_mode"
const val PREFS_CURRENCY_SYMBOL = "currency_symbol"
const val PREFS_PIN = "app_pin"
const val PREFS_USE_BIOMETRIC = "use_biometric"
const val PREFS_GOOGLE_ACCOUNT_EMAIL = "google_account_email"

// ---------------------------------------------------------------------------
// Defaults
// ---------------------------------------------------------------------------
/** Bangladeshi Taka symbol – can be changed in Settings. */
const val DEFAULT_CURRENCY_SYMBOL = "৳"

// ---------------------------------------------------------------------------
// Theme color palette
// ---------------------------------------------------------------------------
/**
 * Ten Material-style seed colours stored as ARGB [Long] values.
 * These are passed to [androidx.compose.ui.graphics.Color] and subsequently to
 * [com.google.material.color.MaterialTheme] dynamic colour generation.
 */
object ThemeColors {
    val options: List<Long> = listOf(
        0xFF6650A4L, // Purple (Material3 baseline)
        0xFF4285F4L, // Google Blue
        0xFF34A853L, // Google Green
        0xFFEA4335L, // Google Red
        0xFFFBBC05L, // Google Yellow
        0xFF00BCD4L, // Cyan
        0xFFFF5722L, // Deep Orange
        0xFF795548L, // Brown
        0xFF607D8BL, // Blue Grey
        0xFF9C27B0L  // Purple accent
    )
}

// ---------------------------------------------------------------------------
// Icon resource maps
// ---------------------------------------------------------------------------
/**
 * Placeholder icon resource IDs for accounts and categories.
 * In production, replace Int values with actual `R.drawable.*` references.
 *
 * Account icon IDs  → 1–10
 * Category icon IDs → 11–20
 */
object Icons {

    /** Map of human-readable account icon names to their resource ID placeholders. */
    val accountIcons: Map<String, Int> = mapOf(
        "wallet"       to 1,
        "bank"         to 2,
        "cash"         to 3,
        "credit_card"  to 4,
        "savings"      to 5,
        "investment"   to 6,
        "mobile_money" to 7,
        "piggy_bank"   to 8,
        "safe"         to 9,
        "other"        to 10
    )

    /** Map of human-readable category icon names to their resource ID placeholders. */
    val categoryIcons: Map<String, Int> = mapOf(
        "food"          to 11,
        "transport"     to 12,
        "shopping"      to 13,
        "entertainment" to 14,
        "health"        to 15,
        "education"     to 16,
        "bills"         to 17,
        "salary"        to 18,
        "freelance"     to 19,
        "other"         to 20
    )

    /** Returns the resource ID for a given icon [name], or the "other" fallback. */
    fun accountIconRes(name: String): Int = accountIcons[name] ?: 10
    fun categoryIconRes(name: String): Int = categoryIcons[name] ?: 20
}
