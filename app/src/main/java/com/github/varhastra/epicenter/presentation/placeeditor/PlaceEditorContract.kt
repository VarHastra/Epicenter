package com.github.varhastra.epicenter.presentation.placeeditor

import android.os.Bundle
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView
import com.google.android.gms.maps.model.LatLngBounds

interface PlaceEditorContract {

    interface View : BaseView<Presenter> {
        fun loadMap()

        fun renderArea(center: Coordinates, radiusMeters: Double)

        fun showAreaRadiusText(radiusText: String)

        fun showRadius(percentage: Int)

        fun adjustCameraToFitBounds(bounds: LatLngBounds, animate: Boolean)

        fun showNamePicker(coordinates: Coordinates)

        fun navigateBack()
    }

    interface Presenter : BasePresenter {
        fun initialize(placeId: Int?)

        fun onChangeAreaCenter(coordinates: Coordinates)

        fun onChangeAreaRadius(percentage: Int)

        fun onStopChangingAreaRadius(mapBounds: LatLngBounds)

        fun openNamePicker()

        fun saveWithName(placeName: String)

        fun onSaveState(outState: Bundle)

        fun onRestoreState(state: Bundle)
    }
}