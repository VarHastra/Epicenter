package com.github.varhastra.epicenter.ui.main.map.maputils

import com.github.varhastra.epicenter.ui.main.map.AlertLevel
import com.github.varhastra.epicenter.ui.main.map.EventMarker
import com.github.varhastra.epicenter.utils.toLocalDateTime
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.temporal.ChronoUnit
import java.text.DecimalFormat
import java.util.*

class EventClusterItem(
        private val eventId: String,
        private val title: String,
        private val snippet: String,
        lat: Double,
        lng: Double,
        val alertLevel: AlertLevel,
        val alpha: Float
) : ClusterItem {

    private val position: LatLng = LatLng(lat, lng)

    override fun getSnippet() = snippet

    override fun getTitle() = title

    override fun getPosition() = position

    companion object {
        fun from(eventMarker: EventMarker): EventClusterItem {
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
            val magFormatter = DecimalFormat("0.0")


            return with(eventMarker) {
                val markerTitle = String.format(Locale.getDefault(), "%s %s", magFormatter.format(magnitude), title)

                EventClusterItem(
                        eventId,
                        markerTitle,
                        formatter.format(instant.toLocalDateTime()),
                        coordinates.latitude,
                        coordinates.longitude,
                        eventMarker.alertLevel,
                        getAlpha(eventMarker.instant)
                )
            }
        }

        private fun getAlpha(instant: Instant): Float {
            val days = ChronoUnit.DAYS.between(instant, Instant.now())
            val alpha = 1.0f - 0.3f * (days / 2)
            return if (alpha > 0) alpha else 0.1f
        }
    }
}