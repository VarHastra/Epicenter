package me.alex.pet.apps.epicenter.data.network

import me.alex.pet.apps.epicenter.domain.model.Event

/**
 * Defines common interface for all responses of
 * [EventServiceProvider]s.
 */
interface EventServiceResponse {

    fun mapToModel(): List<Event>
}