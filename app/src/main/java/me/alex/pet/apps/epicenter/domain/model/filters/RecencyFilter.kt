package me.alex.pet.apps.epicenter.domain.model.filters

import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

class RecencyFilter(daysAgo: Int) : Filter<RemoteEvent> {

    private val daysAgo: Int

    init {
        if (daysAgo < 1) {
            throw IllegalArgumentException("The 'daysAgo' actual value must be >= 1.")
        }
        this.daysAgo = daysAgo
    }

    override fun invoke(p1: RemoteEvent): Boolean {
        val now = Instant.now()
        return ChronoUnit.DAYS.between(p1.event.timestamp, now) <= daysAgo
    }
}