package com.github.varhastra.epicenter.presentation.main.feed

import com.github.varhastra.epicenter.presentation.common.AlertLevel

data class EventViewBlock(
        val id: String,
        val title: String,
        val magnitude: String,
        val alertLevel: AlertLevel,
        val distance: String,
        val depth: String,
        val date: String,
        val tsunamiAlert: Boolean
)