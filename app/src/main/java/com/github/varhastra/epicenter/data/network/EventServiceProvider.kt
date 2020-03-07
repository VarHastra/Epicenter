package com.github.varhastra.epicenter.data.network

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.failures.Failure

interface EventServiceProvider {

    suspend fun getWeekFeed(): Either<EventServiceResponse, Failure>
}