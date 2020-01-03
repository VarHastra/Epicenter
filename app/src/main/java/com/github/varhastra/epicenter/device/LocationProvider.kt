package com.github.varhastra.epicenter.device

import android.content.Context
import android.location.Geocoder
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Position
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.github.varhastra.epicenter.domain.repos.RepositoryCallback
import com.google.android.gms.location.LocationServices
import org.jetbrains.anko.*

class LocationProvider(val context: Context = App.instance) : LocationRepository {

    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context)
    private val logger = AnkoLogger(this.javaClass)

    override fun getLastLocation(callback: RepositoryCallback<Position>) {
        try {
            locationProviderClient.lastLocation.addOnSuccessListener {
                if (it == null) {
                    logger.warn("getLastLocation() fusedLocationProvider has return null")
                    callback.onFailure(null) // todo: throw custom throwable
                } else {
                    logger.debug("getLastLocation() ${it.latitude} ${it.longitude}")
                    callback.onResult(Position(Coordinates(it.latitude, it.longitude), it.accuracy.toDouble(), it.time))
                }

            }.addOnFailureListener {
                callback.onFailure(it)
            }
        } catch (e: SecurityException) {
            callback.onFailure(e)
        }
    }

    override fun getLocationName(coordinates: Coordinates, callback: RepositoryCallback<String>) {
        doAsync(callback::onFailure) {
            val addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
            uiThread {
                if (addresses.isEmpty()) {
                    logger.warn("getLocationName() geocoder has returned empty list")
                    callback.onFailure(null)
                } else {
                    val address = addresses[0]
                    logger.debug("getLocationName() $address")
                    val addressString = address.locality?.plus(", ").orEmpty() +
                            address.adminArea?.plus(", ").orEmpty() +
                            address.countryCode.orEmpty()
                    callback.onResult(addressString)
                }
            }
        }
    }

    override fun isGeoCodingAvailable(): Boolean {
        return Geocoder.isPresent()
    }
}