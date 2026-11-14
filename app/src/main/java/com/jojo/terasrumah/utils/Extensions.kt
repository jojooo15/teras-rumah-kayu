package com.jojo.terasrumah.utils

import com.jojo.terasrumah.data.model.*
import java.text.SimpleDateFormat
import java.util.*

// Additional extension functions that might be needed across the app

// Date formatting extensions
fun Date.formatToString(pattern: String = "dd MMM yyyy"): String {
    val formatter = SimpleDateFormat(pattern, Locale("id", "ID"))
    return formatter.format(this)
}

fun Date.formatToTimeAgo(): String {
    val now = System.currentTimeMillis()
    val time = this.time
    val diff = now - time
    
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    
    return when {
        months > 0 -> "${months} bulan lalu"
        weeks > 0 -> "${weeks} minggu lalu"
        days > 0 -> "${days} hari lalu"
        hours > 0 -> "${hours} jam lalu"
        minutes > 0 -> "${minutes} menit lalu"
        else -> "Baru saja"
    }
}

// Currency formatting
fun Int.formatCurrency(): String {
    return "Rp ${String.format("%,d", this).replace(',', '.')}"
}

fun Double.formatCurrency(): String {
    return "Rp ${String.format("%,.0f", this).replace(',', '.')}"
}

// Rating extensions
fun Double.formatRating(): String {
    return String.format("%.1f", this)
}

fun Int.toStarRating(): String {
    return "★".repeat(this) + "☆".repeat(5 - this)
}

// Validation extensions
fun String.isValidPhoneNumber(): Boolean {
    val phoneRegex = "^(\\+62|62|0)[0-9]{9,13}$".toRegex()
    return phoneRegex.matches(this)
}

fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(this)
}

// Collection extensions
fun <T> List<T>.isNotEmpty(): Boolean = this.isNotEmpty()

fun <T> List<T>.safeGet(index: Int): T? {
    return if (index >= 0 && index < size) this[index] else null
}

// String extensions
fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        }
    }
}

fun String.truncate(maxLength: Int, suffix: String = "..."): String {
    return if (length <= maxLength) this else take(maxLength - suffix.length) + suffix
}

// Review specific extensions (backup in case of import issues)
fun Review.getFormattedTimeAgo(): String {
    return createdAt.formatToTimeAgo()
}

fun Review.getFormattedRating(): String {
    return "$rating/5"
}

fun Review.isRecentReview(): Boolean {
    val now = System.currentTimeMillis()
    val reviewTime = createdAt.time
    val hoursDiff = (now - reviewTime) / (1000 * 60 * 60)
    return hoursDiff <= 24
}

// Removed voucher extensions - using points only

// User specific extensions (backup in case of import issues)
fun User.getFormattedPoints(): String {
    return points.formatCurrency().replace("Rp ", "") + " poin"
}

fun User.getLevelProgress(): Float {
    return getProgressToNextLevel()
}

fun User.getFormattedLevel(): String {
    return getUserLevelInfo().name
}

// Removed UserVoucher extensions - using points only

// Generic utility functions
fun getCurrentTimestamp(): Long = System.currentTimeMillis()

fun formatTimestamp(timestamp: Long, pattern: String = "dd MMM yyyy HH:mm"): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat(pattern, Locale("id", "ID"))
    return formatter.format(date)
}

fun calculatePercentage(value: Int, total: Int): Float {
    return if (total == 0) 0f else (value.toFloat() / total.toFloat()) * 100f
}

fun generateRandomId(): String {
    return UUID.randomUUID().toString()
}

// Safe casting extensions
inline fun <reified T> Any?.safeCast(): T? {
    return this as? T
}

// Null safety extensions
fun <T> T?.orDefault(default: T): T {
    return this ?: default
}

fun String?.orEmpty(): String {
    return this ?: ""
}

fun Int?.orZero(): Int {
    return this ?: 0
}

fun Double?.orZero(): Double {
    return this ?: 0.0
}
