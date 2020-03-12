package me.alex.pet.apps.epicenter.presentation.details.mappers

import android.content.Context
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import me.alex.pet.apps.epicenter.presentation.common.AlertLevel
import me.alex.pet.apps.epicenter.presentation.common.UnitsFormatter
import me.alex.pet.apps.epicenter.presentation.common.UnitsLocale
import me.alex.pet.apps.epicenter.presentation.details.EventViewBlock
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.temporal.ChronoUnit
import java.text.DecimalFormat

class EventMapper(val context: Context, unitsLocale: UnitsLocale) {

    private val magnitudeDecimalFormat = DecimalFormat("0.0")

    private val unitsFormatter = UnitsFormatter(context, unitsLocale)

    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

    private val distanceString = context.getString(R.string.details_event_distance)

    private val coordinatesString = context.getString(R.string.details_event_coordinates)

    fun map(remoteEvent: RemoteEvent): EventViewBlock {
        val (event, distance) = remoteEvent

        val title = event.placeName

        val magnitudeText = magnitudeDecimalFormat.format(event.magnitude)
        val magnitudeType = event.magnitudeType
        val alertLevel = AlertLevel.from(event.magnitude)

        val coordinatesText = String.format(
                coordinatesString,
                event.coordinates.latitude,
                event.coordinates.longitude
        )
        val distanceText = String.format(
                distanceString,
                unitsFormatter.getLocalizedDistanceString(distance?.toInt())
        )

        val dateTimeText = dateTimeFormatter.format(event.localDatetime)
        val daysAgo = ChronoUnit.DAYS.between(event.localDatetime, LocalDateTime.now()).toInt()
        val daysAgoText = context.resources.getQuantityString(R.plurals.plurals_details_days_ago, daysAgo, daysAgo)

        val depthText = unitsFormatter.getLocalizedDistanceString(event.depth)

        val feltReports = event.feltReportsCount.toString()

        val sourceLink = event.link

        val tsunamiAlert = event.tsunamiAlert

        return EventViewBlock(
                title,
                magnitudeText,
                alertLevel,
                magnitudeType,
                dateTimeText,
                daysAgoText,
                coordinatesText,
                distanceText,
                depthText,
                feltReports,
                sourceLink,
                tsunamiAlert
        )
    }
}