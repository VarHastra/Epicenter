package com.github.varhastra.epicenter.presentation.placeeditor

import android.os.Bundle
import com.github.varhastra.epicenter.domain.interactors.InsertPlaceInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlaceInteractor
import com.github.varhastra.epicenter.domain.interactors.UpdatePlaceInteractor
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.failures.Failure
import com.github.varhastra.epicenter.presentation.common.UnitsFormatter
import com.github.varhastra.epicenter.presentation.common.UnitsLocale
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PlaceEditorPresenter(
        private val view: PlaceEditorContract.View,
        private val loadPlace: LoadPlaceInteractor,
        private val insertPlace: InsertPlaceInteractor,
        private val updatePlace: UpdatePlaceInteractor,
        unitsLocale: UnitsLocale
) : PlaceEditorContract.Presenter {

    private var placeId: Int? = null

    private var areaCenter = Coordinates(0.0, 0.0)

    private var areaRadiusKm = MIN_RADIUS_KM

    private val areaRadiusMeters get() = areaRadiusKm * 1000

    private val areaBounds: LatLngBounds
        get() {
            val from = LatLng(areaCenter.latitude, areaCenter.longitude)
            val southwestDeg = 265.0
            val northeastDeg = 85.0
            val west = SphericalUtil.computeOffset(from, areaRadiusMeters, southwestDeg)
            val east = SphericalUtil.computeOffset(from, areaRadiusMeters, northeastDeg)

            return LatLngBounds(west, east)
        }

    private val unitsFormatter = UnitsFormatter(unitsLocale, 0)

    init {
        view.attachPresenter(this)
    }

    override fun initialize(placeId: Int?) {
        this.placeId = placeId

        if (placeId == null) {
            view.loadMap()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            loadPlace(placeId).fold(::handlePlace, ::handleFailure)
        }
    }

    private fun handlePlace(place: Place) {
        place.let {
            areaCenter = it.coordinates
            areaRadiusKm = it.radiusKm
        }
        view.loadMap()
    }

    private fun handleFailure(failure: Failure) {
        // TODO: notify the user about the error
        view.navigateBack()
    }

    override fun onRestoreState(state: Bundle) {
        state.let {
            placeId = it.getSerializable(STATE_PLACE_ID) as Int?
            areaCenter = it.getSerializable(STATE_AREA_CENTER) as Coordinates
            areaRadiusKm = it.getDouble(STATE_AREA_RADIUS, MIN_RADIUS_KM)
        }
        view.loadMap()
    }

    override fun start() {
        view.apply {
            renderArea(areaCenter.toLatLng(), areaRadiusMeters)
            showAreaRadiusText(unitsFormatter.getLocalizedDistanceString(areaRadiusKm))
            val radiusPercentage = convertAreaRadiusToPercentage(areaRadiusKm)
            showRadius(radiusPercentage.roundToInt())
        }
        adjustCameraToFitBounds(areaBounds, false)
    }

    override fun onChangeAreaCenter(latLng: LatLng) {
        areaCenter = latLng.toCoordinates()
        view.renderArea(latLng, areaRadiusMeters)
    }

    override fun onChangeAreaRadius(percentage: Int) {
        areaRadiusKm = convertPercentageToAreaRadius(percentage.toDouble())
        view.apply {
            renderArea(areaCenter.toLatLng(), areaRadiusMeters)
            showAreaRadiusText(unitsFormatter.getLocalizedDistanceString(areaRadiusKm.roundToInt()))
        }
    }

    override fun onStopChangingAreaRadius(mapBounds: LatLngBounds) {
        val areaBounds = areaBounds
        if (areaBounds.northeast !in mapBounds || areaBounds.southwest !in mapBounds) {
            adjustCameraToFitBounds(areaBounds, true)
        }
    }

    override fun onSaveState(outState: Bundle) {
        outState.run {
            putSerializable(STATE_PLACE_ID, placeId)
            putSerializable(STATE_AREA_CENTER, areaCenter)
            putDouble(STATE_AREA_RADIUS, areaRadiusKm)
        }
    }

    override fun openNamePicker() {
        view.showNamePicker(areaCenter.toLatLng())
    }

    override fun saveWithName(placeName: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val id = placeId
            if (id == null) {
                insertPlace(placeName, areaCenter, areaRadiusKm)
            } else {
                updatePlace(id, placeName, areaCenter, areaRadiusKm)
            }
        }
        view.navigateBack()
    }

    private fun adjustCameraToFitBounds(bounds: LatLngBounds, animate: Boolean) {
        view.adjustCameraToFitBounds(bounds, animate)
    }


    private fun Coordinates.toLatLng() = LatLng(latitude, longitude)

    private fun LatLng.toCoordinates() = Coordinates(latitude, longitude)


    companion object {
        private const val MIN_RADIUS_KM = 500.0
        private const val MAX_RADIUS_KM = 5000.0
        private const val RADIUS_DELTA_KM = MAX_RADIUS_KM - MIN_RADIUS_KM

        private const val STATE_PLACE_ID = "PLACE_ID"
        private const val STATE_AREA_CENTER = "AREA_CENTER"
        private const val STATE_AREA_RADIUS = "AREA_RADIUS"

        private fun convertAreaRadiusToPercentage(radiusKm: Double): Double {
            return (radiusKm - MIN_RADIUS_KM) / RADIUS_DELTA_KM * 100
        }

        private fun convertPercentageToAreaRadius(percentage: Double): Double {
            return percentage / 100.0 * RADIUS_DELTA_KM + MIN_RADIUS_KM
        }
    }
}