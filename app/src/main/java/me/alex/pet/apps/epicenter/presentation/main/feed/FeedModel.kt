package me.alex.pet.apps.epicenter.presentation.main.feed

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.common.functionaltypes.Either.Companion.failure
import me.alex.pet.apps.epicenter.common.functionaltypes.Either.Companion.success
import me.alex.pet.apps.epicenter.common.functionaltypes.flatMap
import me.alex.pet.apps.epicenter.domain.interactors.LoadFeedInteractor
import me.alex.pet.apps.epicenter.domain.interactors.LoadPlaceInteractor
import me.alex.pet.apps.epicenter.domain.interactors.LoadPlaceNamesInteractor
import me.alex.pet.apps.epicenter.domain.interactors.LoadSelectedPlaceNameInteractor
import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.PlaceName
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.model.failures.Failure.LocationFailure
import me.alex.pet.apps.epicenter.domain.model.failures.Failure.NetworkFailure
import me.alex.pet.apps.epicenter.domain.model.filters.AndFilter
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeFilter
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel
import me.alex.pet.apps.epicenter.domain.model.filters.PlaceFilter
import me.alex.pet.apps.epicenter.domain.model.sorting.SortCriterion
import me.alex.pet.apps.epicenter.domain.model.sorting.SortOrder
import me.alex.pet.apps.epicenter.domain.model.sorting.SortStrategy
import me.alex.pet.apps.epicenter.domain.repos.UnitsLocaleRepository
import me.alex.pet.apps.epicenter.domain.state.FeedStateDataSource
import me.alex.pet.apps.epicenter.presentation.Destinations
import me.alex.pet.apps.epicenter.presentation.common.events.EmptyEvent
import me.alex.pet.apps.epicenter.presentation.common.events.Event
import me.alex.pet.apps.epicenter.presentation.common.events.NavigationEvent
import me.alex.pet.apps.epicenter.presentation.common.navigation.NavigationCommand
import me.alex.pet.apps.epicenter.presentation.main.feed.Error.PersistentError
import me.alex.pet.apps.epicenter.presentation.main.feed.Error.TransientError
import me.alex.pet.apps.epicenter.presentation.main.feed.mappers.EventMapper

