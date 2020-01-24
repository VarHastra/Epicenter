package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Event

interface EventsRepository {

    fun getWeekFeed(
            callback: RepositoryCallback<List<Event>>,
            forceLoad: Boolean = false
    )

    suspend fun getWeekFeedSuspending(forceLoad: Boolean = false): Either<List<Event>, Throwable>

    fun getEvent(eventId: String, callback: RepositoryCallback<Event>)

    suspend fun getEventSuspending(eventId: String): Either<Event, Throwable>
}