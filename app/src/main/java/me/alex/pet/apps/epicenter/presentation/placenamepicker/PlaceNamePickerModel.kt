package me.alex.pet.apps.epicenter.presentation.placenamepicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.domain.interactors.LoadLocationNameInteractor
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.presentation.common.Event

class PlaceNamePickerModel(
        lat: Double,
        lng: Double,
        private val loadLocationName: LoadLocationNameInteractor
) : ViewModel() {

    private val coordinates = Coordinates(lat, lng)

    val name: LiveData<String>
        get() = _name
    private val _name = MutableLiveData<String>()

    val transientErrorEvent: LiveData<Event<Int>>
        get() = _transientErrorEvent
    private val _transientErrorEvent = MutableLiveData<Event<Int>>()

    val navigateBackEvent: LiveData<Event<String>>
        get() = _navigateBackEvent
    private val _navigateBackEvent = MutableLiveData<Event<String>>()


    init {
        viewModelScope.launch {
            fetchSuggestedPlaceName()
        }
    }

    private suspend fun fetchSuggestedPlaceName() {
        loadLocationName(coordinates).fold(::handleLocationName, ::handleFailure)
    }

    private fun handleLocationName(locationName: String) {
        _name.value = locationName
    }

    private fun handleFailure(failure: Failure) {
        _name.value = ""
    }

    fun onChangeName(name: String) {
        if (_name.value == name) {
            return
        }
        _name.value = name
    }

    fun onSaveAndExit() {
        val name = _name.value!!
        if (name.isBlank()) {
            _transientErrorEvent.value = Event(R.string.place_name_picker_error_empty_name)
        } else {
            _navigateBackEvent.value = Event(name)
        }
    }
}