package com.github.varhastra.epicenter.data.network

import com.github.varhastra.epicenter.common.functionaltypes.Either

/**
 * Defines common interface for all earthquake data providers.
 */
interface EventServiceProvider {

    fun getWeekFeed(responseCallback: ResponseCallback)

    fun getDayFeed(responseCallback: ResponseCallback)

    suspend fun getWeekFeedSuspending(): Either<EventServiceResponse, Throwable>

    interface ResponseCallback {
        fun onResult(response: EventServiceResponse)

        fun onFailure(t: Throwable?)
    }
}