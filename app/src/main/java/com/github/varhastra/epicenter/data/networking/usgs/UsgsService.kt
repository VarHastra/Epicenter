package com.github.varhastra.epicenter.data.networking.usgs

import com.github.varhastra.epicenter.data.networking.usgs.model.UsgsResponse
import retrofit2.Call
import retrofit2.http.GET

interface UsgsService {

    @GET("earthquakes/feed/v1.0/summary/all_month.geojson")
    fun getMonthFeed(): Call<UsgsResponse>

    @GET("earthquakes/feed/v1.0/summary/all_week.geojson")
    fun getWeekFeed(): Call<UsgsResponse>

    @GET("earthquakes/feed/v1.0/summary/all_day.geojson")
    fun getDayFeed(): Call<UsgsResponse>

    @GET("earthquakes/feed/v1.0/summary/all_hour.geojson")
    fun getHourFeed(): Call<UsgsResponse>
}