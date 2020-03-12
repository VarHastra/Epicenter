package me.alex.pet.apps.epicenter.domain.repos

import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.domain.model.Event
import me.alex.pet.apps.epicenter.domain.model.failures.Failure

interface EventsRepository {

    suspend fun getWeekFeedSuspending(forceLoad: Boolean = false): Either<List<Event>, Failure>

    suspend fun getEventSuspending(eventId: String): Either<Event, Failure>
}