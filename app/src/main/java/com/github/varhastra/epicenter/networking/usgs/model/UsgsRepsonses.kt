package com.github.varhastra.epicenter.networking.usgs.model

import com.github.varhastra.epicenter.model.Event
import com.github.varhastra.epicenter.networking.EventServiceResponse


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

data class Feature(
        /**
         * The value is always: "Feature".
         */
        val type: String,

        /**
         * Properties of the earthquake.
         * See [Properties].
         */
        val properties: Properties,

        /**
         * Earthquake location.
         */
        val geometry: Geometry,

        val id: String
)

data class Geometry(
        /**
         * Basically, the value is always: "Point".
         */
        val type: String,

        /**
         * Longitude, latitude and depth.
         */
        val coordinates: List<Double>
)

data class Properties(
        /**
         * The magnitude for the event. See also magType.
         * Note: it can be negative.
         */
        val mag: Double,

        /**
         * Textual description of named geographic region near to the event.
         * This may be a city name, or a Flinn-Engdahl Region name.
         */
        val place: String,

        /**
         * Time when the event occurred.
         * Times are reported in milliseconds since the epoch, and do not include leap seconds.
         */
        val time: Long,

        /**
         * Time when the event was most recently updated.
         * Times are reported in milliseconds since the epoch.
         */
        val updated: Long,

        /**
         * Timezone offset from UTC in minutes at the event epicenter.
         */
        val tz: Int,

        /**
         * Link to USGS Event Page for event.
         */
        val url: String,

        /**
         * Link to GeoJSON detail feed from a GeoJSON summary feed.
         */
        val detail: String,

        /**
         * The total number of felt reports submitted to the DYFI? system.
         */
        val felt: Int?,

        /**
         * The maximum reported intensity for the event.
         * Computed by DYFI. While typically reported as a roman numeral,
         * for the purposes of this API, intensity is expected as the decimal
         * equivalent of the roman numeral.
         */
        val cdi: Double?,

        /**
         * The maximum estimated instrumental intensity for the event.
         * Computed by ShakeMap. While typically reported as a roman numeral,
         * for the purposes of this API, intensity is expected as the decimal equivalent
         * of the roman numeral. Learn more about magnitude vs. intensity.
         */
        val mmi: Double?,

        /**
         * The alert level from the PAGER earthquake impact scale.
         * Typical Values: "green", "yellow", "red".
         */
        val alert: String?,

        /**
         * This flag is set to "1" for large events in oceanic regions and "0" otherwise.
         */
        val tsunami: Int,

        /**
         * A number describing how significant the event is. Larger numbers indicate a more significant event.
         * Values: [0, 1000]
         */
        val sig: Int,

        /**
         * The ID of a data contributor.
         */
        val net: String,

        /**
         * An identifying code assigned by - and unique from - the corresponding source for the event.
         */
        val code: String,

        /**
         * A comma-separated list of event ids that are associated to an event.
         * Value: ",ci15296281,us2013mqbd,at00mji9pf,"
         */
        val ids: String,

        /**
         * A comma-separated list of network contributors.
         * Value: ",us,nc,ci,"
         */
        val sources: String,

        /**
         * A comma-separated list of product types associated to this event.
         * Value: “,cap,dyfi,general-link,origin,p-wave-travel-times,phase-data,”
         */
        val types: String,

        /**
         * The total number of seismic stations used to determine earthquake location.
         */
        val nst: Int,

        /**
         * Horizontal distance from the epicenter to the nearest station (in degrees). 1 degree is approximately 111.2 kilometers.
         */
        val dmin: Double,

        /**
         * The root-mean-square (RMS) travel time residual, in sec, using all weights.
         * This parameter provides a measure of the fit of the observed arrival times to the predicted arrival times for this location. Smaller numbers reflect a better fit of the data.
         */
        val rms: Double,

        /**
         * The largest azimuthal gap between azimuthally adjacent stations (in degrees).
         * In general, the smaller this number,
         * the more reliable is the calculated horizontal position of the earthquake.
         */
        val gap: Double,

        /**
         * The method or algorithm used to calculate the preferred magnitude for the event.
         */
        val magType: String,

        /**
         * Type of seismic event.
         * Values: “earthquake”, “quarry”
         */
        val type: String,

        /**
         * Magnitude + place.
         */
        val title: String
)