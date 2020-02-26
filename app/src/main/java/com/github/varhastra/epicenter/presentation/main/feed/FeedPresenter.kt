package com.github.varhastra.epicenter.presentation.main.feed

import android.content.Context
import com.github.varhastra.epicenter.common.functionaltypes.flatMap
import com.github.varhastra.epicenter.data.AppSettings
import com.github.varhastra.epicenter.data.FeedState
import com.github.varhastra.epicenter.data.network.exceptions.NoNetworkConnectionException
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.interactors.LoadFeedInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlaceInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlacesInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadSelectedPlaceNameInteractor
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.PlaceName
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.model.filters.AndFilter
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeFilter
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.filters.PlaceFilter
import com.github.varhastra.epicenter.domain.model.sorting.SortCriterion
import com.github.varhastra.epicenter.domain.model.sorting.SortOrder
import com.github.varhastra.epicenter.domain.model.sorting.SortStrategy
import com.github.varhastra.epicenter.domain.repos.UnitsLocaleRepository
import com.github.varhastra.epicenter.domain.state.FeedStateDataSource
import com.github.varhastra.epicenter.presentation.main.feed.Error.PersistentError
import com.github.varhastra.epicenter.presentation.main.feed.Error.TransientError
import com.github.varhastra.epicenter.presentation.main.feed.mappers.EventMapper
import com.github.varhastra.epicenter.presentation.main.feed.mappers.PlaceMapper
import com.google.android.gms.common.api.ResolvableApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

class FeedPresenter(
        private val context: Context,
        private val view: FeedContract.View,
        private val loadSelectedPlaceNameInteractor: LoadSelectedPlaceNameInteractor,
        private val loadFeedInteractor: LoadFeedInteractor,
        private val loadPlacesInteractor: LoadPlacesInteractor,
        private val loadPlaceInteractor: LoadPlaceInteractor,
        private val unitsLocaleRepository: UnitsLocaleRepository = AppSettings,
        private val feedStateDataSource: FeedStateDataSource = FeedState
) : FeedContract.Presenter {

    private val logger = AnkoLogger(this.javaClass)

    private var events = listOf<EventViewBlock>()

    private val sortStrategy
        get() = SortStrategy(sortCriterion, sortOrder)

    private lateinit var sortCriterion: SortCriterion

    private lateinit var sortOrder: SortOrder

    private val magnitudeFilter
        get() = MagnitudeFilter(minMagnitude)

    private lateinit var minMagnitude: MagnitudeLevel

    private lateinit var selectedPlace: PlaceName

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

        sortCriterion = feedStateDataSource.sortCriterion
        sortOrder = feedStateDataSource.sortOrder
        minMagnitude = feedStateDataSource.minMagnitude

        view.showCurrentSortCriterion(sortCriterion)
        view.showCurrentSortOrder(sortOrder)
        view.showCurrentMagnitudeFilter(minMagnitude)

        CoroutineScope(Dispatchers.Main).launch {
            fetchPlaces()
            fetchSelectedPlace()
            fetchEvents(false)
        }
    }

    private suspend fun fetchPlaces() {
        loadPlacesInteractor()
                .map { places -> mapPlacesToViews(places) }
                .fold(::handlePlaces, ::handlePlacesFailure)
    }

    private suspend fun fetchSelectedPlace() {
        selectedPlace = loadSelectedPlaceNameInteractor()
        view.showSelectedPlace(selectedPlace.id)
        view.showSelectedPlaceName(selectedPlace.name)
    }

    private suspend fun mapPlacesToViews(places: List<Place>): List<PlaceViewBlock> {
        return withContext(Dispatchers.Default) {
            val mapper = PlaceMapper(context, unitsLocaleRepository.preferredUnits)
            places.map { mapper.map(it) }
        }
    }

    private fun handlePlaces(places: List<PlaceViewBlock>) {
        if (!view.isActive()) {
            return
        }

        view.showPlaces(places)
    }

    private fun handlePlacesFailure(t: Throwable?) {
        logger.error("Error loading places $t")
    }

    override fun loadEvents() {
        CoroutineScope(Dispatchers.Main).launch { fetchEvents(false) }
    }

    override fun refreshEvents() {
        CoroutineScope(Dispatchers.Main).launch { fetchEvents(true) }
    }

    private suspend fun fetchEvents(forceLoad: Boolean) {
        view.showProgress(true)
        loadPlaceInteractor(selectedPlace.id)
                .map { place -> AndFilter(PlaceFilter(place), magnitudeFilter) }
                .flatMap { filter -> loadFeedInteractor(forceLoad, filter, sortStrategy) }
                .map { events -> mapEventsToViews(events) }
                .fold(
                        { eventViews -> handleEvents(eventViews) },
                        { failure -> handleFailure(failure, forceLoad) }
                )
    }

    private suspend fun handleEvents(newEvents: List<EventViewBlock>) {
        if (!view.isActive()) {
            return
        }
        view.showProgress(false)

        if (newEvents.isEmpty()) {
            view.showError(PersistentError.NoEvents)
            this.events = newEvents
            return
        }

        if (!areEventListsEqual(newEvents, this.events)) {
            view.showEvents(newEvents)
            this.events = newEvents
        }
    }

    private fun handleFailure(t: Throwable, forceLoad: Boolean) {
        if (!view.isActive()) {
            return
        }
        this.events = emptyList()

        view.showProgress(false)
        when (t) {
            is ResolvableApiException -> view.showError(PersistentError.LocationIsOff(t))
            is LocationProvider.PermissionDeniedException -> view.showError(PersistentError.NoLocationPermission)
            is NoNetworkConnectionException -> {
                if (forceLoad) {
                    view.showError(TransientError.NoConnection)
                } else {
                    view.showError(PersistentError.NoConnection)
                }
            }
            else -> view.showError(PersistentError.Unknown)
        }
    }

    private suspend fun areEventListsEqual(first: List<EventViewBlock>, second: List<EventViewBlock>): Boolean {
        return withContext(Dispatchers.Default) {
            first == second
        }
    }

    private suspend fun mapEventsToViews(events: List<RemoteEvent>): List<EventViewBlock> {
        val mapper = EventMapper(context, unitsLocaleRepository.preferredUnits)
        return withContext(Dispatchers.Default) {
            events.map { mapper.map(it) }
        }
    }

    override fun setPlaceAndReload(place: Place) {
        setPlaceAndReload(place.id)
    }

    override fun setPlaceAndReload(placeId: Int) {
        if (placeId == selectedPlace.id) {
            return
        }
        feedStateDataSource.selectedPlaceId = placeId
        CoroutineScope(Dispatchers.Main).launch {
            fetchSelectedPlace()
            fetchEvents(false)
        }
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

    override fun onResolveError(error: Error) {
        if (error is PersistentError) {
            resolveError(error)
        }
    }

    private fun resolveError(error: PersistentError) {
        when (error) {
            is PersistentError.LocationIsOff -> view.renderLocationSettingsPrompt(error.resolvableException)
            is PersistentError.NoLocationPermission -> view.renderLocationPermissionRequest()
            else -> loadEvents()
        }
    }

    override fun ignoreUpcomingStartCall() {
        ignoreUpcomingStartCall = true
    }
}