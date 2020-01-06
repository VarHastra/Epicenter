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
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class UsgsServiceProvider(
        private val usgsService: UsgsService = Network.retrofit.create(UsgsService::class.java),
        private val connectivityProvider: ConnectivityProvider = ConnectivityProvider()
) : EventServiceProvider {

    val logger = AnkoLogger(this.javaClass)

    private val isNetworkConnected get() = connectivityProvider.isNetworkConnected()


    override fun getWeekFeed(responseCallback: EventServiceProvider.ResponseCallback) {
        usgsService.getWeekFeed().enqueue(object : Callback<UsgsResponse> {
            override fun onFailure(call: Call<UsgsResponse>, t: Throwable) {
                logger.error("getWeekFeed() failed loading data: $t")
                responseCallback.onFailure(t)
            }

            override fun onResponse(call: Call<UsgsResponse>, response: Response<UsgsResponse>) {
                if (response.isSuccessful) {
                    val usgsResponse = response.body()
                    if (usgsResponse == null) {
                        logger.error("getWeekFeed() null response body")
                        // TODO: pass custom exception
                        responseCallback.onFailure(null)
                        return
                    }

                    logger.info("getWeekFeed() response.size: ${usgsResponse.features.size}")
                    responseCallback.onResult(usgsResponse)
                } else {
                    logger.error("getWeekFeed() bad response code: ${response.code()}")
                    // TODO: pass custom exception
                    responseCallback.onFailure(null)
                }
            }
        })
    }

    override fun getDayFeed(responseCallback: EventServiceProvider.ResponseCallback) {
        usgsService.getDayFeed().enqueue(object : Callback<UsgsResponse> {
            override fun onFailure(call: Call<UsgsResponse>, t: Throwable) {
                logger.error("getDayFeed() failed loading data: $t")
                responseCallback.onFailure(t)
            }

            override fun onResponse(call: Call<UsgsResponse>, response: Response<UsgsResponse>) {
                if (response.isSuccessful) {
                    val usgsResponse = response.body()
                    if (usgsResponse == null) {
                        logger.error("getDayFeed() null response body")
                        // TODO: pass custom exception
                        responseCallback.onFailure(null)
                        return
                    }

                    logger.info("getDayFeed() response.size: ${usgsResponse.features.size}")
                    responseCallback.onResult(usgsResponse)
                } else {
                    logger.error("getDayFeed() bad response code: ${response.code()}")
                    // TODO: pass custom exception
                    responseCallback.onFailure(null)
                }
            }
        })
    }

    override suspend fun getWeekFeedSuspending(): Either<Throwable, EventServiceResponse> {
        return if (isNetworkConnected.not()) {
            Either.Failure(NoNetworkConnectionException())
        } else {
            fetchWeekFeedCatching()
        }
    }

    private suspend fun fetchWeekFeedCatching(): Either<Throwable, EventServiceResponse> {
        return try {
            fetchWeekFeed()
        } catch (t: Throwable) {
            Timber.w(t, "fetchWeekFeedCatching(): Unable to load data.")
            Either.Failure(UnknownException(cause = t))
        }
    }

    private suspend fun fetchWeekFeed(): Either<Throwable, UsgsResponse> {
        val response = usgsService.getWeekFeedSuspending()
        return if (response.isSuccessful.not()) {
            Timber.w("fetchWeekFeed(): Bad response code encountered: ${response.code()}.")
            Either.Failure(BadResponseCodeException(response.code()))
        } else {
            val usgsResponse = response.body()
            return if (usgsResponse == null) {
                Timber.w("fetchWeekFeed(): Response body is null.")
                Either.Failure(BadResponseException())
            } else {
                Timber.i("fetchWeekFeed(): Fetched ${usgsResponse.features.size} events.")
                Either.Success(usgsResponse)
            }
        }
    }
}