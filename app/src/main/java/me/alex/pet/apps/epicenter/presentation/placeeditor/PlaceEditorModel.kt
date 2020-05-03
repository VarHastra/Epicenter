package me.alex.pet.apps.epicenter.presentation.placeeditor

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.launch
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.domain.interactors.InsertPlaceInteractor
import me.alex.pet.apps.epicenter.domain.interactors.LoadLocationNameInteractor
import me.alex.pet.apps.epicenter.domain.interactors.LoadPlaceInteractor
import me.alex.pet.apps.epicenter.domain.interactors.UpdatePlaceInteractor
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.model.kmToM
import me.alex.pet.apps.epicenter.domain.repos.UnitsLocaleRepository
import me.alex.pet.apps.epicenter.presentation.common.Event
import me.alex.pet.apps.epicenter.presentation.common.NavigationEvent
import me.alex.pet.apps.epicenter.presentation.common.UnitsFormatter
import me.alex.pet.apps.epicenter.presentation.common.navigation.NavigationCommand
import me.alex.pet.apps.epicenter.presentation.placeeditor.navigation.PlaceEditorDestinations
import kotlin.math.roundToInt

class PlaceEditorModel(
        context: Context,
        private val placeId: Int?,
        private val loadPlace: LoadPlaceInteractor,
        private val insertPlace: InsertPlaceInteractor,
        private val updatePlace: UpdatePlaceInteractor,
        private val loadLocationName: LoadLocationNameInteractor,
        unitsLocaleRepository: UnitsLocaleRepository
) : ViewModel() {

    private val areaCenter = MutableLiveData<Coordinates>()

    val areaCenterLatLng = areaCenter.map { it.toLatLng() }

    private val areaRadiusKm = MutableLiveData<Double>()

    val areaRadiusMeters: LiveData<Double> = areaRadiusKm.map { kmToM(it) }

    val areaRadiusText: LiveData<String> = areaRadiusKm.map { unitsFormatter.getLocalizedDistanceString(it) }

    val areaRadiusPercentage: LiveData<Int> = areaRadiusKm.map { convertAreaRadiusToPercentage(it).roundToInt() }

    val name: LiveData<String>
        get() = _name
    private val _name = MutableLiveData<String>()

    private val unitsLocale = unitsLocaleRepository.preferredUnits

    val adjustCameraEvent: LiveData<AdjustCameraEvent>
        get() = _adjustCameraEvent
    private val _adjustCameraEvent = MutableLiveData<AdjustCameraEvent>()

    val navigationEvent: LiveData<NavigationEvent>
        get() = _navigationEvent
    private val _navigationEvent = MutableLiveData<NavigationEvent>()

    val transientErrorEvent: LiveData<Event<Int>>
        get() = _transientErrorEvent
    private val _transientErrorEvent = MutableLiveData<Event<Int>>()

    private var stateHasBeenRestored = false


    private val areaBounds: LatLngBounds
        get() {
            val from = LatLng(areaCenter.value!!.latitude, areaCenter.value!!.longitude)
            val southwestDeg = 265.0
            val northeastDeg = 85.0
            val radiusM = kmToM(areaRadiusKm.value!!)
            val west = SphericalUtil.computeOffset(from, radiusM, southwestDeg)
            val east = SphericalUtil.computeOffset(from, radiusM, northeastDeg)

            return LatLngBounds(west, east)
        }

    private val unitsFormatter = UnitsFormatter(context, unitsLocale, 0)

    init {
        if (placeId == null) {
            areaCenter.value = DEFAULT_COORDINATES
            areaRadiusKm.value = MIN_RADIUS_KM
            _name.value = ""
            _adjustCameraEvent.value = AdjustCameraEvent(Pair(areaBounds, false))
        } else {
            viewModelScope.launch {
                loadPlace(placeId).fold(::handlePlace, ::handleFailure)
            }
        }
    }

    private fun handlePlace(place: Place) {
        if (!stateHasBeenRestored) {
            _name.value = place.name
            areaCenter.value = place.coordinates
            areaRadiusKm.value = place.radiusKm
            _adjustCameraEvent.value = AdjustCameraEvent(Pair(areaBounds, false))
        }
    }

    private fun handleFailure(failure: Failure) {
        // TODO: notify the user about the error
        _navigationEvent.value = NavigationEvent(NavigationCommand.FinishFlow)
    }

    fun onRestoreState(state: Bundle?) {
        if (state == null) {
            return
        }
        state.let {
            stateHasBeenRestored = true
            areaCenter.value = it.getSerializable(STATE_AREA_CENTER) as Coordinates
            areaRadiusKm.value = it.getDouble(STATE_AREA_RADIUS, MIN_RADIUS_KM)
            _name.value = it.getString(STATE_LOCATION_NAME)
        }
        _adjustCameraEvent.value = AdjustCameraEvent(Pair(areaBounds, false))
    }

    fun onChangeAreaCenter(latLng: LatLng) {
        val coordinates = latLng.toCoordinates()
        if (areaCenter.value == coordinates) {
            return
        }
        areaCenter.value = coordinates
    }

    fun onChangeAreaRadius(percentage: Int) {
        val radiusKm = convertPercentageToAreaRadius(percentage.toDouble())
        if (areaRadiusKm.value == radiusKm) {
            return
        }
        areaRadiusKm.value = radiusKm
    }

    fun onStopChangingAreaRadius(mapBounds: LatLngBounds) {
        val areaBounds = areaBounds
        if (areaBounds.northeast !in mapBounds || areaBounds.southwest !in mapBounds) {
            _adjustCameraEvent.value = AdjustCameraEvent(Pair(areaBounds, true))
        }
    }

    fun onChangeName(name: String) {
        if (_name.value == name) {
            return
        }
        _name.value = name
    }

    fun onSaveState(outState: Bundle) {
        outState.run {
            putSerializable(STATE_AREA_CENTER, areaCenter.value!!)
            putDouble(STATE_AREA_RADIUS, areaRadiusKm.value!!)
            putString(STATE_LOCATION_NAME, _name.value!!)
        }
    }

    fun onOpenNamePicker() {
        val namePicker = PlaceEditorDestinations.NamePicker()
        _navigationEvent.value = NavigationEvent(NavigationCommand.To(namePicker))
        if (placeId == null && _name.value.isNullOrEmpty()) {
            viewModelScope.launch { fetchSuggestedPlaceName() }
        }
    }

    fun onSaveAndExit() {
        val name = name.value
        if (name.isNullOrBlank()) {
            _transientErrorEvent.value = Event(R.string.place_name_picker_error_empty_name)
            return
        }

        if (placeId == null) {
            viewModelScope.launch { insertPlace(name, areaCenter.value!!, areaRadiusKm.value!!) }
        } else {
            viewModelScope.launch { updatePlace(placeId, name, areaCenter.value!!, areaRadiusKm.value!!) }
        }
        _navigationEvent.value = NavigationEvent(NavigationCommand.FinishFlow)
    }


    private suspend fun fetchSuggestedPlaceName() {
        loadLocationName(areaCenter.value!!).fold(
                { name -> _name.value = name },
                { _name.value = "" }
        )
    }


    private fun Coordinates.toLatLng() = LatLng(latitude, longitude)

    private fun LatLng.toCoordinates() = Coordinates(latitude, longitude)
}


private const val MIN_RADIUS_KM = 500.0
private const val MAX_RADIUS_KM = 5000.0
private const val RADIUS_DELTA_KM = MAX_RADIUS_KM - MIN_RADIUS_KM
private val DEFAULT_COORDINATES = Coordinates(0.0, 0.0)

private const val STATE_AREA_CENTER = "AREA_CENTER"
private const val STATE_AREA_RADIUS = "AREA_RADIUS"
private const val STATE_LOCATION_NAME = "LOCATION_NAME"

private fun convertAreaRadiusToPercentage(radiusKm: Double): Double {
    return (radiusKm - MIN_RADIUS_KM) / RADIUS_DELTA_KM * 100
}

private fun convertPercentageToAreaRadius(percentage: Double): Double {
    return percentage / 100.0 * RADIUS_DELTA_KM + MIN_RADIUS_KM
}

typealias AdjustCameraEvent = Event<Pair<LatLngBounds, Boolean>>