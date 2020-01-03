package com.github.varhastra.epicenter.data.network.usgs

import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.data.network.Network
import com.github.varhastra.epicenter.data.network.usgs.model.UsgsResponse
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UsgsServiceProvider(
        private val usgsService: UsgsService = Network.retrofit.create(UsgsService::class.java)
) : EventServiceProvider {

    val logger = AnkoLogger(this.javaClass)


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
}