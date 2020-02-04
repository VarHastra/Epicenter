package com.github.varhastra.epicenter.presentation.placesmanager

import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView

interface PlacesManagerContract {

    interface View : BaseView<Presenter> {
        fun showPlaces(places: List<Place>)

        fun isActive(): Boolean

        fun showEditor(placeId: Int?)

        fun showUndoDeleteOption()
    }

    interface Presenter : BasePresenter {
        fun fetchPlaces()

        fun openEditor(placeId: Int?)

        fun saveOrder(places: List<Place>)

        fun tryDeletePlace(place: Place)

        fun deletePlace()

        fun undoDeletion()
    }
}