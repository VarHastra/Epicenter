package com.github.varhastra.epicenter.presentation.main.feed

import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.*
import com.github.varhastra.epicenter.domain.interactors.FeedLoaderInteractor
import com.github.varhastra.epicenter.domain.interactors.InteractorCallback
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.warn
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

class FeedPresenter(
        private val view: FeedContract.View,
        private val eventsDataSource: EventsDataSource,
        private val placesDataSource: PlacesDataSource,
        private val locationRepository: LocationRepository,
        private val connectivityRepository: ConnectivityRepository,
        private val unitsLocaleRepository: UnitsLocaleRepository = Prefs,
        private val feedStateDataSource: FeedStateDataSource = Prefs
) : FeedContract.Presenter {

    private val logger = AnkoLogger(this.javaClass)

    private val feedLoaderInteractor = FeedLoaderInteractor(eventsDataSource, locationRepository)

    private lateinit var filter: FeedFilter
    private var placeId = Place.WORLD.id
    private var ignoreUpcomingStartCall = false


    init {
        view.attachPresenter(this)
    }

    override fun init() {
        // Do nothing
    }

    override fun start() {
        if (ignoreUpcomingStartCall) {
            ignoreUpcomingStartCall = false
            return
        }
        view.showTitle()

        filter = feedStateDataSource.getCurrentFilter()
        view.showCurrentFilter(filter)

        placeId = feedStateDataSource.getSelectedPlaceId()

        loadPlaces()
        loadEvents()
    }

    /**
     * Loads a list of places and passes it to the view.
     */
    override fun loadPlaces() {
        placesDataSource.getPlaces(object : DataSourceCallback<List<Place>> {
            override fun onResult(result: List<Place>) {
                if (!view.isActive()) {
                    return
                }

                view.showPlaces(result, unitsLocaleRepository.getPreferredUnitsLocale())
            }

            override fun onFailure(t: Throwable?) {
                // Unreachable callback
                logger.error("Error loading places $t")
            }
        })
    }

    override fun loadEvents() {
        startLoadingEvents(false)
    }

    override fun refreshEvents() {
        startLoadingEvents(true)
    }

    private fun startLoadingEvents(forceLoad: Boolean) {
        if (placeId == Place.CURRENT_LOCATION.id) {
            // If the user has selected "Current location", then we first need
            // to check location permission
            view.showLocationPermissionRequest(object : FeedContract.View.PermissionRequestCallback {
                override fun onGranted() {
                    // Permission is granted, proceed by loading information
                    getPlaceAndEvents(forceLoad)
                }

                override fun onDenied() {
                    // Permission denied, inform the user and switch to "World"
                    view.showErrorLocationNotAvailable()
                    setPlaceAndReload(Place.WORLD)
                }
            })
        } else {
            // If we deal with any place other than "Current location"
            // then just load information for that place
            getPlaceAndEvents(forceLoad)
        }
    }

    private fun getPlaceAndEvents(forceLoad: Boolean) {
        placesDataSource.getPlace(object : DataSourceCallback<Place> {
            override fun onResult(result: Place) {
                // Place is loaded, proceed by loading events
                view.showCurrentPlace(result)
                getEvents(result, forceLoad)
            }

            override fun onFailure(t: Throwable?) {
                // We might end up here only if we requested place representing current location
                // and for some reason current location is not available at the moment
                logger.warn("callback.onFailure(): $t")
                if (t !is NoSuchElementException) {
                    view.showErrorLocationNotAvailable()
                }
                setPlaceAndReload(Place.WORLD)
            }
        }, placeId)
    }

    private fun getEvents(place: Place, forceLoadRequested: Boolean) {
        val networkAvailable = connectivityRepository.isNetworkConnected()
        if (forceLoadRequested && eventsDataSource.isCacheAvailable() && !networkAvailable) {
            view.showErrorNoConnection()
        }

        val minsSinceUpd = ChronoUnit.MINUTES.between(eventsDataSource.getWeekFeedLastUpdated(), Instant.now())
        val forceLoad = (forceLoadRequested || (minsSinceUpd > FORCE_LOAD_RATE_MINS)) && networkAvailable

        val params = FeedLoaderInteractor.RequestValues(forceLoad, filter, place)

        view.showProgress(true)
        feedLoaderInteractor.execute(
                params,
                object : InteractorCallback<List<RemoteEvent>> {
                    override fun onResult(result: List<RemoteEvent>) {
                        if (!view.isActive()) {
                            return
                        }
                        view.showProgress(false)

                        if (result.isNotEmpty()) {
                            view.showEvents(result, unitsLocaleRepository.getPreferredUnitsLocale())
                        } else {
                            view.showErrorNoData(FeedContract.View.ErrorReason.ERR_NO_EVENTS)
                        }
                    }

                    override fun onFailure(t: Throwable?) {
                        if (!view.isActive()) {
                            return
                        }
                        logger.error("Error loading events: $t")
                        view.showProgress(false)

                        if (!connectivityRepository.isNetworkConnected()) {
                            view.showErrorNoData(FeedContract.View.ErrorReason.ERR_NO_CONNECTION)
                        } else {
                            view.showErrorNoData(FeedContract.View.ErrorReason.ERR_UNKNOWN)
                        }
                    }
                })
    }

    override fun setPlaceAndReload(place: Place) {
        placeId = place.id
        feedStateDataSource.saveSelectedPlaceId(placeId)
        loadEvents()
    }

    override fun setFilterAndReload(filter: FeedFilter) {
        this.filter = filter
        feedStateDataSource.saveCurrentFilter(filter)
        loadEvents()
    }

    override fun setMagnitudeFilterAndReload(minMag: Int) {
        filter = filter.copy(minMagnitude = minMag.toDouble())
        feedStateDataSource.saveCurrentFilter(filter)
        loadEvents()
    }

    override fun setSortingAndReload(sorting: FeedFilter.Sorting) {
        filter = filter.copy(sorting = sorting)
        feedStateDataSource.saveCurrentFilter(filter)
        loadEvents()
    }

    override fun openPlacesEditor() {
        view.showPlacesEditor()
    }

    override fun openEventDetails(eventId: String) {
        view.showEventDetails(eventId)
    }

    override fun ignoreUpcomingStartCall() {
        ignoreUpcomingStartCall = true
    }

    companion object {
        const val FORCE_LOAD_RATE_MINS = 10
    }
}