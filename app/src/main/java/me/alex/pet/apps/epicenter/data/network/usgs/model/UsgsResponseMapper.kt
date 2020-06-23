package me.alex.pet.apps.epicenter.data.network.usgs.model

import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.Event
import me.alex.pet.apps.epicenter.domain.model.Magnitude
import me.alex.pet.apps.epicenter.domain.model.Position
import org.threeten.bp.Instant

class UsgsResponseMapper {

    private val distancePattern = Regex("\\d+\\s?km\\s[NESW ]+\\sof\\s")

    fun mapToModel(usgsResponse: UsgsResponse): List<Event> {
        val features = usgsResponse.features
        return features.mapNotNull(::mapFeatureToEvent)
    }

    private fun mapFeatureToEvent(feature: Feature): Event? {
        val id = feature.id ?: return null

        val props = feature.properties
        val placeName = props.place?.replace(distancePattern, "") ?: return null
        val magnitudeValue = props.mag ?: return null
        val timestamp = props.time?.let { Instant.ofEpochMilli(it) } ?: return null
        val linkUrl = props.url ?: return null
        val feltReports = props.felt ?: 0
        val tsunamiAlert = props.tsunami == 1
        val magnitudeType = props.magType ?: return null

        val coordinates = feature.geometry.coordinates
        if (coordinates.size < 3) return null
        val (lng, lat, depth) = coordinates

        return Event(
                id,
                Position(Coordinates(lat, lng), depth, placeName),
                Magnitude(magnitudeValue, magnitudeType),
                timestamp,
                linkUrl,
                feltReports,
                tsunamiAlert
        )
    }
}