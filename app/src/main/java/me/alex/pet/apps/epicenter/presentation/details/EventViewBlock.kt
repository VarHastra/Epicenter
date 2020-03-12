package me.alex.pet.apps.epicenter.presentation.details

import me.alex.pet.apps.epicenter.presentation.common.AlertLevel

class EventViewBlock(
        val title: String,
        val magnitudeValue: String,
        val alertLevel: AlertLevel,
        val magnitudeType: String,
        val dateTime: String,
        val daysAgo: String,
        val coordinates: String,
        val distanceFromUser: String,
        val depth: String,
        val feltReports: String,
        val sourceLink: String,
        val tsunamiAlert: Boolean
)