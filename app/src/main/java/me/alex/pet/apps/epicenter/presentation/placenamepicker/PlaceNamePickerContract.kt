package me.alex.pet.apps.epicenter.presentation.placenamepicker

import me.alex.pet.apps.epicenter.presentation.BasePresenter
import me.alex.pet.apps.epicenter.presentation.BaseView

interface PlaceNamePickerContract {

    interface View : BaseView<Presenter> {
        fun isActive(): Boolean

        fun showSuggestedName(suggestedName: String)

        fun showErrorEmptyName()

        fun navigateBackWithResult(placeName: String)
    }

    interface Presenter : BasePresenter {
        fun initialize(latitude: Double, longitude: Double)

        fun loadSuggestedName()

        fun setPlaceName(name: String)

        fun saveAndExit()
    }
}