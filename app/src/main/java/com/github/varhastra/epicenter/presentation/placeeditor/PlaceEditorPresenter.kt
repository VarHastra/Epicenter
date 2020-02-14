package com.github.varhastra.epicenter.presentation.placeeditor

import android.os.Bundle
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.repos.PlacesRepository
import com.github.varhastra.epicenter.domain.repos.RepositoryCallback
import com.github.varhastra.epicenter.domain.state.placeeditor.Area
import com.github.varhastra.epicenter.presentation.common.UnitsFormatter
import com.github.varhastra.epicenter.presentation.common.UnitsLocale
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import kotlin.math.roundToInt

class PlaceEditorPresenter(
        private val view: PlaceEditorContract.View,
        private val placesRepository: PlacesRepository,
        unitsLocale: UnitsLocale
) : PlaceEditorContract.Presenter {

    private var placeId: Int? = null

    private var areaCenter = Coordinates(0.0, 0.0)

    private var areaRadiusKm = Area.MIN_RADIUS_KM

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

    private var placeOrder = -10

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

        placesRepository.getPlace(object : RepositoryCallback<Place> {
            override fun onResult(result: Place) {
                areaCenter = result.coordinates
                areaRadiusKm = result.radiusKm!!
                placeOrder = result.order
                view.loadMap()
            }

            override fun onFailure(t: Throwable?) {
                // TODO: notify the user about the error
                view.navigateBack()
            }
        }, placeId = placeId)
    }

    override fun onRestoreState(state: Bundle) {
        state.let {
            placeId = it.getSerializable(STATE_PLACE_ID) as Int?
            areaCenter = it.getSerializable(STATE_AREA_CENTER) as Coordinates
            areaRadiusKm = it.getDouble(STATE_AREA_RADIUS, Area.MIN_RADIUS_KM)
            placeOrder = it.getInt(STATE_PLACE_ORDER, -10)
        }
        view.loadMap()
    }

    override fun start() {
        view.apply {
            setMaxRadiusValue((Area.MAX_RADIUS_KM - Area.MIN_RADIUS_KM).roundToInt() - 1)
            renderArea(areaCenter, areaRadiusMeters)
            showAreaRadiusText(unitsFormatter.getLocalizedDistanceString(areaRadiusKm))
            setRadius((areaRadiusKm - Area.MIN_RADIUS_KM - 1).roundToInt())
        }
        adjustCameraToFitBounds(areaBounds, false)
    }

    override fun onChangeAreaCenter(coordinates: Coordinates) {
        areaCenter = coordinates
        view.renderArea(areaCenter, areaRadiusMeters)
    }

    override fun onChangeAreaRadius(value: Int) {
        areaRadiusKm = value + Area.MIN_RADIUS_KM + 1
        view.apply {
            renderArea(areaCenter, areaRadiusMeters)
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
            putInt(STATE_PLACE_ORDER, placeOrder)
        }
    }

    override fun openNamePicker() {
        view.showNamePicker(areaCenter)
    }

    override fun saveWithName(placeName: String) {
        placesRepository.savePlace(Place(placeId
                ?: 0, placeName, areaCenter, areaRadiusKm, placeOrder))
        view.navigateBack()
    }

    private fun adjustCameraToFitBounds(bounds: LatLngBounds, animate: Boolean) {
        view.adjustCameraToFitBounds(bounds, animate)
    }


    companion object {
        private const val STATE_PLACE_ID = "PLACE_ID"
        private const val STATE_AREA_CENTER = "AREA_CENTER"
        private const val STATE_AREA_RADIUS = "AREA_RADIUS"
        private const val STATE_PLACE_ORDER = "PLACE_ORDER"
    }
}