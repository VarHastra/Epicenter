package com.github.varhastra.epicenter.domain.repos

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.failures.Failure

interface EventsRepository {

    suspend fun getWeekFeedSuspending(forceLoad: Boolean = false): Either<List<Event>, Failure>

    suspend fun getEventSuspending(eventId: String): Either<Event, Failure>
}