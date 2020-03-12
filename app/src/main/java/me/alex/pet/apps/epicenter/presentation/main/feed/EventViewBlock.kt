package me.alex.pet.apps.epicenter.presentation.main.feed

import me.alex.pet.apps.epicenter.presentation.common.AlertLevel

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