package me.alex.pet.apps.epicenter.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.toLocalDate
import me.alex.pet.apps.epicenter.domain.model.Event
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import org.threeten.bp.LocalDate
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
        val opacity: Float = 1f
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


fun EventMarker.toMarkerOptions(context: Context): MarkerOptions {
    return MarkerOptions().position(LatLng(latitude, longitude))
            .icon(newBitmapDescriptorFromVectorResource(context, alertLevel.markerResId))
            .title(markerTitle)
            .snippet(markerSnippet)
            .anchor(0.5f, 0.5f)
            .alpha(opacity)
            .zIndex(zIndex)
}

private fun newBitmapDescriptorFromVectorResource(context: Context, @DrawableRes resId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, resId)!!.apply {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    }
    val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


class Mapper(val context: Context) {

    private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)

    private val magnitudeFormat = DecimalFormat("0.0")

    private val opacityStep = 0.08f

    fun map(event: RemoteEvent): EventMarker {
        val title = context.getString(
                R.string.map_format_marker_title,
                magnitudeFormat.format(event.magnitude),
                event.placeName
        )
        val snippet = dateTimeFormatter.format(event.localDatetime)
        val alertLevel = AlertLevel.from(event.magnitude)
        val zIndex = event.magnitude.toFloat()
        val opacity = calculateOpacityFor(event)

        return EventMarker(
                event.id,
                title,
                snippet,
                alertLevel,
                event.latitude,
                event.longitude,
                zIndex,
                opacity
        )
    }

    private fun calculateOpacityFor(event: Event): Float {
        val localEventDate = event.timestamp.toLocalDate()
        val daysSinceEvent = ChronoUnit.DAYS.between(localEventDate, LocalDate.now())
        return 1.0f - opacityStep * daysSinceEvent
    }
}