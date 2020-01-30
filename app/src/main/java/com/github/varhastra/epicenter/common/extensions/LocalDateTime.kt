package com.github.varhastra.epicenter.common.extensions

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

val LocalDateTime.isToday
    get() = this.toLocalDate() == LocalDate.now()

val LocalDateTime.isYesterday
    get() = this.toLocalDate() == LocalDate.now().minusDays(1)