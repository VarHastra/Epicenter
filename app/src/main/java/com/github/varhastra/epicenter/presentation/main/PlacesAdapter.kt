package com.github.varhastra.epicenter.presentation.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.AppSettings
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.presentation.common.UnitsFormatter
import com.github.varhastra.epicenter.presentation.common.UnitsLocale
import kotlin.math.roundToInt

class PlacesAdapter(val context: Context, unitsLocale: UnitsLocale = AppSettings.preferredUnits) : BaseAdapter() {

    var places = listOf<Place>()

    var unitsLocale = unitsLocale
        set(value) {
            field = value
            unitsFormatter = UnitsFormatter(unitsLocale)
        }

    private var unitsFormatter = UnitsFormatter(unitsLocale)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        lateinit var holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_popup_place, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val place = getItem(position)
        if (place == null) {
            holder.bindButton()
        } else {
            holder.bind(place)
        }

        return view!!
    }

    override fun getItem(position: Int): Place? {
        return places.getOrNull(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount() = places.size + 1

    inner class ViewHolder(view: View) {
        @BindView(R.id.tv_item_view_popup_place)
        lateinit var textView: TextView

        @BindView(R.id.tv_item_view_popup_place_radius)
        lateinit var radiusTextView: TextView

        @BindView(R.id.iv_item_view_popup_place_icon)
        lateinit var imgView: ImageView

        init {
            ButterKnife.bind(this, view)
        }

        fun bind(place: Place) {
            textView.text = place.name
            val radiusStr = if (place.radiusKm == null) {
                context.getString(R.string.feed_place_infinite)
            } else {
                unitsFormatter.getLocalizedDistanceString(place.radiusKm.roundToInt())
            }
            radiusTextView.text = radiusStr

            val iconId = when (place.id) {
                Place.CURRENT_LOCATION.id -> R.drawable.ic_place_near_me_24px
                Place.WORLD.id -> R.drawable.ic_place_world_24px
                else -> R.drawable.ic_place_empty
            }
            imgView.setImageResource(iconId)
        }

        fun bindButton() {
            textView.setText(R.string.feed_edit_places)
            radiusTextView.setText(R.string.feed_edit_places_desc)
            imgView.setImageResource(R.drawable.ic_place_edit_24px)
        }
    }
}