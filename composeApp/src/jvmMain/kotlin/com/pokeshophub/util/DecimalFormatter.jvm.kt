package com.pokeshophub.util

actual fun Double.formatTwoDecimals(): String {
    return String.format(java.util.Locale.US, "%.2f", this)
}
