package com.github.varhastra.epicenter.domain.model

import com.github.varhastra.epicenter.common.toLocalDateTime
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime

data class Event(
        /**
         * Even id.
         */
        val id: String,

        /**
         * Magnitude of the event.
         * Note: can be negative for very small earthquakes.
         */
        val magnitude: Double,

        /**
         * Place description.
         */
        val placeName: String,

        /**
         * Timestamp when the event occurred.
         */
        val timestamp: Instant,

        /**
         * Decimal degrees latitude and longitude.
         */
        val coordinates: Coordinates,

        /**
         * Link to event page for the event.
         */
        val link: String,

        /**
         * The total number of felt reports submitted to USGS DYFI system.
         */
        val feltReportsCount: Int,

        /**
         * Set to true for large events in oceanic regions.
         */
        val tsunamiAlert: Boolean,

        /**
         * The method or algorithm used to calculate the preferred
         * magnitude for the event.
         */
        val magnitudeType: String,

        /**
         * Depth of the event in kilometers.
         */
        val depth: Double
) {

    /**
     * Decimal degrees latitude.
     */
    val latitude: Double
        get() {
            return coordinates.latitude
        }

    /**
     * Decimal degrees longitude.
     */
    val longitude: Double
        get() {
            return coordinates.longitude
        }

    /**
     * Time when the event happened converted
     * to local timezone.
     */
    val localDatetime: LocalDateTime
        get() {
            return timestamp.toLocalDateTime()
        }
}