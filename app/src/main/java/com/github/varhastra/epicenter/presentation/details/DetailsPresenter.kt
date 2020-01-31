package com.github.varhastra.epicenter.presentation.details

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import com.github.varhastra.epicenter.domain.interactors.LoadEventInteractor
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.domain.repos.UnitsLocaleRepository
import com.github.varhastra.epicenter.presentation.common.AlertLevel
import com.github.varhastra.epicenter.presentation.common.EventMarker
import com.github.varhastra.epicenter.presentation.details.mappers.EventMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsPresenter(
        private val context: Context,
        private val view: DetailsContract.View,
        private val loadEventInteractor: LoadEventInteractor,
        private val unitsLocaleRepository: UnitsLocaleRepository
) : DetailsContract.Presenter {

    private lateinit var eventId: String

    private var remoteEvent: RemoteEvent? = null

    init {
        view.attachPresenter(this)
    }

    override fun init(eventId: String) {
        this.eventId = eventId
    }

    override fun start() {
        loadEvent(eventId)
    }

    override fun loadEvent(eventId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            loadEventInteractor(eventId).fold(
                    { handleEvent(it) },
                    { handleFailure(it) }
            )
        }
    }

    private suspend fun mapEventToView(remoteEvent: RemoteEvent): EventViewBlock = withContext(Dispatchers.Default) {
        val mapper = EventMapper(context, unitsLocaleRepository.preferredUnits)
        mapper.map(remoteEvent)
    }

    private suspend fun mapEventToMarker(remoteEvent: RemoteEvent): EventMarker = withContext(Dispatchers.Default) {
        val (event, _) = remoteEvent
        EventMarker(
                event.id,
                event.placeName,
                "",
                AlertLevel.from(event.magnitude),
                event.latitude,
                event.longitude
        )
    }

    private suspend fun handleEvent(remoteEvent: RemoteEvent) {
        this.remoteEvent = remoteEvent
        val eventViewBlock = mapEventToView(remoteEvent)
        val marker = mapEventToMarker(remoteEvent)
        view.run {
            showEvent(eventViewBlock)
            showEventMapMarker(marker)
        }
    }

    private fun handleFailure(t: Throwable) {
        view.showErrorNoData()
    }

    override fun openSourceLink() {
        val urlStr = remoteEvent?.event?.link
        if (urlStr != null && URLUtil.isNetworkUrl(urlStr)) {
            view.openSourceLink(Uri.parse(urlStr))
        }
    }
}