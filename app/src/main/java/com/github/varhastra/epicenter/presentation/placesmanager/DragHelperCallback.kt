package com.github.varhastra.epicenter.presentation.placesmanager

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragHelperCallback : ItemTouchHelper.Callback() {

    var onMove: ((Int, Int) -> Unit)? = null
    var onPrepareItemMove: ((Int, Int) -> Boolean)? = null

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val onCheckMove = onPrepareItemMove
        onCheckMove?.apply {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            return if (onCheckMove.invoke(from, to)) {
                onMove?.invoke(viewHolder.adapterPosition, target.adapterPosition)
                true
            } else {
                false
            }
        }
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Do nothing
    }
}