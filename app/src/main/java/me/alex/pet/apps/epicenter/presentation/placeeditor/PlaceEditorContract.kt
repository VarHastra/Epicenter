package me.alex.pet.apps.epicenter.presentation.placeeditor

import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import me.alex.pet.apps.epicenter.presentation.BasePresenter
import me.alex.pet.apps.epicenter.presentation.BaseView

interface PlaceEditorContract {

    interface View : BaseView<Presenter> {
        fun loadMap()

        fun renderArea(center: LatLng, radiusMeters: Double)

        fun showAreaRadiusText(radiusText: String)

        fun showRadius(percentage: Int)

        fun adjustCameraToFitBounds(bounds: LatLngBounds, animate: Boolean)

        fun showNamePicker(latLng: LatLng)

        fun navigateBack()
    }

    interface Presenter : BasePresenter {
        fun initialize(placeId: Int?)

        fun onChangeAreaCenter(latLng: LatLng)

        fun onChangeAreaRadius(percentage: Int)

        fun onStopChangingAreaRadius(mapBounds: LatLngBounds)

        fun openNamePicker()

        fun saveWithName(placeName: String)

        fun onSaveState(outState: Bundle)

        fun onRestoreState(state: Bundle)
    }
}