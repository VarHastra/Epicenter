package com.github.varhastra.epicenter.presentation.common

import androidx.annotation.ColorRes
import com.github.varhastra.epicenter.R

enum class AlertLevel(val value: Int, @ColorRes val colorResId: Int) {
    LEVEL_0(0, R.color.colorAlert0),
    LEVEL_2(2, R.color.colorAlert2),
    LEVEL_4(4, R.color.colorAlert4),
    LEVEL_6(6, R.color.colorAlert6),
    LEVEL_8(8, R.color.colorAlert8);

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