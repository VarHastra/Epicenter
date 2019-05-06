package com.github.varhastra.epicenter.ui.placesmanager

import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.PlacesDataSource
import com.github.varhastra.epicenter.domain.model.Place

class PlacesManagerPresenter(
        private val view: PlacesManagerContract.View,
        private val placesDataSource: PlacesDataSource
) : PlacesManagerContract.Presenter {

    init {
        view.attachPresenter(this)
    }

    override fun start() {
        loadPlaces()
    }

    override fun loadPlaces() {
        placesDataSource.getPlaces(object : DataSourceCallback<List<Place>> {
            override fun onResult(result: List<Place>) {
                if (!view.isActive()) {
                    return
                }
                view.showPlaces(result)
            }

            override fun onFailure(t: Throwable?) {
                TODO("stub, not implemented")
            }
        })
    }


    override fun openEditor(placeId: Int) {
        view.showEditor(placeId)
    }

    override fun saveOrder(places: List<Place>) {
        // TODO("stub, not implemented")
    }

    override fun tryDeletePlace(placeId: Int) {
        // TODO("stub, not implemented")
    }

    override fun deletePlace() {
        // TODO("stub, not implemented")
    }
}