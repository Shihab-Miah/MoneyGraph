package com.miah_shihab.moneygraph.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * Safely parses a [String] to [BigDecimal], returning [BigDecimal.ZERO] if the string
 * is blank, null-ish, or not a valid decimal number.
 */
fun String.toBigDecimalSafe(): BigDecimal {
    return try {
        if (this.isBlank()) BigDecimal.ZERO
        else BigDecimal(this.trim())
    } catch (e: NumberFormatException) {
        BigDecimal.ZERO
    }
}

/**
 * Formats a [BigDecimal] to a plain string with exactly 2 decimal places.
 * Uses [RoundingMode.HALF_UP] for rounding.
 * Example: 1234.5 → "1234.50"
 */
fun BigDecimal.toFormattedString(): String {
    return this.setScale(2, RoundingMode.HALF_UP).toPlainString()
}

/**
 * Formats a [BigDecimal] as a currency string with the given [symbol] prefix.
 * Example: BigDecimal("1234.5").toCurrencyString("৳") → "৳ 1234.50"
 */
fun BigDecimal.toCurrencyString(symbol: String): String {
    return "$symbol ${this.toFormattedString()}"
}

/**
 * Formats a Unix-epoch millisecond timestamp as "dd MMM yyyy".
 * Example: 1700000000000L → "15 Nov 2023"
 */
fun Long.toLocalDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Formats a Unix-epoch millisecond timestamp as "MMM yyyy".
 * Example: 1700000000000L → "Nov 2023"
 */
fun Long.toMonthYearString(): String {
    val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Returns the Unix-epoch milliseconds for the very start (00:00:00.000) of the
 * calendar month that contains this timestamp.
 */
fun Long.startOfMonth(): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = this@startOfMonth
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

/**
 * Returns the Unix-epoch milliseconds for the very end (23:59:59.999) of the
 * calendar month that contains this timestamp.
 */
fun Long.endOfMonth(): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = this@endOfMonth
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return cal.timeInMillis
}

/**
 * Generates a pseudo-unique ID by combining the current system time in milliseconds
 * with a random integer in [0, 1000).  Suitable for local Room entity IDs.
 */
fun generateId(): Long = System.currentTimeMillis() + Random.nextInt(1000)
