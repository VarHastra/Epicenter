@file:Suppress("RemoveExplicitTypeArguments")

package me.alex.pet.apps.epicenter.di

import androidx.room.Room
import com.squareup.moshi.Moshi
import me.alex.pet.apps.epicenter.BuildConfig
import me.alex.pet.apps.epicenter.data.*
import me.alex.pet.apps.epicenter.data.db.AppDb
import me.alex.pet.apps.epicenter.data.db.PlaceDao
import me.alex.pet.apps.epicenter.data.network.EventServiceProvider
import me.alex.pet.apps.epicenter.data.network.usgs.UsgsService
import me.alex.pet.apps.epicenter.data.network.usgs.UsgsServiceProvider
import me.alex.pet.apps.epicenter.domain.interactors.*
import me.alex.pet.apps.epicenter.domain.repos.*
import me.alex.pet.apps.epicenter.domain.state.FeedStateDataSource
import me.alex.pet.apps.epicenter.domain.state.MapStateDataSource
import me.alex.pet.apps.epicenter.presentation.common.navigation.AppRouter
import me.alex.pet.apps.epicenter.presentation.common.navigation.Router
import me.alex.pet.apps.epicenter.presentation.details.DetailsModel
import me.alex.pet.apps.epicenter.presentation.main.MainModel
import me.alex.pet.apps.epicenter.presentation.main.feed.FeedModel
import me.alex.pet.apps.epicenter.presentation.main.map.MapModel
import me.alex.pet.apps.epicenter.presentation.placeeditor.locationpicker.LocationPickerModel
import me.alex.pet.apps.epicenter.presentation.placeeditor.namepicker.NamePickerModel
import me.alex.pet.apps.epicenter.presentation.places.PlacesModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val presentationModule = module {
    single<Router> {
        AppRouter()
    }

    viewModel {
        MainModel()
    }

    viewModel {
        FeedModel(
                androidContext(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
        )
    }

    viewModel {
        MapModel(
                androidContext(),
                get(),
                get(),
                get()
        )
    }

    viewModel { (eventId: String) ->
        DetailsModel(
                androidContext(),
                eventId,
                get(),
                get()
        )
    }

    viewModel {
        PlacesModel(
                androidContext(),
                get(),
                get(),
                get(),
                get()
        )
    }

    viewModel { (placeId: Int?) ->
        LocationPickerModel(
                androidContext(),
                placeId,
                get(),
                get(),
                get(),
                get()
        )
    }

    viewModel { (lat: Double, lng: Double) ->
        NamePickerModel(
                lat,
                lng,
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