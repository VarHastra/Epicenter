package com.github.varhastra.epicenter.ui.main.map

enum class AlertLevel(val id: Int) {
    ALERT_0(0),
    ALERT_2(2),
    ALERT_4(4),
    ALERT_6(6),
    ALERT_8(8);

    companion object {
        fun fromMagnitudeValue(magnitude: Int): AlertLevel {
            return when (magnitude) {
                in -2 until 2 -> ALERT_0
                in 2 until 4 -> ALERT_2
                in 4 until 6 -> ALERT_4
                in 6 until 8 -> ALERT_6
                in 8..10 -> ALERT_8
                else -> ALERT_0
            }
        }
    }
}