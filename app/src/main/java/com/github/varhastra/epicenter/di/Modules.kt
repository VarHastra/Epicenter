@file:Suppress("RemoveExplicitTypeArguments")

package com.github.varhastra.epicenter.di

import androidx.room.Room
import com.github.varhastra.epicenter.BuildConfig
import com.github.varhastra.epicenter.data.*
import com.github.varhastra.epicenter.data.db.AppDb
import com.github.varhastra.epicenter.data.db.PlaceDao
import com.github.varhastra.epicenter.data.network.EventServiceProvider
import com.github.varhastra.epicenter.data.network.usgs.UsgsService
import com.github.varhastra.epicenter.data.network.usgs.UsgsServiceProvider
import com.github.varhastra.epicenter.domain.interactors.*
import com.github.varhastra.epicenter.domain.repos.*
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import com.github.varhastra.epicenter.domain.state.MapStateDataSource
import com.github.varhastra.epicenter.presentation.details.DetailsContract
import com.github.varhastra.epicenter.presentation.details.DetailsPresenter
import com.github.varhastra.epicenter.presentation.main.feed.FeedContract
import com.github.varhastra.epicenter.presentation.main.feed.FeedPresenter
import com.github.varhastra.epicenter.presentation.main.map.MapContract
import com.github.varhastra.epicenter.presentation.main.map.MapPresenter
import com.github.varhastra.epicenter.presentation.placeeditor.PlaceEditorContract
import com.github.varhastra.epicenter.presentation.placeeditor.PlaceEditorPresenter
import com.github.varhastra.epicenter.presentation.placenamepicker.PlaceNamePickerContract
import com.github.varhastra.epicenter.presentation.placenamepicker.PlaceNamePickerPresenter
import com.github.varhastra.epicenter.presentation.places.PlacesContract
import com.github.varhastra.epicenter.presentation.places.PlacesPresenter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val presentationModule = module {
    factory<FeedPresenter> { (view: FeedContract.View) ->
        FeedPresenter(
                androidContext(),
                view,
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
        )
    }

    factory<MapPresenter> { (view: MapContract.View) ->
        MapPresenter(
                androidContext(),
                view,
                get(),
                get()
        )
    }

    factory<DetailsPresenter> { (view: DetailsContract.View) ->
        DetailsPresenter(
                androidContext(),
                view,
                get(),
                get()
        )
    }

    factory<PlacesPresenter> { (view: PlacesContract.View) ->
        PlacesPresenter(
                androidContext(),
                view,
                get(),
                get(),
                get(),
                get()
        )
    }

    factory<PlaceEditorPresenter> { (view: PlaceEditorContract.View) ->
        PlaceEditorPresenter(
                androidContext(),
                view,
                get(),
                get(),
                get(),
                get()
        )
    }

    factory<PlaceNamePickerPresenter> { (view: PlaceNamePickerContract.View) ->
        PlaceNamePickerPresenter(
                view,
                get()
        )
    }
}

val domainModule = module {
    factory { DeletePlaceInteractor(get()) }

    factory { InsertPlaceInteractor(get()) }

    factory { LoadEventInteractor(get(), get()) }

    factory { LoadFeedInteractor(get(), get()) }

    factory { LoadLocationNameInteractor(get()) }

    factory { LoadMapEventsInteractor(get(), get()) }

    factory { LoadPlaceInteractor(get()) }

    factory { LoadPlacesInteractor(get()) }

    factory { LoadPlaceNamesInteractor(get()) }

    factory { LoadSelectedPlaceNameInteractor(get(), get()) }

    factory { UpdatePlaceInteractor(get()) }

    factory { UpdatePlacesOrderInteractor(get()) }
}

val dataModule = module {
    single<PlacesRepository> { PlacesDataSource(get(), get(), androidContext()) }

    single<LocationRepository> { LocationProvider(androidContext()) }

    single<ConnectivityRepository> { ConnectivityProvider(androidContext()) }

    single<EventServiceProvider> { UsgsServiceProvider(get(), get()) }

    single<EventsRepository> { EventsDataSource(get()) }

    single<UnitsLocaleRepository> { AppSettings }

    single<FeedStateDataSource> { FeedState }

    single<MapStateDataSource> { MapState }
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