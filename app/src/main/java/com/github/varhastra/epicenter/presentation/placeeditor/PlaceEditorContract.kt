package com.github.varhastra.epicenter.presentation.placeeditor

import android.os.Bundle
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView

interface PlaceEditorContract {

    interface View : BaseView<Presenter> {
        fun loadMap()

        fun renderArea(center: Coordinates, radiusMeters: Double)

        fun showAreaRadiusText(radiusText: String)

        fun setRadius(value: Int)

        fun setMaxRadiusValue(maxRadius: Int)

        fun adjustCameraToFitBounds(left: Coordinates, right: Coordinates)

        fun showNamePicker(coordinates: Coordinates)

        fun navigateBack()
    }

    interface Presenter : BasePresenter {
        fun initialize(placeId: Int?)

        fun onChangeAreaCenter(coordinates: Coordinates)

        fun onChangeAreaRadius(value: Int, lastUpdate: Boolean)

        fun openNamePicker()

        fun saveWithName(placeName: String)

        fun onSaveState(outState: Bundle)

        fun onRestoreState(state: Bundle)
    }
}