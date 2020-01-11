package com.github.varhastra.epicenter.data.network.usgs

import android.content.Context
import android.os.Handler
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.data.network.EventServiceResponse
import com.github.varhastra.epicenter.data.network.Network
import com.github.varhastra.epicenter.data.network.usgs.model.UsgsResponse
import com.squareup.moshi.Moshi
import okio.Okio
import java.util.concurrent.Executors

class UsgsMockProvider(val context: Context = App.instance, val moshi: Moshi = Network.moshi) : EventServiceProvider {
    override fun getWeekFeed(responseCallback: EventServiceProvider.ResponseCallback) {
        val handler = Handler()
        Executors.newSingleThreadExecutor().execute {
            val inputStream = context.assets.open("mock_response.json")
            val reader = Okio.buffer(Okio.source(inputStream))
            val adapter = moshi.adapter<UsgsResponse>(UsgsResponse::class.java)
            val response = adapter.fromJson(reader)

            handler.post {
                response?.let { responseCallback.onResult(response) }
            }
        }

    }

    override fun getDayFeed(responseCallback: EventServiceProvider.ResponseCallback) {
        TODO("stub, not implemented")
    }

    override suspend fun getWeekFeedSuspending(): Either<EventServiceResponse, Throwable> {
        TODO("stub, not implemented")
    }
}