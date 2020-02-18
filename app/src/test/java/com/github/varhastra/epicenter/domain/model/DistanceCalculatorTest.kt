package com.github.varhastra.epicenter.domain.model

import com.google.common.collect.Range
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DistanceCalculatorTest {

    @Test
    fun `haversineDistance calculates distance between north and south poles`() {
        val northPole = Coordinates(90.0, 0.0)
        val southPole = Coordinates(-90.0, 0.0)
        val expectedRange = Range.closed(20_000.0, 20_020.0)

        val actual = haversineDistance(northPole, southPole)

        assertThat(actual).isIn(expectedRange)
    }

    @Test
    fun `haversineDistance calculates half the length of the equator using positive longitude`() {
        val equatorAndPrimeMeridian = Coordinates(0.0, 0.0)
        val equatorAndInternationalDateLine = Coordinates(0.0, 180.0)
        val expectedRange = Range.closed(20_000.0, 20_020.0)

        val actual = haversineDistance(equatorAndPrimeMeridian, equatorAndInternationalDateLine)

        assertThat(actual).isIn(expectedRange)
    }

    @Test
    fun `haversineDistance calculates half the length of the equator using negative longitude`() {
        val equatorAndPrimeMeridian = Coordinates(0.0, 0.0)
        val equatorAndInternationalDateLine = Coordinates(0.0, -180.0)
        val expectedRange = Range.closed(20_000.0, 20_020.0)

        val actual = haversineDistance(equatorAndPrimeMeridian, equatorAndInternationalDateLine)

        assertThat(actual).isIn(expectedRange)
    }

    @Test
    fun `haversineDistance calculates distance symmetrically`() {
        val northPole = Coordinates(90.0, 0.0)
        val southPole = Coordinates(-90.0, 0.0)

        val northToSouth = haversineDistance(northPole, southPole)
        val southToNorth = haversineDistance(southPole, northPole)

        assertThat(northToSouth).isWithin(1.0e-10).of(southToNorth)
    }
}