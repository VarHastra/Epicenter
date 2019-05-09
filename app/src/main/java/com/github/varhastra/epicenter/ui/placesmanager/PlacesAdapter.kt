package com.github.varhastra.epicenter.ui.placesmanager

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.utils.UnitsLocale
import com.github.varhastra.epicenter.utils.kmToMi
import kotlinx.android.synthetic.main.item_view_place.view.*
import kotlin.math.roundToInt

class PlacesAdapter(val context: Context, val unitsLocale: UnitsLocale) : RecyclerView.Adapter<PlacesAdapter.PlaceHolder>() {

    var data: MutableList<Place> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onStartDrag: ((RecyclerView.ViewHolder) -> Unit)? = null
    var onItemClick: ((Place) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view_place, parent, false)
        return PlaceHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.bindPlace(data[position])
    }

    fun onItemMove(from: Int, to: Int) {
        val item = data.removeAt(from)
        data.add(to, item)

        notifyItemMoved(from, to)
    }

    fun onPrepareItemMove(from: Int, to: Int) = !(from in 0..1 || to in 0..1)


    inner class PlaceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindPlace(place: Place) {
            val radiusStr = if (place.radiusKm == null) {
                context.getString(R.string.places_infinite)
            } else {
                val localizedRadius = getLocalizedDistance(place.radiusKm)
                getLocalizedUnitsString(localizedRadius)
            }

            itemView.setOnClickListener { onItemClick?.invoke(data[adapterPosition]) }

            itemView.placeNameTextView.text = place.name
            itemView.radiusTextView.text = radiusStr

            val touchListener: ((View, MotionEvent) -> Boolean)? = if (adapterPosition <= 1) null else { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    onStartDrag?.invoke(this)
                }
                false
            }
            itemView.dragHandleView.setOnTouchListener(touchListener)

            itemView.removeButton.visibility = if (adapterPosition <= 1) View.INVISIBLE else View.VISIBLE
            itemView.dragHandleView.setImageDrawable(
                    when (adapterPosition) {
                        0 -> context.getDrawable(R.drawable.ic_place_near_me_24px)
                        1 -> context.getDrawable(R.drawable.ic_place_world_24px)
                        else -> context.getDrawable(R.drawable.ic_drag_handle_24px)
                    }
            )
        }

        private fun getLocalizedDistance(distanceInKm: Double): Int {
            return when (unitsLocale) {
                UnitsLocale.METRIC -> distanceInKm.roundToInt()
                UnitsLocale.IMPERIAL -> kmToMi(distanceInKm.toDouble()).roundToInt()
                else -> distanceInKm.roundToInt()
            }
        }

        private fun getLocalizedUnitsString(distance: Int): String {
            return when (unitsLocale) {
                UnitsLocale.METRIC -> context.getString(R.string.app_format_kilometers, distance)
                UnitsLocale.IMPERIAL -> context.getString(R.string.app_format_miles, distance)
                else -> context.getString(R.string.app_format_kilometers, distance)
            }
        }
    }
}