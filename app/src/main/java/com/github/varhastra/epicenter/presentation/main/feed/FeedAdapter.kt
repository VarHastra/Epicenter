package com.github.varhastra.epicenter.presentation.main.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindColor
import butterknife.BindString
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.presentation.common.UnitsFormatter
import com.github.varhastra.epicenter.presentation.common.UnitsLocale
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
    @BindString(R.string.app_today)
    var todayString: String = ""

    @JvmField
    @BindString(R.string.app_yesterday)
    var yesterdayString: String = ""


    private val now = LocalDateTime.now()

    private val magFormatter: DecimalFormat = DecimalFormat("0.0")
    private val largeMagFormatter: DecimalFormat = DecimalFormat("#")
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    var onEventClickListener: ((RemoteEvent, Int) -> Unit)? = null

    var data: List<RemoteEvent> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var unitsLocale: UnitsLocale = unitsLocale
        set(value) {
            field = value
            unitsFormatter = UnitsFormatter(unitsLocale)
        }

    private var unitsFormatter = UnitsFormatter(unitsLocale)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_feed_event, parent, false)
        return EventHolder(view)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        ButterKnife.bind(this, recyclerView)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemId(position: Int): Long {
        return with(data[position].event) {
            latitude.toRawBits() xor longitude.toRawBits() xor timestamp.toEpochMilli()
        }
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
            itemView.setOnClickListener { onEventClickListener?.invoke(remoteEvent, adapterPosition) }

            with(remoteEvent) {
                val formatter = if (event.magnitude < 10) magFormatter else largeMagFormatter
                magnitudeTextView.text = formatter.format(event.magnitude)
                magnitudeTextView.setTextColor(getAlertColor(event.magnitude.toInt()))

                titleTextView.text = event.placeName

                distanceTextView.text = String.format(distanceString, unitsFormatter.getLocalizedDistanceString(distance?.roundToInt()))

                depthTextView.text = String.format(depthString, unitsFormatter.getLocalizedDistanceString(event.depth))

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
    }
}