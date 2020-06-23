package me.alex.pet.apps.epicenter.domain.model.filters

import me.alex.pet.apps.epicenter.domain.model.RemoteEvent

class MagnitudeFilter(private val magnitudeLevel: MagnitudeLevel) : Filter<RemoteEvent> {
    override fun invoke(p1: RemoteEvent) = p1.magnitude >= magnitudeLevel.magnitudeValue
}

enum class MagnitudeLevel(val value: Int, val magnitudeValue: Int) {
    ZERO_OR_LESS(0, -10),
    ONE(1, 1),
    TWO(2, 2),
    THREE(3, 3),
    FOUR(4, 4),
    FIVE(5, 5),
    SIX(6, 6),
    SEVEN(7, 7),
    EIGHT(8, 8),
    NINE(9, 9);

    companion object {
        fun fromValue(value: Int) = when (value) {
            0 -> ZERO_OR_LESS
            1 -> ONE
            2 -> TWO
            3 -> THREE
            4 -> FOUR
            5 -> FIVE
            6 -> SIX
            7 -> SEVEN
            8 -> EIGHT
            9 -> NINE
            else -> throw IllegalArgumentException()
        }
    }
}