package com.github.varhastra.epicenter.ui.placeeditor

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.state.placeeditor.PlaceEditorState

interface PlaceEditorContract {

    interface View : BaseView<Presenter> {
        fun allowNameEditor(allow: Boolean)

        fun showRadiusControls(show: Boolean)

        fun showRequestLocationPermission(onGranted: () -> Unit, onDenied: () -> Unit)

        fun drawAreaCenter(coordinates: Coordinates, draggable: Boolean = true)

        fun drawArea(coordinates: Coordinates, radiusMeters: Double)

        fun showTooltip(show: Boolean)

        fun updateAreaRadius(radiusMeters: Double)

        fun showAreaRadiusText(radiusText: String)

        fun setRadius(value: Int)

        fun setMaxRadiusValue(maxRadius: Int)

        fun adjustCameraToFitBounds(left: Coordinates, right: Coordinates)

        fun showNamePicker(coordinates: Coordinates)

        fun navigateBack()
    }

    interface Presenter : BasePresenter {
        var state: PlaceEditorState

        fun initialize(placeId: Int)

        fun initialize(placeEditorState: PlaceEditorState)

        fun createArea(coordinates: Coordinates)

        fun setAreaCenter(coordinates: Coordinates)

        fun setAreaRadius(value: Int, lastUpdate: Boolean)

        fun openNamePicker()

        fun saveWithName(placeName: String)
    }
}