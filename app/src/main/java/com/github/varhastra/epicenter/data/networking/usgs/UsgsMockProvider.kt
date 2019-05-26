package com.github.varhastra.epicenter.data.networking.usgs

import android.content.Context
import android.os.Handler
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.data.networking.EventServiceProvider
import com.github.varhastra.epicenter.data.networking.Network
import com.github.varhastra.epicenter.data.networking.usgs.model.UsgsResponse
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
}