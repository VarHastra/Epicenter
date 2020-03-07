package com.github.varhastra.epicenter.presentation.placenamepicker

import com.github.varhastra.epicenter.domain.interactors.LoadLocationNameInteractor
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.failures.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaceNamePickerPresenter(
        private val view: PlaceNamePickerContract.View,
        private val loadLocationName: LoadLocationNameInteractor
) : PlaceNamePickerContract.Presenter {

    private lateinit var coordinates: Coordinates

    private var placeName = ""

    init {
        view.attachPresenter(this)
    }

    override fun start() {
        loadSuggestedName()
    }

    override fun initialize(latitude: Double, longitude: Double) {
        this.coordinates = Coordinates(latitude, longitude)
    }

    override fun loadSuggestedName() {
        CoroutineScope(Dispatchers.Main).launch {
            loadLocationName(coordinates).fold(::handleLocationName, ::handleFailure)
        }
    }

    private fun handleLocationName(locationName: String) {
        if (!view.isActive()) {
            return
        }

        view.showSuggestedName(locationName)
    }

    private fun handleFailure(failure: Failure) {
        if (!view.isActive()) {
            return
        }

        view.showSuggestedName("")
    }

    override fun setPlaceName(name: String) {
        placeName = name
    }

    override fun saveAndExit() {
        if (placeName.isEmpty()) {
            view.showErrorEmptyName()
            return
        }

        view.navigateBackWithResult(placeName)
    }
}