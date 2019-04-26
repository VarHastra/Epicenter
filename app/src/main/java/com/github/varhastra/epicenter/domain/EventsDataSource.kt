package com.github.varhastra.epicenter.domain

import com.github.varhastra.epicenter.domain.model.Event
import org.threeten.bp.Instant

interface EventsDataSource {

    fun getWeekFeed(
        callback: DataSourceCallback<List<Event>>,
        forceLoad: Boolean = false
    )

    fun getWeekFeedLastUpdated(): Instant

    fun isCacheAvailable(): Boolean
}