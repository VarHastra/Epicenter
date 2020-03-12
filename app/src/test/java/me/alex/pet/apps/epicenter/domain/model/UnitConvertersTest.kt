package me.alex.pet.apps.epicenter.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import kotlin.math.PI


class UnitConvertersTest {

    private val fiveThousandths = 0.005

    private val fiveTenths = 0.5

    @Test
    fun `kmToMi converts 0 kilometers to miles`() {
        val distanceInKm = 0.0

        val distanceInMi = kmToMi(distanceInKm)

        assertThat(distanceInMi).isWithin(fiveThousandths).of(0.0)
    }

    @Test
    fun `kmToMi converts kilometers to miles`() {
        val distanceInKm = 1.0

        val distanceInMi = kmToMi(distanceInKm)

        assertThat(distanceInMi).isWithin(fiveThousandths).of(0.621)
    }


    @Test
    fun `miToKm converts 0 miles to kilometers`() {
        val distanceInMi = 0.0

        val distanceInKm = miToKm(distanceInMi)

        assertThat(distanceInKm).isWithin(fiveThousandths).of(0.0)
    }

    @Test
    fun `miToKm converts miles to kilometers`() {
        val distanceInMi = 1.0

        val distanceInKm = miToKm(distanceInMi)

        assertThat(distanceInKm).isWithin(fiveThousandths).of(1.609)
    }


    @Test
    fun `kmToM converts 0 kilometers to meters`() {
        val distanceInKm = 0.0

        val distanceInMeters = kmToM(distanceInKm)

        assertThat(distanceInMeters).isWithin(fiveThousandths).of(0.0)
    }

    @Test
    fun `kmToM converts kilometers to meters`() {
        val distanceInKm = 1.0

        val distanceInMeters = kmToM(distanceInKm)

        assertThat(distanceInMeters).isWithin(fiveThousandths).of(1000.0)
    }


    @Test
    fun `mToKm converts 0 meters to kilometers`() {
        val distanceInMeters = 0.0

        val distanceInKm = mToKm(distanceInMeters)

        assertThat(distanceInKm).isWithin(fiveThousandths).of(0.0)
    }

    @Test
    fun `mToKm converts meters to kilometers`() {
        val distanceInMeters = 1000.0

        val distanceInKm = mToKm(distanceInMeters)

        assertThat(distanceInKm).isWithin(fiveThousandths).of(1.0)
    }


    @Test
    fun `toRadians converts degrees to radians`() {
        val deg = 180.0

        val rad = toRadians(deg)

        assertThat(rad).isWithin(fiveThousandths).of(PI)
    }

    @Test
    fun `toDegrees converts radians to degrees`() {
        val rad = PI

        val deg = toDegrees(rad)

        assertThat(deg).isWithin(fiveThousandths).of(180.0)
    }


    @Test
    fun `latDegToKm converts degrees of latitude to kilometers`() {
        val latDeg = 1.0

        val km = latDegToKm(latDeg)

        assertThat(km).isWithin(fiveTenths).of(111.0)
    }

    @Test
    fun `lngDegToKm converts degrees of longitude to kilometers at the north pole`() {
        val lngDeg = 1.0
        val northPoleLatDeg = 90.0

        val km = lngDegToKm(lngDeg, northPoleLatDeg)

        assertThat(km).isWithin(fiveTenths).of(0.0)
    }

    @Test
    fun `lngDegToKm converts degrees of longitude to kilometers at 45 deg N`() {
        val lngDeg = 1.0
        val fortyFiveLatDeg = 45.0

        val km = lngDegToKm(lngDeg, fortyFiveLatDeg)

        assertThat(km).isWithin(fiveTenths).of(79.0)
    }

    @Test
    fun `lngDegToKm converts degrees of longitude to kilometers at the south pole`() {
        val lngDeg = 1.0
        val southPoleLatDeg = -90.0

        val km = lngDegToKm(lngDeg, southPoleLatDeg)

        assertThat(km).isWithin(fiveTenths).of(0.0)
    }
}