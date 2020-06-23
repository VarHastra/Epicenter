package me.alex.pet.apps.epicenter.presentation.main.feed.mappers

import android.content.Context
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.isToday
import me.alex.pet.apps.epicenter.common.extensions.isYesterday
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import me.alex.pet.apps.epicenter.presentation.common.AlertLevel
import me.alex.pet.apps.epicenter.presentation.common.UnitsFormatter
import me.alex.pet.apps.epicenter.presentation.common.UnitsLocale
import me.alex.pet.apps.epicenter.presentation.main.feed.EventViewBlock
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.text.DecimalFormat
import kotlin.math.roundToInt

class EventMapper(context: Context, unitsLocale: UnitsLocale) {

    private val magnitudeDecimalFormat: DecimalFormat = DecimalFormat("0.0")

    private val largeMagnitudeDecimalFormat: DecimalFormat = DecimalFormat("#")

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    private var unitsFormatter = UnitsFormatter(context, unitsLocale)

    private val distanceString = context.getString(R.string.feed_event_distance)

    private val depthString = context.getString(R.string.feed_event_depth)

    private val todayString = context.getString(R.string.app_today)

    private val yesterdayString = context.getString(R.string.app_yesterday)

    fun map(event: RemoteEvent): EventViewBlock {
        val magnitudeFormat = if (event.magnitude < 10) magnitudeDecimalFormat else largeMagnitudeDecimalFormat

        val placeName = event.placeName

        val magnitudeText = magnitudeFormat.format(event.magnitude)
        val alertLevel = AlertLevel.from(event.magnitude)

        val distanceText = String.format(distanceString, unitsFormatter.getLocalizedDistanceString(event.distanceToUser?.roundToInt()))
        val depthText = String.format(depthString, unitsFormatter.getLocalizedDistanceString(event.depth))

        val tsunamiAlert = event.tsunamiAlert

        val prettifiedDate = when {
            event.localDatetime.isToday -> todayString
            event.localDatetime.isYesterday -> yesterdayString
            else -> dateFormatter.format(event.localDatetime.toLocalDate())
        }
        val prettifiedTime = timeFormatter.format(event.localDatetime.toLocalTime())
        val dateTimeText = "$prettifiedDate $prettifiedTime"

        return EventViewBlock(
                event.id,
                placeName,
                magnitudeText,
                alertLevel,
                distanceText,
                depthText,
                dateTimeText,
                tsunamiAlert
        )
    }
}