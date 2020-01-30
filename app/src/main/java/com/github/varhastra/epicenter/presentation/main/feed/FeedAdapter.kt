package com.github.varhastra.epicenter.presentation.main.feed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.extensions.setTextColorRes
import kotlinx.android.synthetic.main.item_view_feed_event.view.*

class FeedAdapter(val context: Context) :
        RecyclerView.Adapter<FeedAdapter.EventHolder>() {

    var onItemClickListener: ((String, Int) -> Unit)? = null

    var data: List<EventViewBlock> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_feed_event, parent, false)
        return EventHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(data[position])
    }


    inner class EventHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val magnitudeTextView = itemView.magnitudeTextView

        private val titleTextView = itemView.titleTextView

        private val distanceTextView = itemView.distanceTextView

        private val depthTextView = itemView.depthTextView

        private val tsunamiImageView = itemView.tsunamiIconImgView

        private val dateTextView = itemView.dateTextView


        fun bind(event: EventViewBlock) {
            itemView.setOnClickListener { onItemClickListener?.invoke(event.id, adapterPosition) }

            event.let {
                titleTextView.text = it.title
                magnitudeTextView.text = it.magnitude
                magnitudeTextView.setTextColorRes(it.alertLevel.colorResId)
                distanceTextView.text = it.distance
                depthTextView.text = it.depth
                tsunamiImageView.isVisible = it.tsunamiAlert
                dateTextView.text = it.date
            }
        }
    }
}