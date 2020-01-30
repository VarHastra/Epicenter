package com.github.varhastra.epicenter.presentation.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.presentation.main.feed.PlaceViewBlock
import kotlinx.android.synthetic.main.item_view_popup_place.view.*

class PlacesAdapter : BaseAdapter() {

    var places = listOf<PlaceViewBlock>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
                ?: LayoutInflater.from(parent.context).inflate(R.layout.item_view_popup_place, parent, false)

        val holder = view.tag as? ViewHolder ?: ViewHolder(view)
        view.tag = holder

        val place = getItem(position)
        if (place == null) {
            holder.bindButton()
        } else {
            holder.bind(place)
        }

        return view
    }

    override fun getItem(position: Int): PlaceViewBlock? {
        return places.getOrNull(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount() = places.size + 1

    inner class ViewHolder(view: View) {
        private val titleTextView = view.popupTitleTextView

        private val radiusTextView = view.popupRadiusTextView

        private val iconImgView = view.popupIconImageView

        fun bind(place: PlaceViewBlock) {
            titleTextView.text = place.titleText
            radiusTextView.text = place.radiusText
            iconImgView.setImageResource(place.iconResId)
        }

        fun bindButton() {
            titleTextView.setText(R.string.feed_edit_places)
            radiusTextView.setText(R.string.feed_edit_places_desc)
            iconImgView.setImageResource(R.drawable.ic_place_edit_24px)
        }
    }
}