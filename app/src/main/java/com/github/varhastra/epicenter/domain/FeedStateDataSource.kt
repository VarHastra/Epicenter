package com.github.varhastra.epicenter.domain

import com.github.varhastra.epicenter.domain.model.FeedFilter

interface FeedStateDataSource {

    fun getSelectedPlaceId(): Int

    fun getSelectedFilter(): FeedFilter
}