class FeedModel(
        private val context: Context,
        private val loadSelectedPlaceNameInteractor: LoadSelectedPlaceNameInteractor,
        private val loadFeedInteractor: LoadFeedInteractor,
        private val loadPlaceNamesInteractor: LoadPlaceNamesInteractor,
        private val loadPlaceInteractor: LoadPlaceInteractor,
        private val unitsLocaleRepository: UnitsLocaleRepository,
        private val feedStateDataSource: FeedStateDataSource
) : ViewModel() {

    val eventsData: LiveData<EventsData>
        get() = _data
    private val _data = MutableLiveData<EventsData>()

    val places: LiveData<List<PlaceViewBlock>>
        get() = _places
    private val _places = MutableLiveData<List<PlaceViewBlock>>()

    val selectedPlace: LiveData<PlaceName>
        get() = _selectedPlace
    private val _selectedPlace = MutableLiveData<PlaceName>()

    val sortCriterion: LiveData<SortCriterion>
        get() = _sortCriterion
    private val _sortCriterion = MutableLiveData<SortCriterion>()

    val minMagnitude: LiveData<MagnitudeLevel>
        get() = _minMagnitude
    private val _minMagnitude = MutableLiveData<MagnitudeLevel>()

    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }

    val transientErrorEvent: LiveData<TransientErrorEvent>
        get() = _transientErrorEvent
    private val _transientErrorEvent = MutableLiveData<TransientErrorEvent>()

    val toggleFiltersEvent: LiveData<EmptyEvent>
        get() = _toggleFiltersEvent
    private val _toggleFiltersEvent = MutableLiveData<EmptyEvent>()

    val requestLocationPermissionEvent: LiveData<EmptyEvent>
        get() = _requestLocationPermissionEvent
    private val _requestLocationPermissionEvent = MutableLiveData<EmptyEvent>()

    val adjustLocationSettingsEvent: LiveData<AdjustLocationSettingsEvent>
        get() = _adjustLocationSettingsEvent
    private val _adjustLocationSettingsEvent = MutableLiveData<AdjustLocationSettingsEvent>()

    val navigationEvent: LiveData<NavigationEvent>
        get() = _navigationEvent
    private val _navigationEvent = MutableLiveData<NavigationEvent>()

    private var placesMightHaveChanged = false

    private var runningJob: Job? = null


    init {
        _sortCriterion.value = feedStateDataSource.sortCriterion
        _minMagnitude.value = feedStateDataSource.minMagnitude

        viewModelScope.launch {
            fetchPlaces()
            fetchSelectedPlace()
            fetchEvents(false)
        }
    }

    fun onStart() {
        if (placesMightHaveChanged) {
            placesMightHaveChanged = false
            viewModelScope.launch {
                fetchPlaces()
                fetchSelectedPlace()
                fetchEvents(false)
            }
        }
    }

    private suspend fun fetchPlaces() {
        _places.value = mapPlaceNamesToViews(loadPlaceNamesInteractor())
    }

    private suspend fun fetchSelectedPlace() {
        _selectedPlace.value = loadSelectedPlaceNameInteractor()
    }

    private suspend fun mapPlaceNamesToViews(places: List<PlaceName>) = withContext(Dispatchers.Default) {
        places.map { it.toView() }
    }

    fun onRefreshEvents() {
        viewModelScope.launch { fetchEvents(true) }
    }

    private suspend fun fetchEvents(forceLoad: Boolean) {
        _isLoading.value = true
        loadPlaceInteractor(_selectedPlace.value!!.id)
                .map { place -> AndFilter(PlaceFilter(place), MagnitudeFilter(_minMagnitude.value!!)) }
                .flatMap { filter -> loadFeedInteractor(forceLoad, filter, _sortCriterion.value!!.toSortStrategy()) }
                .map { events -> mapEventsToViews(events) }
                .fold(::handleEvents, ::handleFailure)
    }

    private fun handleEvents(newEvents: List<EventViewBlock>) {
        _isLoading.value = false

        if (newEvents.isEmpty()) {
            _data.value = failure(PersistentError.NoEvents)
            return
        }

        _data.value = success(newEvents)
    }

    private fun handleFailure(failure: Failure) {
        _isLoading.value = false
        when (failure) {
            is LocationFailure.ProviderFailure -> if (failure.t is ResolvableApiException) _data.value = failure(PersistentError.LocationIsOff(failure.t))
            is LocationFailure.PermissionDenied -> _data.value = failure(PersistentError.NoLocationPermission)
            is NetworkFailure.NoConnection -> {
                if (_data.value is Either.Success) {
                    _transientErrorEvent.value = TransientErrorEvent(TransientError.NoConnection)
                } else {
                    _data.value = failure(PersistentError.NoConnection)
                }
            }
            else -> _data.value = failure(PersistentError.Unknown)
        }
    }

    private suspend fun mapEventsToViews(events: List<RemoteEvent>): List<EventViewBlock> {
        val mapper = EventMapper(context, unitsLocaleRepository.preferredUnits)
        return withContext(Dispatchers.Default) {
            events.map { mapper.map(it) }
        }
    }

    fun onChangePlace(placeId: Int) {
        if (placeId == _selectedPlace.value!!.id) {
            return
        }
        feedStateDataSource.selectedPlaceId = placeId

        runningJob?.cancel()
        runningJob = viewModelScope.launch {
            fetchSelectedPlace()
            fetchEvents(false)
        }
    }

    fun onChangeSortCriterion(sortCriterion: SortCriterion) {
        if (sortCriterion == _sortCriterion.value!!) {
            return
        }
        feedStateDataSource.sortCriterion = sortCriterion
        _sortCriterion.value = sortCriterion

        runningJob?.cancel()
        runningJob = viewModelScope.launch { fetchEvents(false) }
    }

    fun onChangeMinMagnitude(magnitudeLevel: MagnitudeLevel) {
        if (magnitudeLevel == _minMagnitude.value!!) {
            return
        }
        feedStateDataSource.minMagnitude = magnitudeLevel
        _minMagnitude.value = magnitudeLevel

        runningJob?.cancel()
        runningJob = viewModelScope.launch { fetchEvents(false) }
    }

    fun onOpenPlaceEditor() {
        placesMightHaveChanged = true
        _navigationEvent.value = NavigationEvent(NavigationCommand.To(Destinations.Places()))
    }

    fun onOpenDetails(eventId: String) {
        _navigationEvent.value = NavigationEvent(NavigationCommand.To(Destinations.Details(eventId)))
    }

    fun onOpenSettings() {
        _navigationEvent.value = NavigationEvent(NavigationCommand.To(Destinations.Settings()))
    }

    fun onToggleFiltersVisibility() {
        _toggleFiltersEvent.value = EmptyEvent()
    }

    fun onResolveError(error: Error) {
        if (error is PersistentError) {
            resolveError(error)
        }
    }

    private fun resolveError(error: PersistentError) {
        when (error) {
            is PersistentError.LocationIsOff -> _adjustLocationSettingsEvent.value = AdjustLocationSettingsEvent(error.resolvableException)
            is PersistentError.NoLocationPermission -> _requestLocationPermissionEvent.value = EmptyEvent()
            else -> viewModelScope.launch { fetchEvents(false) }
        }
    }
}


private fun PlaceName.toView(): PlaceViewBlock {
    val titleText = this.name

    val iconResId = when (this.id) {
        Place.CURRENT_LOCATION.id -> R.drawable.ic_chip_place_near_me
        Place.WORLD.id -> R.drawable.ic_chip_place_world
        else -> null
    }

    return PlaceViewBlock(
            this.id,
            titleText,
            iconResId
    )
}

private fun SortCriterion.toSortStrategy() = when (this) {
    SortCriterion.DATE -> SortStrategy(this, SortOrder.DESCENDING)
    SortCriterion.MAGNITUDE -> SortStrategy(this, SortOrder.DESCENDING)
    SortCriterion.DISTANCE -> SortStrategy(this, SortOrder.ASCENDING)
}

typealias EventsData = Either<List<EventViewBlock>, PersistentError>

typealias TransientErrorEvent = Event<TransientError>

typealias OpenDetailsEvent = Event<String>

typealias AdjustLocationSettingsEvent = Event<ResolvableApiException>