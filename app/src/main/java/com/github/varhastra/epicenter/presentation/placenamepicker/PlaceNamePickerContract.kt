package com.github.varhastra.epicenter.presentation.placenamepicker

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.Coordinates

interface PlaceNamePickerContract {

    interface View : BaseView<Presenter> {
        fun isActive(): Boolean

        fun showSuggestedName(suggestedName: String)

        fun showErrorEmptyName()

        fun navigateBackWithResult(placeName: String)
    }

    interface Presenter : BasePresenter {
        fun initialize(coordinates: Coordinates)

        fun loadSuggestedName()

        fun setPlaceName(name: String)

        fun saveAndExit()
    }
}