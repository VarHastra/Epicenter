package com.github.varhastra.epicenter.ui.placesmanager

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.Place

interface PlacesManagerContract {

    interface View : BaseView<Presenter> {
        fun showPlaces(places: List<Place>)

        fun isActive(): Boolean

        fun showEditor(placeId: Int?)

        fun showUndoDeleteOption()
    }

    interface Presenter : BasePresenter {
        fun loadPlaces()

        fun openEditor(placeId: Int?)

        fun saveOrder(places: List<Place>)

        fun tryDeletePlace(place: Place)

        fun deletePlace()

        fun undoDeletion()
    }
}