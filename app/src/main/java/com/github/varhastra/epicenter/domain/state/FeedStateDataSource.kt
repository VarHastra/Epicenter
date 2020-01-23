package com.github.varhastra.epicenter.domain.state

import com.github.varhastra.epicenter.domain.model.FeedFilter

interface FeedStateDataSource {

    var selectedPlaceId: Int

    var filter: FeedFilter

//    fun saveSelectedPlaceId(id: Int)

//    fun getSelectedPlaceId(): Int

//    fun saveCurrentFilter(filter: FeedFilter)

//    fun getCurrentFilter(): FeedFilter
}