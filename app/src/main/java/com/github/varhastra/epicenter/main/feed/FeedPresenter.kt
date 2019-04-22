package com.github.varhastra.epicenter.main.feed

import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.EventsDataSource
import com.github.varhastra.epicenter.domain.FeedStateDataSource
import com.github.varhastra.epicenter.domain.PlacesDataSource
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place

class FeedPresenter(
        private val view: FeedContract.View,
        private val eventsDataSource: EventsDataSource,
        private val placesDataSource: PlacesDataSource,
        private val feedStateDataSource: FeedStateDataSource = Prefs
) : FeedContract.Presenter {

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
        view.showProgress(true)
        eventsDataSource.getWeekFeed(object : DataSourceCallback<List<Event>> {
            override fun onResult(result: List<Event>) {
                if (!view.isActive()) {
                    return
                }

                view.showProgress(false)
                view.showEvents(result)
            }

            override fun onFailure(t: Throwable?) {
                if (!view.isActive()) {
                    return
                }

                view.showProgress(false)
                TODO("stub, not implemented")
            }
        }, filter, place)
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
}