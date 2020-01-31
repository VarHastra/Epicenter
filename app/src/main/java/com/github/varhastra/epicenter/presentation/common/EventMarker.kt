package com.github.varhastra.epicenter.presentation.common

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class EventMarker(
        val eventId: String,
        val title: String,
        val snippet: String,
        val alertLevel: AlertLevel,
        val latitude: Double,
        val longitude: Double
)

fun EventMarker.toMarkerOptions(): MarkerOptions {
    return MarkerOptions().position(LatLng(latitude, longitude))
            .icon(BitmapDescriptorFactory.fromResource(alertLevel.markerResId))
            .title(title)
            .snippet(snippet)
            .anchor(0.5f, 0.5f)
}