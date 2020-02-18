package com.github.varhastra.epicenter.data.network.usgs.model

import com.github.varhastra.epicenter.data.network.EventServiceResponse
import com.github.varhastra.epicenter.domain.model.Event
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UsgsResponse(
    val type: String,
    val metadata: Metadata,
    val features: List<Feature>,
    val bbox: List<Double>
) : EventServiceResponse {
    override fun mapToModel(): List<Event> {
        return UsgsResponseMapper().mapToModel(this)
    }
}

@JsonClass(generateAdapter = true)
data class Metadata(
        /**
         * Timestamp indicating when this response
         * was generated.
         */
        val generated: Long,

        /**
         * Url that leads to this response.
         */
        val url: String,

        /**
         * Text description of what it is.
         */
        val title: String,

        /**
         * Response status code.
         */
        val status: Int,

        /**
         * Api version.
         */
        val api: String,

        /**
         * Feature count.
         */
        val count: Int
)

@JsonClass(generateAdapter = true)
data class Feature(
        /**
         * The value is always: "Feature".
         */
        val type: String? = null,

        /**
         * Properties of the earthquake.
         * See [Properties].
         */
        val properties: Properties = Properties(),

        /**
         * Earthquake location.
         */
        val geometry: Geometry = Geometry(),

        /**
         * A unique identifier for the event. This is the current preferred id for the event, and may change over time.
         */
        val id: String? = null
)

@JsonClass(generateAdapter = true)
data class Geometry(
        /**
         * Basically, the value is always: "Point".
         */
        val type: String? = null,

        /**
         * Longitude, latitude and depth.
         */
        val coordinates: List<Double> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Properties(
        /**
         * The magnitude for the event. See also magType.
         * Note: it can be negative.
         */
        val mag: Double? = null,

        /**
         * Textual description of named geographic region near to the event.
         * This may be a city name, or a Flinn-Engdahl Region name.
         */
        val place: String? = null,

        /**
         * Time when the event occurred.
         * Times are reported in milliseconds since the epoch, and do not include leap seconds.
         */
        val time: Long? = null,

        /**
         * Time when the event was most recently updated.
         * Times are reported in milliseconds since the epoch.
         */
//        val updated: Long? = null,

        /**
         * Timezone offset from UTC in minutes at the event epicenter.
         */
//        val tz: Int? = null,

        /**
         * Link to USGS Event Page for event.
         */
        val url: String? = null,

        /**
         * Link to GeoJSON detail feed from a GeoJSON summary feed.
         */
//        val detail: String? = null,

        /**
         * The total number of felt reports submitted to the DYFI? system.
         */
        val felt: Int? = null,

        /**
         * The maximum reported intensity for the event.
         * Computed by DYFI. While typically reported as a roman numeral,
         * for the purposes of this API, intensity is expected as the decimal
         * equivalent of the roman numeral.
         */
//        val cdi: Double? = null,

        /**
         * The maximum estimated instrumental intensity for the event.
         * Computed by ShakeMap. While typically reported as a roman numeral,
         * for the purposes of this API, intensity is expected as the decimal equivalent
         * of the roman numeral. Learn more about magnitude vs. intensity.
         */
//        val mmi: Double? = null,

        /**
         * The alert level from the PAGER earthquake impact scale.
         * Typical Values: "green", "yellow", "red".
         */
//        val alert: String? = null,

        /**
         * This flag is set to "1" for large events in oceanic regions and "0" otherwise.
         */
        val tsunami: Int? = null,

        /**
         * A number describing how significant the event is. Larger numbers indicate a more significant event.
         * Values: [0, 1000]
         */
//        val sig: Int? = null,

        /**
         * The ID of a data contributor.
         */
//        val net: String? = null,

        /**
         * An identifying code assigned by - and unique from - the corresponding source for the event.
         */
//        val code: String? = null,

        /**
         * A comma-separated list of event ids that are associated to an event.
         * Value: ",ci15296281,us2013mqbd,at00mji9pf,"
         */
//        val ids: String? = null,

        /**
         * A comma-separated list of network contributors.
         * Value: ",us,nc,ci,"
         */
//        val sources: String? = null,

        /**
         * A comma-separated list of product types associated to this event.
         * Value: “,cap,dyfi,general-link,origin,p-wave-travel-times,phase-data,”
         */
//        val types: String? = null,

        /**
         * The total number of seismic stations used to determine earthquake location.
         */
//        val nst: Int? = null,

        /**
         * Horizontal distance from the epicenter to the nearest station (in degrees). 1 degree is approximately 111.2 kilometers.
         */
//        val dmin: Double? = null,

        /**
         * The root-mean-square (RMS) travel time residual, in sec, using all weights.
         * This parameter provides a measure of the fit of the observed arrival times to the predicted arrival times for this location. Smaller numbers reflect a better fit of the data.
         */
//        val rms: Double? = null,

        /**
         * The largest azimuthal gap between azimuthally adjacent stations (in degrees).
         * In general, the smaller this number,
         * the more reliable is the calculated horizontal position of the earthquake.
         */
//        val gap: Double? = null,

        /**
         * The method or algorithm used to calculate the preferred magnitude for the event.
         */
        val magType: String? = null

        /**
         * Type of seismic event.
         * Values: “earthquake”, “quarry”
         */
//        val type: String? = null,

        /**
         * Magnitude + place.
         */
//        val title: String? = null
)