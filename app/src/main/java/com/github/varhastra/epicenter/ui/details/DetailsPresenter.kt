package com.github.varhastra.epicenter.ui.details

import com.github.varhastra.epicenter.domain.interactors.EventLoaderInteractor
import com.github.varhastra.epicenter.domain.interactors.InteractorCallback
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.time.temporal.Temporal

class DetailsPresenter(
        private val view: DetailsContract.View,
        private val eventLoader: EventLoaderInteractor
) : DetailsContract.Presenter {

    private val logger = AnkoLogger(this.javaClass)
    private lateinit var eventId: String
    private var event: RemoteEvent? = null

    init {
        view.attachPresenter(this)
    }

    override fun init(eventId: String)
    {
        this.eventId = eventId
    }

    override fun start() {
        loadEvent(eventId)
    }

    override fun loadEvent(eventId: String) {
        val requestVals = EventLoaderInteractor.RequestValues(eventId)
        eventLoader.execute(requestVals, object : InteractorCallback<RemoteEvent> {
            override fun onResult(result: RemoteEvent) {
                if (!view.isActive()) {
                    return
                }

                event = result
                with(result.event) {
                    view.setAlertColor(getAlertType(magnitude.toInt()))
                    view.showEventMagnitude(magnitude, magnitudeType)
                    view.showEventPlace(placeName)
                    view.showEventCoordinates(coordinates)
                    view.showEventDepth(depth)
                    val days = ChronoUnit.DAYS.between(localDatetime, LocalDateTime.now())
                    view.showEventDate(localDatetime, days.toInt())
                    view.showEventReports(feltReportsCount)
                    view.showEventLink(link)
                }

                view.showEventDistance(result.distance)
            }

            override fun onFailure(t: Throwable?) {
                logger.error("Error retrieving event. ${t?.stackTrace}")
                if (!view.isActive()) {
                    return
                }

                view.showErrorNoData()
            }
        })
    }

    override fun openSourceLink() {
        val e = event
        e?.let {
            view.showSourceLinkViewer(e.event.link)
        }
    }

    private fun getAlertType(magnitude: Int): DetailsContract.View.AlertType {
        return when (magnitude) {
            in -2 until 2 -> DetailsContract.View.AlertType.ALERT_0
            in 2 until 4 -> DetailsContract.View.AlertType.ALERT_2
            in 4 until 6 -> DetailsContract.View.AlertType.ALERT_4
            in 6 until 8 -> DetailsContract.View.AlertType.ALERT_6
            in 8..10 -> DetailsContract.View.AlertType.ALERT_8
            else -> DetailsContract.View.AlertType.ALERT_0
        }
    }
}