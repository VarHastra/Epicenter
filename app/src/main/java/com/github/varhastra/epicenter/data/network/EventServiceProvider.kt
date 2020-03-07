package com.github.varhastra.epicenter.data.network

import com.github.varhastra.epicenter.common.functionaltypes.Either

interface EventServiceProvider {

    suspend fun getWeekFeed(): Either<EventServiceResponse, Throwable>
}