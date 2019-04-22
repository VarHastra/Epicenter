package com.github.varhastra.epicenter.data.networking.usgs

import android.content.Context
import android.os.Handler
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.data.networking.EventServiceProvider
import com.github.varhastra.epicenter.data.networking.Network
import com.github.varhastra.epicenter.data.networking.usgs.model.UsgsResponse
import com.google.gson.Gson
import java.util.concurrent.Executors

class UsgsMockProvider(val context: Context = App.instance, val gson: Gson = Network.gson) : EventServiceProvider {
    override fun getWeekFeed(responseCallback: EventServiceProvider.ResponseCallback) {
        val handler = Handler()
        Executors.newSingleThreadExecutor().execute {
            val reader = context.assets.open("mock_response.json").bufferedReader()
            val response = gson.fromJson<UsgsResponse>(reader, UsgsResponse::class.java)
            handler.post {
                responseCallback.onResult(response)
            }
        }

    }

    override fun getDayFeed(responseCallback: EventServiceProvider.ResponseCallback) {
        TODO("stub, not implemented")
    }
}