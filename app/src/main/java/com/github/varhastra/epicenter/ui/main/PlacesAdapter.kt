package com.github.varhastra.epicenter.ui.main

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
import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.utils.UnitsLocale
import com.github.varhastra.epicenter.utils.kmToMi
import kotlin.math.roundToInt

class PlacesAdapter(val context: Context, val unitsLocale: UnitsLocale = Prefs.getPreferredUnits()) : BaseAdapter() {

    var places = listOf<Place>()

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
                "${getLocalizedDistance(place.radiusKm)} ${getLocalizedUnitsString()}"
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

        private fun getLocalizedDistance(distanceInKm: Double): Int {
            return when (unitsLocale) {
                UnitsLocale.METRIC -> distanceInKm.roundToInt()
                UnitsLocale.IMPERIAL -> kmToMi(distanceInKm.toDouble()).roundToInt()
                else -> distanceInKm.roundToInt()
            }
        }

        private fun getLocalizedUnitsString(): String {
            return when (unitsLocale) {
                UnitsLocale.METRIC -> context.getString(R.string.app_kilometers_abbreviation)
                UnitsLocale.IMPERIAL -> context.getString(R.string.app_miles_abbreviation)
                else -> context.getString(R.string.app_kilometers_abbreviation)
            }
        }
    }
}