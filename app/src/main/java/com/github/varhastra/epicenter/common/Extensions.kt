package com.github.varhastra.epicenter.common

import android.content.SharedPreferences
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

fun Instant.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(this, ZoneId.systemDefault())
}


fun SharedPreferences.Editor.putDouble(key: String, value: Double): SharedPreferences.Editor {
    putLong(key, value.toRawBits())
    return this
}

fun SharedPreferences.getDouble(key: String, defaultValue: Double): Double {
    return if (!contains(key)) {
        defaultValue
    } else {
        Double.fromBits(getLong(key, 0))
    }
}