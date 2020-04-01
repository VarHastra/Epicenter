package me.alex.pet.apps.epicenter.presentation.locationpicker

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.launch
import me.alex.pet.apps.epicenter.domain.interactors.InsertPlaceInteractor
import me.alex.pet.apps.epicenter.domain.interactors.LoadPlaceInteractor
import me.alex.pet.apps.epicenter.domain.interactors.UpdatePlaceInteractor
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.model.kmToM
import me.alex.pet.apps.epicenter.domain.repos.UnitsLocaleRepository
import me.alex.pet.apps.epicenter.presentation.common.EmptyEvent
import me.alex.pet.apps.epicenter.presentation.common.Event
import me.alex.pet.apps.epicenter.presentation.common.UnitsFormatter
import kotlin.math.roundToInt

class LocationPickerModel(
        context: Context,
        private val placeId: Int?,
        private val loadPlace: LoadPlaceInteractor,
        private val insertPlace: InsertPlaceInteractor,
        private val updatePlace: UpdatePlaceInteractor,
        unitsLocaleRepository: UnitsLocaleRepository
) : ViewModel() {

    private val areaCenter = MutableLiveData<Coordinates>()

    val areaCenterLatLng = areaCenter.map { it.toLatLng() }

    private val areaRadiusKm = MutableLiveData<Double>()

    val areaRadiusMeters: LiveData<Double> = areaRadiusKm.map { kmToM(it) }

    val areaRadiusText: LiveData<String> = areaRadiusKm.map { unitsFormatter.getLocalizedDistanceString(it) }

    val areaRadiusPercentage: LiveData<Int> = areaRadiusKm.map { convertAreaRadiusToPercentage(it).roundToInt() }

    private val unitsLocale = unitsLocaleRepository.preferredUnits

    val adjustCameraEvent: LiveData<AdjustCameraEvent>
        get() = _adjustCameraEvent
    private val _adjustCameraEvent = MutableLiveData<AdjustCameraEvent>()

    val openNamePickerEvent: LiveData<Event<LatLng>>
        get() = _openNamePickerEvent
    private val _openNamePickerEvent = MutableLiveData<Event<LatLng>>()

    val navigateBackEvent: LiveData<EmptyEvent>
        get() = _navigateBackEvent
    private val _navigateBackEvent = MutableLiveData<EmptyEvent>()


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
            _adjustCameraEvent.value = AdjustCameraEvent(Pair(areaBounds, false))
        } else {
            viewModelScope.launch {
                loadPlace(placeId).fold(::handlePlace, ::handleFailure)
            }
        }
    }

    private fun handlePlace(place: Place) {
        areaCenter.value = place.coordinates
        areaRadiusKm.value = place.radiusKm
        _adjustCameraEvent.value = AdjustCameraEvent(Pair(areaBounds, false))
    }

    private fun handleFailure(failure: Failure) {
        // TODO: notify the user about the error
        _navigateBackEvent.value = EmptyEvent()
    }

    fun onRestoreState(state: Bundle) {
        state.let {
            areaCenter.value = it.getSerializable(STATE_AREA_CENTER) as Coordinates
            areaRadiusKm.value = it.getDouble(STATE_AREA_RADIUS, MIN_RADIUS_KM)
        }
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

    fun onSaveState(outState: Bundle) {
        outState.run {
            putSerializable(STATE_AREA_CENTER, areaCenter.value!!)
            putDouble(STATE_AREA_RADIUS, areaRadiusKm.value!!)
        }
    }

    fun onOpenNamePicker() {
        _openNamePickerEvent.value = Event(areaCenter.value!!.toLatLng())
    }

    fun onSaveAndExit(placeName: String) {
        viewModelScope.launch {
            val id = placeId
            if (id == null) {
                insertPlace(placeName, areaCenter.value!!, areaRadiusKm.value!!)
            } else {
                updatePlace(id, placeName, areaCenter.value!!, areaRadiusKm.value!!)
            }
        }
        _navigateBackEvent.value = EmptyEvent()
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

private fun convertAreaRadiusToPercentage(radiusKm: Double): Double {
    return (radiusKm - MIN_RADIUS_KM) / RADIUS_DELTA_KM * 100
}

private fun convertPercentageToAreaRadius(percentage: Double): Double {
    return percentage / 100.0 * RADIUS_DELTA_KM + MIN_RADIUS_KM
}

typealias AdjustCameraEvent = Event<Pair<LatLngBounds, Boolean>>