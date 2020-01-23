package com.github.varhastra.epicenter.domain.state

import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.sorting.SortCriterion
import com.github.varhastra.epicenter.domain.model.sorting.SortOrder

interface FeedStateDataSource {

    var selectedPlaceId: Int

    var sortCriterion: SortCriterion

    var sortOrder: SortOrder

    var minMagnitude: MagnitudeLevel
}