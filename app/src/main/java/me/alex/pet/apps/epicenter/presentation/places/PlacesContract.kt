package me.alex.pet.apps.epicenter.presentation.places

import me.alex.pet.apps.epicenter.presentation.BasePresenter
import me.alex.pet.apps.epicenter.presentation.BaseView

interface PlacesContract {

    interface View : BaseView<Presenter> {
        fun showPlaces(places: List<PlaceViewBlock>)

        fun isActive(): Boolean

        fun showPlaceEditor(placeId: Int)

        fun showPlaceCreator()

        fun showUndoDeleteOption()
    }

    interface Presenter : BasePresenter {
        fun fetchPlaces()

        fun editPlace(placeId: Int)

        fun addPlace()

        fun saveOrder(places: List<PlaceViewBlock>)

        fun tryDeletePlace(placeId: Int)

        fun deletePlace()

        fun undoDeletion()
    }
}