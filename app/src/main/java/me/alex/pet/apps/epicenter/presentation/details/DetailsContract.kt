package me.alex.pet.apps.epicenter.presentation.details

import android.net.Uri
import me.alex.pet.apps.epicenter.presentation.BasePresenter
import me.alex.pet.apps.epicenter.presentation.BaseView
import me.alex.pet.apps.epicenter.presentation.common.EventMarker

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