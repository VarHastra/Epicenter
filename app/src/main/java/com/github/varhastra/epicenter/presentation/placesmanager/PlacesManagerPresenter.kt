package com.github.varhastra.epicenter.presentation.placesmanager

import com.github.varhastra.epicenter.domain.interactors.DeletePlaceInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlacesInteractor
import com.github.varhastra.epicenter.domain.interactors.UpdatePlacesOrderInteractor
import com.github.varhastra.epicenter.domain.model.Place
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PlacesManagerPresenter(
        private val view: PlacesManagerContract.View,
        private val loadPlaces: LoadPlacesInteractor,
        private val deletePlace: DeletePlaceInteractor,
        private val updatePlacesOrder: UpdatePlacesOrderInteractor
) : PlacesManagerContract.Presenter {

    private val deletionQueue: Queue<Place> = LinkedList()

    init {
        view.attachPresenter(this)
    }

    override fun start() {
        fetchPlaces()
    }

    override fun fetchPlaces() {
        CoroutineScope(Dispatchers.Main).launch {
            loadPlaces().fold(::handlePlaces, ::handleFailure)
        }
    }

    private fun handlePlaces(places: List<Place>) {
        if (!view.isActive()) {
            return
        }
        view.showPlaces(places)
    }

    private fun handleFailure(t: Throwable) {
        // TODO: implement
    }


    override fun openEditor(placeId: Int?) {
        if (placeId != Place.WORLD.id) {
            view.showEditor(placeId)
        }
    }

    override fun saveOrder(places: List<Place>) {
        CoroutineScope(Dispatchers.Main).launch {
            updatePlacesOrder(places)
        }
    }

    override fun tryDeletePlace(place: Place) {
        deletionQueue.offer(place)
        view.showUndoDeleteOption()
    }

    override fun deletePlace() {
        if (deletionQueue.isEmpty()) {
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            deletePlace(deletionQueue.remove())
        }
    }

    override fun undoDeletion() {
        deletionQueue.clear()
        fetchPlaces()
    }
}