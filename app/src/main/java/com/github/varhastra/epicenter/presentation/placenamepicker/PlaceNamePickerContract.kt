package com.github.varhastra.epicenter.presentation.placenamepicker

import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView

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