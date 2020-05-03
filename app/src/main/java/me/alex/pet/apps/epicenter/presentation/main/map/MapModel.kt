package me.alex.pet.apps.epicenter.presentation.main.map

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.domain.interactors.LoadMapEventsInteractor
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.model.filters.AndFilter
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeFilter
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel
import me.alex.pet.apps.epicenter.domain.model.filters.RecencyFilter
import me.alex.pet.apps.epicenter.domain.state.CameraState
import me.alex.pet.apps.epicenter.domain.state.MapStateDataSource
import me.alex.pet.apps.epicenter.presentation.Destinations
import me.alex.pet.apps.epicenter.presentation.common.*
import me.alex.pet.apps.epicenter.presentation.common.navigation.NavigationCommand

class MapModel(
        private val context: Context,
        private val mapStateDataSource: MapStateDataSource,
        private val loadEventsInteractor: LoadMapEventsInteractor
) : ViewModel() {

    val eventMarkers: LiveData<List<EventMarker>>
        get() = _eventMarkers
    private val _eventMarkers = MutableLiveData<List<EventMarker>>()

    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }

    val minMagnitude: LiveData<MagnitudeLevel>
        get() = _minMagnitude
    private val _minMagnitude = MutableLiveData<MagnitudeLevel>()

    val numberOfDaysToShow: LiveData<Int>
        get() = _numberOfDaysToShow
    private val _numberOfDaysToShow = MutableLiveData<Int>()

    val toggleFiltersEvent: LiveData<EmptyEvent>
        get() = _toggleFiltersEvent
    private val _toggleFiltersEvent = MutableLiveData<EmptyEvent>()

    val updateCameraPositionEvent: LiveData<Event<CameraState>>
        get() = _updateCameraPositionEvent
    private val _updateCameraPositionEvent = MutableLiveData<Event<CameraState>>()

    val zoomInEvent: LiveData<ZoomInEvent>
        get() = _zoomInEvent
    private val _zoomInEvent = MutableLiveData<ZoomInEvent>()

    val navigationEvent: LiveData<NavigationEvent>
        get() = _navigationEvent
    private val _navigationEvent = MutableLiveData<NavigationEvent>()

    private var runningJob: Job? = null

    private val filter
        get() = AndFilter(MagnitudeFilter(minMagnitude.value!!), RecencyFilter(numberOfDaysToShow.value!!))


    init {
        _minMagnitude.value = mapStateDataSource.minMagnitude
        _numberOfDaysToShow.value = mapStateDataSource.numberOfDaysToShow
        _updateCameraPositionEvent.value = Event(mapStateDataSource.cameraState)

        viewModelScope.launch { fetchEvents(false) }
    }

    private suspend fun fetchEvents(forceLoad: Boolean) {
        _isLoading.value = true
        loadEventsInteractor(forceLoad, filter).map { events -> mapEventsToMarkers(events) }
                .fold(::handleResult, ::handleFailure)
    }

    private suspend fun mapEventsToMarkers(events: List<RemoteEvent>) = withContext(Dispatchers.Default) {
        val mapper = Mapper(context)
        events.map { mapper.map(it) }
    }

    private fun handleResult(markers: List<EventMarker>) {
        _isLoading.value = false
        _eventMarkers.value = markers
    }

    private fun handleFailure(failure: Failure) {
        // TODO: report failures to the user
        _isLoading.value = false
    }

    fun onRefreshEvents() {
        runningJob?.cancel()
        runningJob = viewModelScope.launch { fetchEvents(true) }
    }

    fun onToggleFiltersVisibility() {
        _toggleFiltersEvent.value = EmptyEvent()
    }

    fun onChangeMinMagnitude(magnitudeLevel: MagnitudeLevel) {
        mapStateDataSource.minMagnitude = magnitudeLevel
        _minMagnitude.value = magnitudeLevel
        runningJob?.cancel()
        runningJob = viewModelScope.launch { fetchEvents(false) }
    }

    fun onChangeNumberOfDaysToShow(days: Int) {
        mapStateDataSource.numberOfDaysToShow = days
        _numberOfDaysToShow.value = days
        runningJob?.cancel()
        runningJob = viewModelScope.launch { fetchEvents(false) }
    }

    fun onRememberCameraPosition(coordinates: Coordinates, zoom: Float) {
        mapStateDataSource.cameraState = CameraState(zoom, coordinates)
    }

    fun onOpenDetails(eventId: String) {
        _navigationEvent.value = NavigationEvent(NavigationCommand.To(Destinations.Details(eventId)))
    }

    fun onOpenSettings() {
        _navigationEvent.value = NavigationEvent(NavigationCommand.To(Destinations.Settings()))
    }

    fun onZoomIn(latitude: Double, longitude: Double) {
        _zoomInEvent.value = ZoomInEvent(Coordinates(latitude, longitude))
    }
}

typealias OpenDetailsEvent = Event<String>

typealias ZoomInEvent = Event<Coordinates>