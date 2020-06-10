package me.alex.pet.apps.epicenter.common.extensions

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

fun Instant.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(this, ZoneId.systemDefault())
}

fun Instant.toLocalDate(): LocalDate {
    return LocalDateTime.ofInstant(this, ZoneId.systemDefault()).toLocalDate()
}