package me.alex.pet.apps.epicenter.presentation.places

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.domain.interactors.DeletePlaceInteractor
import me.alex.pet.apps.epicenter.domain.interactors.LoadPlacesInteractor
import me.alex.pet.apps.epicenter.domain.interactors.UpdatePlacesOrderInteractor
import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.repos.UnitsLocaleRepository
import me.alex.pet.apps.epicenter.presentation.Destinations
import me.alex.pet.apps.epicenter.presentation.common.events.Event
import me.alex.pet.apps.epicenter.presentation.common.events.NavigationEvent
import me.alex.pet.apps.epicenter.presentation.common.navigation.NavigationCommand

class PlacesModel(
        private val context: Context,
        private val loadPlaces: LoadPlacesInteractor,
        private val deletePlace: DeletePlaceInteractor,
        private val updatePlacesOrder: UpdatePlacesOrderInteractor,
        private val unitsLocaleRepository: UnitsLocaleRepository
) : ViewModel() {

    val places: LiveData<List<PlaceViewBlock>>
        get() = _places
    private val _places = MutableLiveData<List<PlaceViewBlock>>()

    val deletionAttemptEvent: LiveData<Event<Int>>
        get() = _deletionAttemptEvent
    private val _deletionAttemptEvent = MutableLiveData<Event<Int>>()

    val navigationEvent: LiveData<NavigationEvent>
        get() = _navigationEvent
    private val _navigationEvent = MutableLiveData<NavigationEvent>()

    private val deletedPlaceIds = mutableSetOf<Int>()

    private var placesMightHaveChanged = false

    init {
        viewModelScope.launch { fetchPlaces() }
    }

    fun onStart() {
        if (placesMightHaveChanged) {
            placesMightHaveChanged = false
            viewModelScope.launch { fetchPlaces() }
        }
    }

    private suspend fun fetchPlaces() {
        loadPlaces().map { places -> mapPlacesToViews(places) }
                .fold(::handlePlaces, ::handleFailure)
    }

    private suspend fun mapPlacesToViews(places: List<Place>) = withContext(Dispatchers.Default) {
        val mapper = Mapper(context, unitsLocaleRepository.preferredUnits)
        places.map { mapper.map(it) }
    }

    private fun handlePlaces(places: List<PlaceViewBlock>) {
        _places.value = places
    }

    private fun handleFailure(failure: Failure) {
        // TODO: consider treating the result of LoadPlacesInteractor as always successful
    }

    fun onEditPlace(placeId: Int) {
        if (placeId != Place.WORLD.id && placeId != Place.CURRENT_LOCATION.id) {
            _navigationEvent.value = NavigationEvent(NavigationCommand.To(Destinations.PlaceEditor(placeId)))
        }
        placesMightHaveChanged = true
    }

    fun onAddNewPlace() {
        _navigationEvent.value = NavigationEvent(NavigationCommand.To(Destinations.PlaceEditor(null)))
        placesMightHaveChanged = true
    }

    fun onSaveOrder(places: List<PlaceViewBlock>) {
        CoroutineScope(Dispatchers.Main).launch {
            updatePlacesOrder(places.map { it.id })
        }
    }

    fun onAttemptToDeletePlace(placeId: Int) {
        deletedPlaceIds.add(placeId)
        _deletionAttemptEvent.value = Event(deletedPlaceIds.size)
    }

    fun onDeletePlaces() {
        viewModelScope.launch {
            deletedPlaceIds.forEach { deletePlace(it) }
        }
    }

    fun onUndoDeletion() {
        deletedPlaceIds.clear()
        viewModelScope.launch { fetchPlaces() }
    }
}