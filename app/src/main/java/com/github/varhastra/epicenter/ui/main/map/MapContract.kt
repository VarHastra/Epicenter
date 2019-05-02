package com.github.varhastra.epicenter.ui.main.map

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView

interface MapContract {

    interface View : BaseView<Presenter> {
        fun showTitle()

        fun showEventMarkers(markers: List<EventMarker>)

        fun isActive(): Boolean

        fun showEventDetails(eventId: String)
    }

    interface Presenter : BasePresenter {
        fun loadEvents()

        fun setMinMagnitude(minMagnitude: Int)

        fun setPeriod(days: Int)

        fun viewReady()

        fun openEventDetails(eventId: String)
    }
}