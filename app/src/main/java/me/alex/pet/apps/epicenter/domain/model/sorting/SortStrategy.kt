package me.alex.pet.apps.epicenter.domain.model.sorting

import me.alex.pet.apps.epicenter.domain.model.RemoteEvent

class SortStrategy(sorting: SortCriterion, sortOrder: SortOrder) : Comparator<RemoteEvent> {

    private val comparator: Comparator<RemoteEvent> = when (sortOrder) {
        SortOrder.ASCENDING -> Comparator { o1, o2 -> sorting.comparisonFunction(o1, o2) }
        SortOrder.DESCENDING -> Comparator { o1, o2 -> sorting.comparisonFunction(o2, o1) }
    }

    override fun compare(o1: RemoteEvent?, o2: RemoteEvent?): Int {
        return comparator.compare(o1, o2)
    }
}