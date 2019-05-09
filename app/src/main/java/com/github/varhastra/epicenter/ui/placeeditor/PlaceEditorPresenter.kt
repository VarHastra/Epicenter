package com.github.varhastra.epicenter.ui.placeeditor

import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.PlacesDataSource
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.state.placeeditor.Area
import com.github.varhastra.epicenter.domain.state.placeeditor.PlaceEditorState
import com.github.varhastra.epicenter.utils.UnitsFormatter
import com.github.varhastra.epicenter.utils.UnitsLocale
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import kotlin.math.roundToInt

class PlaceEditorPresenter(
        private val view: PlaceEditorContract.View,
        private val placesDataSource: PlacesDataSource,
        unitsLocale: UnitsLocale
) : PlaceEditorContract.Presenter {

    override var state: PlaceEditorState = PlaceEditorState(0, null)
    private val unitsFormatter = UnitsFormatter(unitsLocale, 0)

    init {
        view.attachPresenter(this)
    }

    override fun initialize(placeId: Int) {
        state = state.copy(placeId = placeId)
        if (placeId == Place.CURRENT_LOCATION.id) {
            view.allowNameEditor(false)
        }
    }

    override fun initialize(placeEditorState: PlaceEditorState) {
        state = placeEditorState
        if (state.placeId == Place.CURRENT_LOCATION.id) {
            view.allowNameEditor(false)
        }
    }

    override fun start() {
        view.setMaxRadiusValue((Area.MAX_RADIUS_KM - Area.MIN_RADIUS_KM).roundToInt() - 1)

        if (state.placeId == 0) {
            drawCurrentState()
            return
        }

        if (state.placeId == Place.CURRENT_LOCATION.id) {
            view.showRequestLocationPermission(onGranted = {
                getAndDrawPlace()
            }, onDenied = {
                view.navigateBack()
            })

        } else {
            getAndDrawPlace()
        }

    }

    private fun getAndDrawPlace() {
        placesDataSource.getPlace(object : DataSourceCallback<Place> {
            override fun onResult(result: Place) {
                state = state.copy(area = Area(result.coordinates, result.radiusKm!!))
                drawCurrentState()
                adjustCameraToAreaBounds()
            }

            override fun onFailure(t: Throwable?) {
                drawCurrentState()
            }
        }, placeId = state.placeId)
    }

    private fun drawCurrentState() {
        state.area?.apply {
            view.drawAreaCenter(center, state.placeId != Place.CURRENT_LOCATION.id)
            view.drawArea(center, radiusM)
            view.showRadiusControls(true)
            view.showAreaRadiusText(unitsFormatter.getLocalizedDistanceString(radiusKm))
            view.setRadius((radiusKm - Area.MIN_RADIUS_KM - 1).roundToInt())
        }
    }

    override fun createArea(coordinates: Coordinates) {
        if (state.area != null) {
            return
        }

        state = state.copy(area = Area(coordinates, Area.MIN_RADIUS_KM))
        state.area?.apply {
            view.drawAreaCenter(center)
            view.drawArea(center, radiusM)
            view.showRadiusControls(true)
            view.showAreaRadiusText(unitsFormatter.getLocalizedDistanceString(radiusKm))
            view.setRadius((radiusKm - Area.MIN_RADIUS_KM - 1).roundToInt())
            adjustCameraToAreaBounds()
        }
    }

    private fun adjustCameraToAreaBounds() {
        state.area?.apply {
            val bounds = computeBounds(center, radiusM)
            view.adjustCameraToFitBounds(bounds.first, bounds.second)

        }
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
        state = state.copy(area = state.area?.copy(center = coordinates))
    }

    override fun setAreaRadius(value: Int, lastUpdate: Boolean) {
        val newRadiusKm = value + Area.MIN_RADIUS_KM + 1
        state = state.copy(area = state.area?.copy(radiusKm = newRadiusKm))
        state.area?.apply {
            view.updateAreaRadius(radiusM)
            view.showAreaRadiusText(unitsFormatter.getLocalizedDistanceString(radiusKm.roundToInt()))
        }

        if (lastUpdate) {
            adjustCameraToAreaBounds()
        }
    }

    override fun openNamePicker() {
        state.area?.apply {
            view.showNamePicker(center)
        }
    }

    override fun saveWithName(placeName: String) {
        state.area?.apply {
            placesDataSource.savePlace(Place(state.placeId, placeName, center, radiusKm))
        }
        view.navigateBack()
    }
}