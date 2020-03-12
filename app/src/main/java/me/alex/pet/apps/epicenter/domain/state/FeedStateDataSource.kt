package me.alex.pet.apps.epicenter.domain.state

import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel
import me.alex.pet.apps.epicenter.domain.model.sorting.SortCriterion
import me.alex.pet.apps.epicenter.domain.model.sorting.SortOrder

interface FeedStateDataSource {

    var selectedPlaceId: Int

    var sortCriterion: SortCriterion

    var sortOrder: SortOrder

    var minMagnitude: MagnitudeLevel
}