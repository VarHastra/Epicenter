package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Event

interface EventsRepository {

    suspend fun getWeekFeedSuspending(forceLoad: Boolean = false): Either<List<Event>, Throwable>

    suspend fun getEventSuspending(eventId: String): Either<Event, Throwable>
}