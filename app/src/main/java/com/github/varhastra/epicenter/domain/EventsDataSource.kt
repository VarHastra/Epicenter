package com.github.varhastra.epicenter.domain

import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place
import org.threeten.bp.Instant

interface EventsDataSource {

    fun getWeekFeed(
        callback: DataSourceCallback<List<Event>>,
        filter: FeedFilter = FeedFilter(),
        place: Place = Place.WORLD,
        forceLoad: Boolean = false
    )

    fun getWeekFeedLastUpdated(): Instant

//    fun getDayFeed(callback: DataSourceCallback<List<Event>>, filter: FeedFilter = FeedFilter(), place: Place = Place.WORLD)
}