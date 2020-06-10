package me.alex.pet.apps.epicenter.presentation.details.mappers

import android.content.Context
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.isToday
import me.alex.pet.apps.epicenter.common.extensions.isYesterday
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import me.alex.pet.apps.epicenter.presentation.common.AlertLevel
import me.alex.pet.apps.epicenter.presentation.common.UnitsFormatter
import me.alex.pet.apps.epicenter.presentation.common.UnitsLocale
import me.alex.pet.apps.epicenter.presentation.details.EventViewBlock
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.chrono.IsoChronology
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeFormatterBuilder
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.format.TextStyle
import org.threeten.bp.temporal.ChronoUnit
import java.text.DecimalFormat

class EventMapper(val context: Context, unitsLocale: UnitsLocale) {

    private val magnitudeDecimalFormat = DecimalFormat("0.0")

    private val unitsFormatter = UnitsFormatter(context, unitsLocale)

    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendLocalized(FormatStyle.SHORT, FormatStyle.SHORT)
            .appendLiteral(" (")
            .appendLocalizedOffset(TextStyle.SHORT)
            .appendLiteral(")")
            .toFormatter()
            .withChronology(IsoChronology.INSTANCE)

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

        val dateTimeAtUtc0 = event.timestamp.atOffset(ZoneOffset.UTC)
        val dateTimeAtCurrentTimezone = event.timestamp.atZone(ZoneId.systemDefault())
        val localDateTime = dateTimeAtCurrentTimezone.toLocalDateTime()
        val daysAgoText = when {
            localDateTime.isToday -> context.getString(R.string.app_today)
            localDateTime.isYesterday -> context.getString(R.string.app_yesterday)
            else -> {
                val daysSinceEvent = ChronoUnit.DAYS.between(localDateTime.toLocalDate(), LocalDate.now()).toInt()
                context.resources.getQuantityString(R.plurals.plurals_details_days_ago, daysSinceEvent, daysSinceEvent)
            }
        }

        val depthText = unitsFormatter.getLocalizedDistanceString(event.depth)

        val feltReports = event.feltReportsCount.toString()

        val sourceLink = event.link

        val tsunamiAlert = event.tsunamiAlert

        return EventViewBlock(
                title,
                magnitudeText,
                alertLevel,
                magnitudeType,
                dateTimeFormatter.format(dateTimeAtUtc0),
                dateTimeFormatter.format(dateTimeAtCurrentTimezone),
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