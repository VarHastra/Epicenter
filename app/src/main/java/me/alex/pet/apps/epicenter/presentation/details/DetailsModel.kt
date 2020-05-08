package me.alex.pet.apps.epicenter.presentation.details

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.alex.pet.apps.epicenter.domain.interactors.LoadEventInteractor
import me.alex.pet.apps.epicenter.domain.model.RemoteEvent
import me.alex.pet.apps.epicenter.domain.model.failures.Failure
import me.alex.pet.apps.epicenter.domain.repos.UnitsLocaleRepository
import me.alex.pet.apps.epicenter.presentation.common.AlertLevel
import me.alex.pet.apps.epicenter.presentation.common.EventMarker
import me.alex.pet.apps.epicenter.presentation.common.events.Event
import me.alex.pet.apps.epicenter.presentation.details.mappers.EventMapper

class DetailsModel(
        private val context: Context,
        private val eventId: String,
        private val loadEventInteractor: LoadEventInteractor,
        private val unitsLocaleRepository: UnitsLocaleRepository
) : ViewModel() {

    private val event = MutableLiveData<RemoteEvent>()

    val eventViewBlock: LiveData<EventViewBlock>
        get() = _eventViewBlock
    private val _eventViewBlock = MutableLiveData<EventViewBlock>()

    val eventMarker: LiveData<EventMarker>
        get() = _eventMarker
    private val _eventMarker = MutableLiveData<EventMarker>()

    val visitSourceLinkEvent: LiveData<Event<Uri>>
        get() = _visitSourceLinkEvent
    private val _visitSourceLinkEvent = MutableLiveData<Event<Uri>>()


    init {
        viewModelScope.launch {
            fetchEvent(eventId)
        }
    }

    private suspend fun fetchEvent(eventId: String) {
        loadEventInteractor(eventId).fold(
                { handleEvent(it) },
                { handleFailure(it) }
        )
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
        event.value = remoteEvent
        _eventViewBlock.value = mapEventToView(remoteEvent)
        _eventMarker.value = mapEventToMarker(remoteEvent)
    }

    private fun handleFailure(failure: Failure) {
        // TODO: handle the failure and report it to the user
    }

    fun onVisitSource() {
        val urlStr = event.value!!.event.link
        if (URLUtil.isNetworkUrl(urlStr)) {
            _visitSourceLinkEvent.value = Event(Uri.parse(urlStr))
        }
    }
}