package me.alex.pet.apps.epicenter.domain.model.sorting

enum class SortOrder(val value: Int) {

    ASCENDING(0),
    DESCENDING(1);

    companion object {
        fun fromValue(value: Int) = when (value) {
            0 -> ASCENDING
            1 -> DESCENDING
            else -> throw IllegalArgumentException()
        }
    }
}