package me.alex.pet.apps.epicenter.domain.model.sorting

import me.alex.pet.apps.epicenter.domain.model.RemoteEvent

enum class SortCriterion(val value: Int, val comparisonFunction: (RemoteEvent?, RemoteEvent?) -> Int) {

    DATE(0, { o1, o2 -> compareValues(o1?.event?.localDatetime, o2?.event?.localDatetime) }),
    MAGNITUDE(1, { o1, o2 -> compareValues(o1?.event?.magnitude, o2?.event?.magnitude) }),
    DISTANCE(2, { o1, o2 -> compareValues(o1?.distance, o2?.distance) });

    companion object {
        fun fromValue(value: Int) = when (value) {
            0 -> DATE
            1 -> MAGNITUDE
            2 -> DISTANCE
            else -> throw IllegalArgumentException()
        }
    }
}