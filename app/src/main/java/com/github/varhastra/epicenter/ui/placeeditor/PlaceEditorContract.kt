package com.github.varhastra.epicenter.ui.placeeditor

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.state.placeeditor.PlaceEditorState

interface PlaceEditorContract {

    interface View : BaseView<Presenter> {
        fun showRadiusControls(show: Boolean)

        fun drawAreaCenter(coordinates: Coordinates)

        fun drawArea(coordinates: Coordinates, radiusMeters: Double)

        fun updateAreaRadius(radiusMeters: Double)

        fun showAreaRadiusText(radiusText: String)

        fun setRadius(value: Int)

        fun setMaxRadiusValue(maxRadius: Int)

        fun adjustCameraToFitBounds(left: Coordinates, right: Coordinates)
    }

    interface Presenter : BasePresenter {
        var state: PlaceEditorState

        fun initialize(presenterMode: PresenterMode, placeId: Int)

        fun initialize(placeEditorState: PlaceEditorState)

        fun createArea(coordinates: Coordinates)

        fun setAreaCenter(coordinates: Coordinates)

        fun setAreaRadius(value: Int, lastUpdate: Boolean)
    }

    enum class PresenterMode {
        ADD,
        EDIT
    }
}