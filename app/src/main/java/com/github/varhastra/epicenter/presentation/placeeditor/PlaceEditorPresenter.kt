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
        view.setMaxRadiusValue((Area.MAX_RADIUS_KM - Area.MIN_RADIUS_KM).roundToInt() - 1)
        drawCurrentState()
        adjustCameraToAreaBounds()
    }

    private fun drawCurrentState() {
        view.drawAreaCenter(areaCenter)
        view.drawArea(areaCenter, areaRadiusMeters)
        view.showAreaRadiusText(unitsFormatter.getLocalizedDistanceString(areaRadiusKm))
        view.setRadius((areaRadiusKm - Area.MIN_RADIUS_KM - 1).roundToInt())
    }

    private fun adjustCameraToAreaBounds() {
        val bounds = computeBounds(areaCenter, areaRadiusMeters)
        view.adjustCameraToFitBounds(bounds.first, bounds.second)
    }

    private fun computeBounds(coordinates: Coordinates, radiusMeters: Double): Pair<Coordinates, Coordinates> {
        val from = LatLng(coordinates.latitude, coordinates.longitude)
        val rightmost = SphericalUtil.computeOffset(from, radiusMeters, 90.0)
        val leftmost = SphericalUtil.computeOffset(from, radiusMeters, 270.0)

        return Coordinates(leftmost.latitude, leftmost.longitude) to Coordinates(
                rightmost.latitude,
                rightmost.longitude
        )
    }

    override fun setAreaCenter(coordinates: Coordinates) {
        areaCenter = coordinates
    }

    override fun setAreaRadius(value: Int, lastUpdate: Boolean) {
        areaRadiusKm = value + Area.MIN_RADIUS_KM + 1
        view.updateAreaRadius(areaRadiusMeters)
        view.showAreaRadiusText(unitsFormatter.getLocalizedDistanceString(areaRadiusKm.roundToInt()))

        if (lastUpdate) {
            adjustCameraToAreaBounds()
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

    companion object {
        private const val STATE_PLACE_ID = "PLACE_ID"
        private const val STATE_AREA_CENTER = "AREA_CENTER"
        private const val STATE_AREA_RADIUS = "AREA_RADIUS"
        private const val STATE_PLACE_ORDER = "PLACE_ORDER"
    }
}