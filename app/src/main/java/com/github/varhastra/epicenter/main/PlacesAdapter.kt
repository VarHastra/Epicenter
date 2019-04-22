package com.github.varhastra.epicenter.main

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
import com.github.varhastra.epicenter.domain.model.Place

class PlacesAdapter(val context: Context) : BaseAdapter() {

    var places = listOf<Place>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        lateinit var holder: ViewHolder
        val place = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_popup_place, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        holder.textView.text = place.name
        // TODO: set drawable
//            vh.imgView.setImageDrawable()

        return view!!
    }

    override fun getItem(position: Int): Place {
        return places[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount() = places.size

    class ViewHolder(view: View) {
        @BindView(R.id.tv_item_view_popup_place)
        lateinit var textView: TextView

        @BindView(R.id.iv_item_view_popup_place_icon)
        lateinit var imgView: ImageView

        init {
            ButterKnife.bind(this, view)
        }
    }
}