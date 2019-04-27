package com.github.varhastra.epicenter.ui.details

import com.github.varhastra.epicenter.BasePresenter
import com.github.varhastra.epicenter.BaseView
import com.github.varhastra.epicenter.domain.model.Coordinates
import org.threeten.bp.LocalDateTime

interface DetailsContract {

    interface View : BaseView<Presenter> {
        enum class AlertType {
            ALERT_0,
            ALERT_2,
            ALERT_4,
            ALERT_6,
            ALERT_8
        }

        //        fun showEvent(remoteEvent: RemoteEvent)
        fun showEventMagnitude(magnitude: Double, type: String)

        fun setAlertColor(alertType: AlertType)

        fun showEventPlace(place: String)

        fun showEventDistance(distance: Double?)

        fun showEventCoordinates(coordinates: Coordinates)

        fun showTsunamiAlert(show: Boolean)

        fun showEventDate(localDateTime: LocalDateTime, daysAgo: Int)

        fun showEventDepth(depth: Double)

        fun showEventReports(reportsCount: Int)

        fun showEventLink(linkUrl: String)

        fun showErrorNoData()

        fun isActive(): Boolean

        fun showSourceLinkViewer(link: String)

        fun showEventOnMap(coordinates: Coordinates, alertType: AlertType)
    }

    interface Presenter : BasePresenter {
        fun init(eventId: String)

        fun loadEvent(eventId: String)

        fun openSourceLink()

        fun onMapReady()
    }
}