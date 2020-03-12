package me.alex.pet.apps.epicenter.domain.model.filters

import me.alex.pet.apps.epicenter.domain.model.Place
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent

class PlaceFilter(private val place: Place) : Filter<RemoteEvent> {
    override fun invoke(p1: RemoteEvent) = p1.event.coordinates in place
}