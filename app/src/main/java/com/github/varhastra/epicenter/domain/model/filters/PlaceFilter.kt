package com.github.varhastra.epicenter.domain.model.filters

import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.RemoteEvent

class PlaceFilter(private val place: Place) : Filter<RemoteEvent> {
    override fun invoke(p1: RemoteEvent) = p1.event.coordinates in place
}