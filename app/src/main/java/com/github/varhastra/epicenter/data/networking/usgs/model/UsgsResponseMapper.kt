package com.github.varhastra.epicenter.data.networking.usgs.model

import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Event
import org.threeten.bp.Instant

/**
 * Converts responses received from USGS
 * to app's internal model.
 */
class UsgsResponseMapper {

    private val distancePattern = Regex("\\d+\\s?km\\s[A-Za-z]+\\sof\\s")

    fun mapToModel(usgsResponse: UsgsResponse): List<Event> {
        val earthquakes = mutableListOf<Event>()

        val features = usgsResponse.features
        for (feature in features) {
            earthquakes.add(mapFeatureToModel(feature))
        }

        return earthquakes
    }

    private fun mapFeatureToModel(feature: Feature): Event {
        val id = feature.id
        val coordinates = feature.geometry.coordinates

        return with(feature.properties) {
            val placeName = place.replace(distancePattern, "")
            Event(id, mag, placeName, Instant.ofEpochMilli(time),
                    Coordinates(coordinates[1], coordinates[0]),
                    url,
                    felt ?: 0,
                    tsunami == 1,
                    magType,
                    coordinates[2]
            )
        }
    }
}