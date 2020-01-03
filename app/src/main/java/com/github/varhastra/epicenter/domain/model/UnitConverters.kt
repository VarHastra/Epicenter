package com.github.varhastra.epicenter.domain.model

import kotlin.math.PI
import kotlin.math.cos

private const val MI_TO_KM_RATIO = 1.609
private const val KM_TO_M_RATIO = 1000
private const val LAT_DEG_TO_KM_RATIO = 111.1

fun kmToMi(km: Double) = km / MI_TO_KM_RATIO

fun kmToM(km: Double) = km * KM_TO_M_RATIO

fun miToKm(mi: Double) = mi * MI_TO_KM_RATIO

fun mToKm(m: Double) = m / KM_TO_M_RATIO

fun latDegToKm(latDegree: Double) = latDegree * LAT_DEG_TO_KM_RATIO

fun latDegToMi(latDegree: Double) = kmToMi(latDegToKm(latDegree))

fun lngDegToMi(lngDeg: Double, latDegree: Double) = lngDeg * cos(degToRad(latDegree)) * LAT_DEG_TO_KM_RATIO

fun degToRad(deg: Double) = PI * deg / 180.0