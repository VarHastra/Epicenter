package com.github.varhastra.epicenter.data.networking

import com.github.varhastra.epicenter.model.Event

/**
 * Defines common interface for all responses of
 * [EventServiceProvider]s.
 */
interface EventServiceResponse {

    fun mapToModel(): List<Event>
}