package com.github.varhastra.epicenter.presentation.places

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.github.varhastra.epicenter.R
import kotlinx.android.synthetic.main.item_view_place.view.*

class PlacesAdapter(
        private val context: Context
) : RecyclerView.Adapter<PlacesAdapter.PlaceHolder>() {

    var data: MutableList<PlaceViewBlock> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: ((Int) -> Unit)? = null

    var onDeleteItem: ((Int) -> Unit)? = null

    private val touchHelperCallback = object : ItemTouchHelper.SimpleCallback(UP or DOWN, 0) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition

            return if (!(data[from].isDraggable && data[to].isDraggable)) {
                false
            } else {
                data.add(to, data.removeAt(from))
                notifyItemMoved(from, to)
                true
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Intentionally do nothing
        }
    }

    private val itemTouchHelper = ItemTouchHelper(touchHelperCallback)


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_view_place, parent, false)
        return PlaceHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        holder.bindPlace(data[position])
    }

    override fun getItemId(position: Int): Long {
        return data[position].id.toLong()
    }


    inner class PlaceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val titleTextView = itemView.placeTitleTextView

        private val captionTextView = itemView.placeCaptionTextView

        private val iconIv = itemView.placeIconIv

        private val removeBtn = itemView.placeRemoveBtn

        fun bindPlace(place: PlaceViewBlock) {
            itemView.setOnClickListener { onItemClick?.invoke(place.id) }
            itemView.placeRemoveBtn.setOnClickListener {
                val removedPlace = data.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                onDeleteItem?.invoke(removedPlace.id)
            }

            titleTextView.text = place.title
            captionTextView.text = place.caption

            iconIv.setImageResource(place.iconResId)
            if (place.isDraggable) {
                iconIv.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        itemTouchHelper.startDrag(this@PlaceHolder)
                    }
                    false
                }
            }


            removeBtn.isVisible = place.isDeletable
        }
    }
}