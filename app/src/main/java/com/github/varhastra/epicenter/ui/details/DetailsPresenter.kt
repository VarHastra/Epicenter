package com.github.varhastra.epicenter.ui.details

import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.UnitsLocaleDataSource
import com.github.varhastra.epicenter.domain.interactors.EventLoaderInteractor
import com.github.varhastra.epicenter.domain.interactors.InteractorCallback
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.utils.UnitsFormatter
import com.github.varhastra.epicenter.utils.UnitsLocale
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit

class DetailsPresenter(
        private val view: DetailsContract.View,
        private val eventLoader: EventLoaderInteractor,
        private val unitsLocaleDataSource: UnitsLocaleDataSource = Prefs
) : DetailsContract.Presenter {

    private val logger = AnkoLogger(this.javaClass)
    private lateinit var eventId: String
    private var event: RemoteEvent? = null
    private lateinit var unitsFormatter: UnitsFormatter

    init {
        view.attachPresenter(this)
    }

    override fun init(eventId: String) {
        this.eventId = eventId
    }

    override fun start() {
        loadEvent(eventId)
        unitsFormatter = UnitsFormatter(unitsLocaleDataSource.getPreferredUnitsLocale())
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
                    view.showEventDepth(depth, unitsFormatter)
                    view.showTsunamiAlert(tsunamiAlert)
                    val days = ChronoUnit.DAYS.between(localDatetime, LocalDateTime.now())
                    view.showEventDate(localDatetime, days.toInt())
                    view.showEventReports(feltReportsCount)
                    view.showEventLink(link)
                }

                view.showEventDistance(result.distance, unitsFormatter)
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

    override fun onMapReady() {
        val ev = event?.event
        if (ev == null) {
            val requestVals = EventLoaderInteractor.RequestValues(eventId)
            eventLoader.execute(requestVals, object : InteractorCallback<RemoteEvent> {
                override fun onResult(result: RemoteEvent) {
                    if (!view.isActive()) {
                        return
                    }

                    view.showEventOnMap(result.event.coordinates, getAlertType(result.event.magnitude.toInt()))
                }

                override fun onFailure(t: Throwable?) {
                    logger.error("Error retrieving event. ${t?.stackTrace}")
                    if (!view.isActive()) {
                        return
                    }

                    view.showErrorNoData()
                }
            })
        } else {
            view.showEventOnMap(ev.coordinates, getAlertType(ev.magnitude.toInt()))
        }
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