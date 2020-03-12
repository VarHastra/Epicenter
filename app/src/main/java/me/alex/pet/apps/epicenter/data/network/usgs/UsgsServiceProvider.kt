package me.alex.pet.apps.epicenter.data.network.usgs

import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.data.network.EventServiceProvider
import me.alex.pet.apps.epicenter.data.network.EventServiceResponse
import me.alex.pet.apps.epicenter.data.network.usgs.model.UsgsResponse
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.model.failures.Failure.NetworkFailure.*
import me.alex.pet.apps.epicenter.domain.repos.ConnectivityRepository
import timber.log.Timber

class UsgsServiceProvider(
        private val usgsService: UsgsService,
        private val connectivityRepository: ConnectivityRepository
) : EventServiceProvider {

    private val isNetworkConnected get() = connectivityRepository.isNetworkConnected()


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