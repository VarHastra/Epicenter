package com.github.varhastra.epicenter.data.network.usgs.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.threeten.bp.Instant

class UsgsResponseMapperTest {

    private val responseMapper = UsgsResponseMapper()

    @Test
    fun `mapToModel returns correct number of valid events`() {
        val stubFeatures = listOf(getFakeFeature())
        val stubResponse = getFakeResponse(features = stubFeatures)

        val actualEvents = responseMapper.mapToModel(stubResponse)

        assertThat(actualEvents.size).isEqualTo(1)
    }

    @Test
    fun `mapToModel maps feature id to event id`() {
        val stubFeature = getFakeFeature()
        val stubFeatures = listOf(stubFeature)
        val stubResponse = getFakeResponse(features = stubFeatures)
        val expectedId = stubFeature.id

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.id).isEqualTo(expectedId)
    }

    @Test
    fun `mapToModel filters events with null ids`() {
        val malformedFeature = getFakeFeature(id = null)
        val malformedFeatures = listOf(malformedFeature)
        val malformedResponse = getFakeResponse(features = malformedFeatures)

        val actualEvents = responseMapper.mapToModel(malformedResponse)

        assertThat(actualEvents.size).isEqualTo(0)
    }

    @Test
    fun `mapToModel maps feature magnitude to event magnitude`() {
        val stubProperties = getFakeProperties(mag = 0.0)
        val stubFeature = getFakeFeature(properties = stubProperties)
        val stubFeatures = listOf(stubFeature)
        val stubResponse = getFakeResponse(features = stubFeatures)
        val expectedMagnitude = stubProperties.mag

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.magnitude).isEqualTo(expectedMagnitude)
    }

    @Test
    fun `mapToModel filters events with null magnitude`() {
        val malformedProperties = getFakeProperties(mag = null)
        val malformedFeature = getFakeFeature(properties = malformedProperties)
        val malformedFeatures = listOf(malformedFeature)
        val malformedResponse = getFakeResponse(features = malformedFeatures)

        val actualEvents = responseMapper.mapToModel(malformedResponse)

        assertThat(actualEvents.size).isEqualTo(0)
    }

    @Test
    fun `mapToModel maps feature place name to event place name removing distance prefix`() {
        val expectedPlaceName = "Place"
        val stubProperties = getFakeProperties(place = "1234567890km NESW of $expectedPlaceName")
        val stubFeature = getFakeFeature(properties = stubProperties)
        val stubFeatures = listOf(stubFeature)
        val stubResponse = getFakeResponse(features = stubFeatures)

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.placeName).isEqualTo(expectedPlaceName)
    }

    @Test
    fun `mapToModel filters events with null place names`() {
        val malformedProperties = getFakeProperties(place = null)
        val malformedFeature = getFakeFeature(properties = malformedProperties)
        val malformedFeatures = listOf(malformedFeature)
        val malformedResponse = getFakeResponse(features = malformedFeatures)

        val actualEvents = responseMapper.mapToModel(malformedResponse)

        assertThat(actualEvents.size).isEqualTo(0)
    }

    @Test
    fun `mapToModel maps feature timestamp to event timestamp`() {
        val expectedTimestamp = Instant.ofEpochMilli(0L)
        val stubProperties = getFakeProperties(time = expectedTimestamp.toEpochMilli())
        val stubFeature = getFakeFeature(properties = stubProperties)
        val stubFeatures = listOf(stubFeature)
        val stubResponse = getFakeResponse(features = stubFeatures)

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.timestamp).isEqualTo(expectedTimestamp)
    }

    @Test
    fun `mapToModel filters events with null timestamp`() {
        val malformedProperties = getFakeProperties(time = null)
        val malformedFeature = getFakeFeature(properties = malformedProperties)
        val malformedFeatures = listOf(malformedFeature)
        val malformedResponse = getFakeResponse(features = malformedFeatures)

        val actualEvents = responseMapper.mapToModel(malformedResponse)

        assertThat(actualEvents.size).isEqualTo(0)
    }

    @Test
    fun `mapToModel maps feature coordinates to event coordinates`() {
        val stubGeometry = getFakeGeometry()
        val stubProperties = getFakeProperties()
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = stubProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)
        val expectedLatitude = stubGeometry.coordinates[1]
        val expectedLongitude = stubGeometry.coordinates[0]
        val expectedDepth = stubGeometry.coordinates[2]

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertAll(
                { assertThat(actualEvent.coordinates.latitude).isEqualTo(expectedLatitude) },
                { assertThat(actualEvent.coordinates.longitude).isEqualTo(expectedLongitude) },
                { assertThat(actualEvent.depth).isEqualTo(expectedDepth) }
        )
    }

    @Test
    fun `mapToModel filters events with malformed coordinates`() {
        val malformedGeometry = getFakeGeometry(coordinates = emptyList())
        val stubProperties = getFakeProperties()
        val stubFeatures = listOf(getFakeFeature(geometry = malformedGeometry, properties = stubProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)

        val actualEvents = responseMapper.mapToModel(stubResponse)

        assertThat(actualEvents.size).isEqualTo(0)
    }

    @Test
    fun `mapToModel maps feature link url to event link url`() {
        val stubGeometry = getFakeGeometry()
        val stubProperties = getFakeProperties()
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = stubProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)
        val expectedUrl = stubProperties.url

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.link).isEqualTo(expectedUrl)
    }

    @Test
    fun `mapToModel filters events with null link url`() {
        val stubGeometry = getFakeGeometry()
        val malformedProperties = getFakeProperties(url = null)
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = malformedProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)

        val actualEvents = responseMapper.mapToModel(stubResponse)

        assertThat(actualEvents.size).isEqualTo(0)
    }

    @Test
    fun `mapToModel maps feature felt reports to event felt reports`() {
        val stubGeometry = getFakeGeometry()
        val stubProperties = getFakeProperties()
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = stubProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)
        val expectedFeltReports = stubProperties.felt

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.feltReportsCount).isEqualTo(expectedFeltReports)
    }

    @Test
    fun `mapToModel maps null feature felt reports to zero`() {
        val stubGeometry = getFakeGeometry()
        val malformedProperties = getFakeProperties(felt = null)
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = malformedProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.feltReportsCount).isEqualTo(0)
    }

    @Test
    fun `mapToModel maps feature tsunami alert to event felt tsunami alert`() {
        val stubGeometry = getFakeGeometry()
        val stubProperties = getFakeProperties()
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = stubProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)
        val expectedTsunamiAlert = false

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.tsunamiAlert).isEqualTo(expectedTsunamiAlert)
    }

    @Test
    fun `mapToModel maps null tsunami alert to false`() {
        val stubGeometry = getFakeGeometry()
        val malformedProperties = getFakeProperties(tsunami = null)
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = malformedProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.tsunamiAlert).isEqualTo(false)
    }

    @Test
    fun `mapToModel maps feature magnitude type to event magnitude type`() {
        val stubGeometry = getFakeGeometry()
        val stubProperties = getFakeProperties()
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = stubProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)
        val expected = stubProperties.magType

        val actualEvent = responseMapper.mapToModel(stubResponse)[0]

        assertThat(actualEvent.magnitudeType).isEqualTo(expected)
    }

    @Test
    fun `mapToModel filters events with null magnitude type`() {
        val stubGeometry = getFakeGeometry()
        val malformedProperties = getFakeProperties(magType = null)
        val stubFeatures = listOf(getFakeFeature(geometry = stubGeometry, properties = malformedProperties))
        val stubResponse = getFakeResponse(features = stubFeatures)

        val actualEvents = responseMapper.mapToModel(stubResponse)

        assertThat(actualEvents.size).isEqualTo(0)
    }


    private fun getFakeResponse(
            metadata: Metadata = getFakeMetadata(),
            features: List<Feature> = getFakeFeatures(),
            boundingBox: List<Double> = getFakeBoundingBox()
    ) = UsgsResponse(
            "FeatureCollection",
            metadata,
            features,
            boundingBox
    )

    private fun getFakeMetadata() = Metadata(
            0L,
            "",
            "",
            0,
            "",
            0
    )

    private fun getFakeBoundingBox() = listOf(
            -10.0,
            -10.0,
            10.0,
            10.0
    )

    private fun getFakeFeatures() = listOf(
            getFakeFeature()
    )

    private fun getFakeFeature(
            type: String? = "Earthquake",
            properties: Properties = getFakeProperties(),
            geometry: Geometry = getFakeGeometry(),
            id: String? = ""
    ) = Feature(type, properties, geometry, id)

    private fun getFakeProperties(
            mag: Double? = 0.0,
            place: String? = "10km NESW of Place",
            time: Long? = 0L,
            url: String? = "",
            felt: Int? = 0,
            tsunami: Int? = 0,
            magType: String? = ""
    ) = Properties(

            mag,
            place,
            time,
            url,
            felt,
            tsunami,
            magType
    )

    private fun getFakeGeometry(
            type: String? = "Point",
            coordinates: List<Double> = listOf(0.0, 0.0, 0.0)
    ) = Geometry(type, coordinates)
}