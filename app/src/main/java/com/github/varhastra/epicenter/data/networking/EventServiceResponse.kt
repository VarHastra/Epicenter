package com.github.varhastra.epicenter.data.networking

import com.github.varhastra.epicenter.domain.model.Event

/**
 * Defines common interface for all responses of
 * [EventServiceProvider]s.
 */
interface EventServiceResponse {

    fun mapToModel(): List<Event>
}