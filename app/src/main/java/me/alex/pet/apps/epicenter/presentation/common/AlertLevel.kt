package me.alex.pet.apps.epicenter.presentation.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import me.alex.pet.apps.epicenter.R

enum class AlertLevel(val value: Int, @ColorRes val colorResId: Int, @DrawableRes val markerResId: Int) {
    LEVEL_0(0, R.color.colorAlert0, R.drawable.marker_0),
    LEVEL_2(2, R.color.colorAlert2, R.drawable.marker_2),
    LEVEL_4(4, R.color.colorAlert4, R.drawable.marker_4),
    LEVEL_6(6, R.color.colorAlert6, R.drawable.marker_6),
    LEVEL_8(8, R.color.colorAlert8, R.drawable.marker_8);

    companion object {
        fun from(magnitude: Double): AlertLevel {
            return when {
                magnitude < 2 -> LEVEL_0
                magnitude < 4 -> LEVEL_2
                magnitude < 6 -> LEVEL_4
                magnitude < 8 -> LEVEL_6
                magnitude < 10 -> LEVEL_8
                else -> LEVEL_8
            }
        }
    }
}