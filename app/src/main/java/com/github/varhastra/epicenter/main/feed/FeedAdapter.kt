package com.github.varhastra.epicenter.main.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindColor
import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.utils.UnitsLocale
import com.github.varhastra.epicenter.utils.kmToMi
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.text.DecimalFormat
import kotlin.math.roundToInt

class FeedAdapter(val context: Context, unitsLocale: UnitsLocale = UnitsLocale.getDefault()) :
        RecyclerView.Adapter<FeedAdapter.EventHolder>() {

    @JvmField
    @BindColor(R.color.colorAlert0)
    @ColorInt
    var colorAlert0: Int = 0

    @JvmField
    @BindColor(R.color.colorAlert2)
    @ColorInt
    var colorAlert2: Int = 0

    @JvmField
    @BindColor(R.color.colorAlert4)
    @ColorInt
    var colorAlert4: Int = 0

    @JvmField
    @BindColor(R.color.colorAlert6)
    @ColorInt
    var colorAlert6: Int = 0

    @JvmField
    @BindColor(R.color.colorAlert8)
    @ColorInt
    var colorAlert8: Int = 0

    @JvmField
    @BindString(R.string.feed_event_distance)
    var distanceString: String = ""

    @JvmField
    @BindString(R.string.feed_event_depth)
    var depthString: String = ""

    @JvmField
    @BindString(R.string.app_miles_abbreviation)
    var milesAbbreviation: String = ""

    @JvmField
    @BindString(R.string.app_today)
    var todayString: String = ""

    @JvmField
    @BindString(R.string.app_yesterday)
    var yesterdayString: String = ""

    @JvmField
    @BindString(R.string.app_kilometers_abbreviation)
    @StringRes
    var kilometersAbbreviation: String = ""

    var unitsAbbreviation = ""

    val now = LocalDateTime.now()

    val magFormatter: DecimalFormat = DecimalFormat("0.0")
    val largeMagFormatter: DecimalFormat = DecimalFormat("#")
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)


    var data: List<RemoteEvent> = listOf()
        set (value) {
            field = value
            notifyDataSetChanged()
        }

    var unitsLocale: UnitsLocale = unitsLocale
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_feed_event, parent, false)
        return EventHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        ButterKnife.bind(this, recyclerView)

        unitsAbbreviation = when (unitsLocale) {
            UnitsLocale.METRIC -> kilometersAbbreviation
            UnitsLocale.IMPERIAL -> milesAbbreviation
            else -> kilometersAbbreviation
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(data[position])
    }


    inner class EventHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.tv_item_feed_magnitude)
        lateinit var magnitudeTextView: TextView

        @BindView(R.id.tv_item_feed_title)
        lateinit var titleTextView: TextView

        @BindView(R.id.tv_item_feed_distance)
        lateinit var distanceTextView: TextView

        @BindView(R.id.tv_item_feed_depth)
        lateinit var depthTextView: TextView

        @BindView(R.id.iv_item_feed_tsunami_indicator)
        lateinit var tsunamiImageView: ImageView

        @BindView(R.id.tv_item_feed_date)
        lateinit var dateTextView: TextView


        init {
            ButterKnife.bind(this, itemView)
        }

        fun bind(remoteEvent: RemoteEvent) {
            with(remoteEvent) {
                val formatter = if (event.magnitude < 10) magFormatter else largeMagFormatter
                magnitudeTextView.text = formatter.format(event.magnitude)
                magnitudeTextView.setTextColor(getAlertColor(event.magnitude.toInt()))

                titleTextView.text = event.placeName

                val dist = getLocalizedDistance(distance)
                distanceTextView.text = String.format(distanceString, dist?.roundToInt(), unitsAbbreviation)

                val depth = getLocalizedDistance(event.depth)
                depthTextView.text = String.format(depthString, depth?.roundToInt(), unitsAbbreviation)

                tsunamiImageView.visibility = if (event.tsunamiAlert) View.VISIBLE else View.GONE

                dateTextView.text = when {
                    event.localDatetime.toLocalDate() == now.toLocalDate() -> "$todayString ${timeFormatter.format(event.localDatetime.toLocalTime())}"
                    event.localDatetime.toLocalDate() == now.toLocalDate().minusDays(1) -> "$yesterdayString ${timeFormatter.format(
                            event.localDatetime.toLocalTime()
                    )}"
                    else -> dateTimeFormatter.format(event.localDatetime)
                }
            }
        }

        @ColorInt
        private fun getAlertColor(magnitude: Int): Int {
            return when (magnitude) {
                in -2 until 2 -> colorAlert0
                in 2 until 4 -> colorAlert2
                in 4 until 6 -> colorAlert4
                in 6 until 8 -> colorAlert6
                in 8..10 -> colorAlert8
                else -> colorAlert0
            }
        }

        private fun getLocalizedDistance(distance: Double?): Double? {
            if (distance == null) {
                return null
            }

            return when (unitsLocale) {
                UnitsLocale.METRIC -> distance
                UnitsLocale.IMPERIAL -> kmToMi(distance)
                else -> distance
            }
        }
    }
}