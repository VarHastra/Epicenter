package com.github.varhastra.epicenter.presentation.main.feed

import com.github.varhastra.epicenter.data.AppSettings
import com.github.varhastra.epicenter.data.FeedState
import com.github.varhastra.epicenter.data.network.exceptions.NoNetworkConnectionException
import com.github.varhastra.epicenter.domain.interactors.InteractorCallback
import com.github.varhastra.epicenter.domain.interactors.LoadFeedInteractor
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.model.filters.AndFilter
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeFilter
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.filters.PlaceFilter
import com.github.varhastra.epicenter.domain.model.sorting.SortCriterion
import com.github.varhastra.epicenter.domain.model.sorting.SortOrder
import com.github.varhastra.epicenter.domain.model.sorting.SortStrategy
import com.github.varhastra.epicenter.domain.repos.*
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.warn

class FeedPresenter(
        private val view: FeedContract.View,
        private val eventsRepository: EventsRepository,
        private val placesRepository: PlacesRepository,
        private val locationRepository: LocationRepository,
        private val unitsLocaleRepository: UnitsLocaleRepository = AppSettings,
        private val feedStateDataSource: FeedStateDataSource = FeedState
) : FeedContract.Presenter {

    private val logger = AnkoLogger(this.javaClass)

    private val feedLoaderInteractor = LoadFeedInteractor(eventsRepository, locationRepository)

    private lateinit var filter: FeedFilter

    private lateinit var sortCriterion: SortCriterion

    private lateinit var sortOrder: SortOrder

    private lateinit var minMagnitude: MagnitudeLevel

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

        sortCriterion = feedStateDataSource.sortCriterion
        sortOrder = feedStateDataSource.sortOrder
        minMagnitude = feedStateDataSource.minMagnitude

        view.showCurrentSortCriterion(sortCriterion)
        view.showCurrentSortOrder(sortOrder)
        view.showCurrentMagnitudeFilter(minMagnitude)

        placeId = feedStateDataSource.selectedPlaceId

        loadPlaces()
        loadEvents()
    }

    /**
     * Loads a list of places and passes it to the view.
     */
    override fun loadPlaces() {
        placesRepository.getPlaces(object : RepositoryCallback<List<Place>> {
            override fun onResult(result: List<Place>) {
                if (!view.isActive()) {
                    return
                }

                view.showPlaces(result, unitsLocaleRepository.preferredUnits)
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
        placesRepository.getPlace(object : RepositoryCallback<Place> {
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

    private fun getEvents(place: Place, forceLoad: Boolean) {
        val filter = AndFilter(PlaceFilter(place), MagnitudeFilter(minMagnitude))
        val sorting = SortStrategy(sortCriterion, sortOrder)
        val params = LoadFeedInteractor.RequestValues(forceLoad, filter, sorting)

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
                            view.showEvents(result, unitsLocaleRepository.preferredUnits)
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

                        if (t is NoNetworkConnectionException) {
                            if (forceLoad) {
                                view.showErrorNoConnection()
                            } else {
                                view.showErrorNoData(FeedContract.View.ErrorReason.ERR_NO_CONNECTION)
                            }
                        } else {
                            view.showErrorNoData(FeedContract.View.ErrorReason.ERR_UNKNOWN)
                        }
                    }
                })
    }

    override fun setPlaceAndReload(place: Place) {
        placeId = place.id
        feedStateDataSource.selectedPlaceId = placeId
        loadEvents()
    }

    override fun setSortCriterion(sortCriterion: SortCriterion) {
        this.sortCriterion = sortCriterion
        feedStateDataSource.sortCriterion = sortCriterion
        loadEvents()
    }

    override fun setSortOrder(sortOrder: SortOrder) {
        this.sortOrder = sortOrder
        feedStateDataSource.sortOrder = sortOrder
        loadEvents()
    }

    override fun setMinMagnitude(magnitudeLevel: MagnitudeLevel) {
        this.minMagnitude = magnitudeLevel
        feedStateDataSource.minMagnitude = magnitudeLevel
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
}