package com.github.varhastra.epicenter.ui.placenamepicker

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.ui.placeeditor.PlaceEditorContract

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