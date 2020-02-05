package com.github.varhastra.epicenter.presentation.placesmanager

import android.content.Context
import com.github.varhastra.epicenter.domain.interactors.DeletePlaceInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlacesInteractor
import com.github.varhastra.epicenter.domain.interactors.UpdatePlacesOrderInteractor
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.repos.UnitsLocaleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PlacesManagerPresenter(
        private val context: Context,
        private val view: PlacesManagerContract.View,
        private val loadPlaces: LoadPlacesInteractor,
        private val deletePlace: DeletePlaceInteractor,
        private val updatePlacesOrder: UpdatePlacesOrderInteractor,
        private val unitsLocaleRepository: UnitsLocaleRepository
) : PlacesManagerContract.Presenter {

    private val deletionQueue: Queue<Int> = LinkedList()

    init {
        view.attachPresenter(this)
    }

    override fun start() {
        fetchPlaces()
    }

    override fun fetchPlaces() {
        CoroutineScope(Dispatchers.Main).launch {
            loadPlaces().map { places -> mapPlacesToViews(places) }
                    .fold(::handlePlaces, ::handleFailure)
        }
    }

    private suspend fun mapPlacesToViews(places: List<Place>) = withContext(Dispatchers.Default) {
        val mapper = Mapper(context, unitsLocaleRepository.preferredUnits)
        places.map { mapper.map(it) }
    }

    private fun handlePlaces(places: List<PlaceViewBlock>) {
        if (!view.isActive()) {
            return
        }

        view.showPlaces(places)
    }

    private fun handleFailure(t: Throwable) {
        // TODO: implement
    }


    override fun editPlace(placeId: Int) {
        if (placeId != Place.WORLD.id) {
            view.showPlaceEditor(placeId)
        }
    }

    override fun addPlace() {
        view.showPlaceCreator()
    }

    override fun saveOrder(places: List<PlaceViewBlock>) {
        CoroutineScope(Dispatchers.Main).launch {
            updatePlacesOrder(places.map { it.id })
        }
    }

    override fun tryDeletePlace(placeId: Int) {
        deletionQueue.offer(placeId)
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