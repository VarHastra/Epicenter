package com.github.varhastra.epicenter.presentation.details

import android.net.Uri
import com.github.varhastra.epicenter.presentation.BasePresenter
import com.github.varhastra.epicenter.presentation.BaseView
import com.github.varhastra.epicenter.presentation.common.EventMarker

interface DetailsContract {

    interface View : BaseView<Presenter> {
        fun showEvent(event: EventViewBlock)

        fun showEventMapMarker(marker: EventMarker)

        fun showErrorNoData()

        fun isActive(): Boolean

        fun openSourceLink(uri: Uri)
    }

    interface Presenter : BasePresenter {
        fun init(eventId: String)

        fun loadEvent(eventId: String)

        fun openSourceLink()
    }
}