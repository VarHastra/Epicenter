package com.github.varhastra.epicenter.presentation.placesmanager

import com.github.varhastra.epicenter.domain.PlacesRepository
import com.github.varhastra.epicenter.domain.RepositoryCallback
import com.github.varhastra.epicenter.domain.model.Place
import java.util.*

class PlacesManagerPresenter(
        private val view: PlacesManagerContract.View,
        private val placesRepository: PlacesRepository
) : PlacesManagerContract.Presenter {

    private val deletionQueue: Queue<Place> = LinkedList()

    init {
        view.attachPresenter(this)
    }

    override fun start() {
        loadPlaces()
    }

    override fun loadPlaces() {
        placesRepository.getPlaces(object : RepositoryCallback<List<Place>> {
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
        placesRepository.updateOrder(places)
    }

    override fun tryDeletePlace(place: Place) {
        deletionQueue.offer(place)
        view.showUndoDeleteOption()
    }

    override fun deletePlace() {
        deletionQueue.poll()?.apply {
            placesRepository.deletePlace(this)
        }
    }

    override fun undoDeletion() {
        deletionQueue.poll()
        loadPlaces()
    }
}