package com.github.varhastra.epicenter.presentation.main.feed

import android.content.Context
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.extensions.isToday
import com.github.varhastra.epicenter.common.extensions.isYesterday
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.presentation.common.AlertLevel
import com.github.varhastra.epicenter.presentation.common.UnitsFormatter
import com.github.varhastra.epicenter.presentation.common.UnitsLocale
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.text.DecimalFormat
import kotlin.math.roundToInt

class Mapper(context: Context, unitsLocale: UnitsLocale) {

    private val magnitudeDecimalFormat: DecimalFormat = DecimalFormat("0.0")

    private val largeMagnitudeDecimalFormat: DecimalFormat = DecimalFormat("#")

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    private var unitsFormatter = UnitsFormatter(unitsLocale)

    private val distanceString = context.getString(R.string.feed_event_distance)

    private val depthString = context.getString(R.string.feed_event_depth)

    private val todayString = context.getString(R.string.app_today)

    private val yesterdayString = context.getString(R.string.app_yesterday)

    fun map(remoteEvent: RemoteEvent): EventViewBlock {
        val (event, distanceFromUser) = remoteEvent
        val magnitudeFormat = if (event.magnitude < 10) magnitudeDecimalFormat else largeMagnitudeDecimalFormat

        val placeName = event.placeName

        val magnitudeText = magnitudeFormat.format(event.magnitude)
        val alertLevel = AlertLevel.from(event.magnitude)

        val distanceText = String.format(distanceString, unitsFormatter.getLocalizedDistanceString(distanceFromUser?.roundToInt()))
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