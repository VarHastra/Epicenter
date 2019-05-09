package com.github.varhastra.epicenter.ui.placesmanager

import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.PlacesDataSource
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.Position
import java.util.*

class PlacesManagerPresenter(
        private val view: PlacesManagerContract.View,
        private val placesDataSource: PlacesDataSource
) : PlacesManagerContract.Presenter {

    private val deletionQueue: Queue<Place> = LinkedList()

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


    override fun openEditor(placeId: Int?) {
        if (placeId != Place.WORLD.id) {
            view.showEditor(placeId)
        }
    }

    override fun saveOrder(places: List<Place>) {
        placesDataSource.updateOrder(places)
    }

    override fun tryDeletePlace(place: Place) {
        deletionQueue.offer(place)
        view.showUndoDeleteOption()
    }

    override fun deletePlace() {
        deletionQueue.poll()?.apply {
            placesDataSource.deletePlace(this)
        }
    }

    override fun undoDeletion() {
        deletionQueue.poll()
        loadPlaces()
    }
}