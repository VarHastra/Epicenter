package com.github.varhastra.epicenter.domain.model

import kotlin.math.PI
import kotlin.math.cos

private const val MI_TO_KM_RATIO = 1.609
private const val KM_TO_M_RATIO = 1000
private const val LAT_DEG_TO_KM_RATIO = 111.1

fun kmToMi(km: Double): Double {
    if (km < 0) throw IllegalArgumentException("Negative arguments are not allowed. The argument was $km.")
    return km / MI_TO_KM_RATIO
}

fun kmToM(km: Double): Double {
    if (km < 0) throw IllegalArgumentException("Negative arguments are not allowed. The argument was $km.")
    return km * KM_TO_M_RATIO
}

fun miToKm(mi: Double): Double {
    if (mi < 0) throw IllegalArgumentException("Negative arguments are not allowed. The argument was $mi.")
    return mi * MI_TO_KM_RATIO
}

fun mToKm(m: Double): Double {
    if (m < 0) throw IllegalArgumentException("Negative arguments are not allowed. The argument was $m.")
    return m / KM_TO_M_RATIO
}

fun latDegToKm(latDegree: Double) = latDegree * LAT_DEG_TO_KM_RATIO

fun lngDegToKm(lngDeg: Double, latDegree: Double) = lngDeg * cos(toRadians(latDegree)) * LAT_DEG_TO_KM_RATIO

fun toRadians(deg: Double) = deg / 180.0 * PI

fun toDegrees(rad: Double) = rad * 180.0 / PI