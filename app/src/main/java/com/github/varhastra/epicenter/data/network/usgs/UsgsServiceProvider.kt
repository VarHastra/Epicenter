package com.github.varhastra.epicenter.data.network.usgs

import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.data.network.EventServiceResponse
import com.github.varhastra.epicenter.data.network.Network
import com.github.varhastra.epicenter.data.network.usgs.model.UsgsResponse
import com.github.varhastra.epicenter.device.ConnectivityProvider
import com.github.varhastra.epicenter.domain.model.failures.Failure
import com.github.varhastra.epicenter.domain.model.failures.Failure.NetworkFailure.*
import timber.log.Timber

class UsgsServiceProvider(
        private val usgsService: UsgsService = Network.retrofit.create(UsgsService::class.java),
        private val connectivityProvider: ConnectivityProvider = ConnectivityProvider()
) : EventServiceProvider {

    private val isNetworkConnected get() = connectivityProvider.isNetworkConnected()


    override suspend fun getWeekFeed(): Either<EventServiceResponse, Failure> {
        return try {
            fetchWeekFeed()
        } catch (t: Throwable) {
            Timber.w(t, "fetchWeekFeedCatching(): Unable to load data.")
            Either.Failure(Unknown)
        }
    }

    private suspend fun fetchWeekFeed(): Either<UsgsResponse, Failure> {
        if (isNetworkConnected.not()) {
            return Either.failure(NoConnection)
        }

        val response = usgsService.getWeekFeedSuspending()
        if (response.isSuccessful.not()) {
            Timber.w("fetchWeekFeed(): Bad response code encountered: ${response.code()}.")
            return Either.Failure(BadResponse)
        }

        val usgsResponse = response.body()
        if (usgsResponse == null) {
            Timber.w("fetchWeekFeed(): Response body is null.")
            return Either.Failure(BadResponse)
        }

        Timber.i("fetchWeekFeed(): Fetched ${usgsResponse.features.size} events.")
        return Either.Success(usgsResponse)
    }
}