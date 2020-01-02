package com.github.varhastra.epicenter.presentation.placenamepicker

import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.LocationDataSource
import com.github.varhastra.epicenter.domain.model.Coordinates

class PlaceNamePickerPresenter(
        val view: PlaceNamePickerContract.View,
        val locationDataSource: LocationDataSource
) : PlaceNamePickerContract.Presenter {

    private var coordinates: Coordinates? = null
    private var placeName = ""

    init {
        view.attachPresenter(this)
    }

    override fun start() {
        loadSuggestedName()
    }

    override fun initialize(coordinates: Coordinates) {
        this.coordinates = coordinates
    }

    override fun loadSuggestedName() {
        if (!locationDataSource.isGeoCodingAvailable()) {
            return
        }

        val cord = coordinates ?: return
        locationDataSource.getLocationName(cord, object : DataSourceCallback<String> {
            override fun onResult(result: String) {
                if (!view.isActive()) {
                    return
                }

                view.showSuggestedName(result)
            }

            override fun onFailure(t: Throwable?) {
                if (!view.isActive()) {
                    return
                }

                view.showSuggestedName("")
            }
        })
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