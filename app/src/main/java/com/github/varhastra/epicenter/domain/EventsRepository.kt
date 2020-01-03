package com.github.varhastra.epicenter.domain

import com.github.varhastra.epicenter.domain.model.Event
import org.threeten.bp.Instant

interface EventsRepository {

    fun getWeekFeed(
            callback: DataSourceCallback<List<Event>>,
            forceLoad: Boolean = false
    )

    fun getEvent(eventId: String, callback: DataSourceCallback<Event>)

    fun getWeekFeedLastUpdated(): Instant

    fun isCacheAvailable(): Boolean
}