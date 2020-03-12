package me.alex.pet.apps.epicenter.data.network

import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.domain.model.failures.Failure

interface EventServiceProvider {

    suspend fun getWeekFeed(): Either<EventServiceResponse, Failure>
}