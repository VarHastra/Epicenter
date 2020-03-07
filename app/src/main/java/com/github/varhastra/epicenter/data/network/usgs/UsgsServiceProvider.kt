package com.github.varhastra.epicenter.data.network.usgs

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.data.network.EventServiceResponse
import com.github.varhastra.epicenter.data.network.Network
import com.github.varhastra.epicenter.data.network.exceptions.BadResponseCodeException
import com.github.varhastra.epicenter.data.network.exceptions.BadResponseException
import com.github.varhastra.epicenter.data.network.exceptions.NoNetworkConnectionException
import com.github.varhastra.epicenter.data.network.exceptions.UnknownException
import com.github.varhastra.epicenter.data.network.usgs.model.UsgsResponse
import com.github.varhastra.epicenter.device.ConnectivityProvider
import timber.log.Timber

class UsgsServiceProvider(
        private val usgsService: UsgsService = Network.retrofit.create(UsgsService::class.java),
        private val connectivityProvider: ConnectivityProvider = ConnectivityProvider()
) : EventServiceProvider {

    private val isNetworkConnected get() = connectivityProvider.isNetworkConnected()


    override suspend fun getWeekFeed(): Either<EventServiceResponse, Throwable> {
        return try {
            fetchWeekFeed()
        } catch (t: Throwable) {
            Timber.w(t, "fetchWeekFeedCatching(): Unable to load data.")
            Either.Failure(UnknownException(cause = t))
        }
    }

    private suspend fun fetchWeekFeed(): Either<UsgsResponse, Throwable> {
        if (isNetworkConnected.not()) {
            return Either.failure(NoNetworkConnectionException())
        }

        val response = usgsService.getWeekFeedSuspending()
        if (response.isSuccessful.not()) {
            Timber.w("fetchWeekFeed(): Bad response code encountered: ${response.code()}.")
            return Either.Failure(BadResponseCodeException(response.code()))
        }

        val usgsResponse = response.body()
        if (usgsResponse == null) {
            Timber.w("fetchWeekFeed(): Response body is null.")
            return Either.Failure(BadResponseException())
        }

        Timber.i("fetchWeekFeed(): Fetched ${usgsResponse.features.size} events.")
        return Either.Success(usgsResponse)
    }
}