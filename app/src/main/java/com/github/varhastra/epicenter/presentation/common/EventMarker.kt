package com.github.varhastra.epicenter.presentation.common

import android.content.Context
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.temporal.ChronoUnit
import java.text.DecimalFormat

class EventMarker(
        val eventId: String,
        val markerTitle: String,
        val markerSnippet: String,
        val alertLevel: AlertLevel,
        val latitude: Double,
        val longitude: Double,
        val zIndex: Float = 0f,
        val alpha: Float = 1f
) : ClusterItem {

    private val latLng = LatLng(latitude, longitude)

    override fun getSnippet(): String = markerSnippet

    override fun getTitle(): String = markerTitle

    override fun getPosition(): LatLng = latLng

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventMarker

        if (eventId != other.eventId) return false

        return true
    }

    override fun hashCode(): Int {
        return eventId.hashCode()
    }

    override fun toString(): String {
        return "EventMarker(eventId='$eventId', markerTitle='$markerTitle', markerSnippet='$markerSnippet', alertLevel=$alertLevel, latitude=$latitude, longitude=$longitude)"
    }
}


fun EventMarker.toMarkerOptions(): MarkerOptions {
    return MarkerOptions().position(LatLng(latitude, longitude))
            .icon(BitmapDescriptorFactory.fromResource(alertLevel.markerResId))
            .title(markerTitle)
            .snippet(markerSnippet)
            .anchor(0.5f, 0.5f)
            .alpha(alpha)
            .zIndex(zIndex)
}


class Mapper(val context: Context) {

    private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)

    private val magnitudeFormat = DecimalFormat("0.0")

    fun map(remoteEvent: RemoteEvent): EventMarker {
        val (event, _) = remoteEvent

        val title = context.getString(
                R.string.map_format_marker_title,
                magnitudeFormat.format(event.magnitude),
                event.placeName
        )
        val snippet = dateTimeFormatter.format(event.localDatetime)
        val alertLevel = AlertLevel.from(event.magnitude)
        val zIndex = event.magnitude.toFloat()
        val alpha = calculateAlphaFor(event)

        return EventMarker(
                event.id,
                title,
                snippet,
                alertLevel,
                event.latitude,
                event.longitude,
                zIndex,
                alpha
        )
    }

    private fun calculateAlphaFor(event: Event): Float {
        val days = ChronoUnit.DAYS.between(event.timestamp, Instant.now())
        return 1.0f - 0.12f * days
    }
}