package com.github.varhastra.epicenter.main.feed

import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.*
import com.github.varhastra.epicenter.domain.model.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

class FeedPresenter(
        private val view: FeedContract.View,
        private val eventsDataSource: EventsDataSource,
        private val placesDataSource: PlacesDataSource,
        private val locationDataSource: LocationDataSource,
        private val feedStateDataSource: FeedStateDataSource = Prefs
) : FeedContract.Presenter {

    private val logger = AnkoLogger(this.javaClass)
    private lateinit var filter: FeedFilter
    private lateinit var place: Place

    init {
        view.attachPresenter(this)
    }

    override fun init() {
        TODO("stub, not implemented")
    }

    override fun start() {
        setPlace(feedStateDataSource.getSelectedPlaceId())
        view.showCurrentPlace(place)
        filter = feedStateDataSource.getCurrentFilter()
        view.showCurrentFilter(filter)
        // TODO: handle deleted place
        loadPlaces()
        loadEvents()
    }

    override fun loadPlaces() {
        placesDataSource.getPlaces(object : DataSourceCallback<List<Place>> {
            override fun onResult(result: List<Place>) {
                if (!view.isActive()) {
                    return
                }

                view.showPlaces(result)
            }

            override fun onFailure(t: Throwable?) {
                TODO("stub, not implemented")
            }
        })
    }

    override fun loadEvents() {
        locationDataSource.getLastLocation(object : DataSourceCallback<Position> {
            override fun onResult(result: Position) {
                loadEvents(result.coordinates)
            }

            override fun onFailure(t: Throwable?) {
                loadEvents(null)
                // todo: Report that location is not available
            }
        })
    }

    private fun loadEvents(coordinates: Coordinates?) {
        view.showProgress(true)
        val minsSinceUpd = ChronoUnit.MINUTES.between(eventsDataSource.getWeekFeedLastUpdated(), Instant.now())

        eventsDataSource.getWeekFeed(object : DataSourceCallback<List<Event>> {
            override fun onResult(result: List<Event>) {
                if (!view.isActive()) {
                    return
                }

                view.showProgress(false)
                if (result.isNotEmpty()) {
                    view.showEvents(RemoteEvent.from(result, coordinates))
                } else {
                    view.showError(FeedContract.View.ErrorReason.ERR_NO_EVENTS)
                }
            }

            override fun onFailure(t: Throwable?) {
                if (!view.isActive()) {
                    return
                }

                view.showProgress(false)
                logger.error("onFailure(): $t")
                TODO("stub, not implemented")
            }
        }, filter, place, minsSinceUpd > FORCE_LOAD_RATE_MINS)
    }

    override fun setPlaceAndReload(place: Place) {
        setPlace(place.id)
        loadEvents()
    }

    override fun setPlaceAndReload(placeId: Int) {
        setPlace(placeId)
        view.showCurrentPlace(place)
        loadEvents()
    }

    private fun setPlace(placeId: Int) {
        feedStateDataSource.saveSelectedPlaceId(placeId)
        placesDataSource.getPlace(object : DataSourceCallback<Place> {
            override fun onResult(result: Place) {
                this@FeedPresenter.place = result
            }

            override fun onFailure(t: Throwable?) {
                TODO("stub, not implemented")
            }
        }, placeId)
    }

    override fun setFilterAndReload(filter: FeedFilter) {
        this.filter = filter
        feedStateDataSource.saveCurrentFilter(filter)
        loadEvents()
    }

    override fun setMagnitudeFilterAndReload(minMag: Int) {
        filter = filter.copy(minMagnitude = minMag.toDouble())
        feedStateDataSource.saveCurrentFilter(filter)
        loadEvents()
    }

    override fun setSortingAndReload(sorting: FeedFilter.Sorting) {
        filter = filter.copy(sorting = sorting)
        feedStateDataSource.saveCurrentFilter(filter)
        loadEvents()
    }

    companion object {
        const val FORCE_LOAD_RATE_MINS = 10
    }
}