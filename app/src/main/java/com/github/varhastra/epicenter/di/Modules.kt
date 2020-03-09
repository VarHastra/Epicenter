@file:Suppress("RemoveExplicitTypeArguments")

package com.github.varhastra.epicenter.di

import androidx.room.Room
import com.github.varhastra.epicenter.BuildConfig
import com.github.varhastra.epicenter.data.ConnectivityProvider
import com.github.varhastra.epicenter.data.EventsDataSource
import com.github.varhastra.epicenter.data.LocationProvider
import com.github.varhastra.epicenter.data.PlacesDataSource
import com.github.varhastra.epicenter.data.db.AppDb
import com.github.varhastra.epicenter.data.db.PlaceDao
import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.data.network.usgs.UsgsService
import com.github.varhastra.epicenter.data.network.usgs.UsgsServiceProvider
import com.github.varhastra.epicenter.domain.repos.ConnectivityRepository
import com.github.varhastra.epicenter.domain.repos.EventsRepository
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val dataModule = module {
    single<PlacesRepository> { PlacesDataSource(get(), get(), androidContext()) }

    single<LocationRepository> { LocationProvider(androidContext()) }

    single<ConnectivityRepository> { ConnectivityProvider(androidContext()) }

    single<EventServiceProvider> { UsgsServiceProvider(get(), get()) }

    single<EventsRepository> { EventsDataSource(get()) }
}

val networkModule = module {
    single<OkHttpClient> {
        OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build()
    }

    single<Moshi> {
        Moshi.Builder().build()
    }

    single<Retrofit> {
        Retrofit.Builder()
                .client(get<OkHttpClient>())
                .baseUrl(BuildConfig.USGS_BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(get<Moshi>()))
                .build()
    }

    single<UsgsService> {
        get<Retrofit>().create(UsgsService::class.java)
    }
}

val dbModule = module {
    single<AppDb> {
        Room.databaseBuilder(
                androidContext(),
                AppDb::class.java,
                DB_NAME
        ).allowMainThreadQueries().build()
    }

    single<PlaceDao> { get<AppDb>().getPlaceDao() }
}

private const val DB_NAME = "epicenter.db"

private const val CONNECTION_TIMEOUT_MS = 15000L