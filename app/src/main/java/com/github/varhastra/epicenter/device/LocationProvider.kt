package com.github.varhastra.epicenter.device

import android.content.Context
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.domain.DataSourceCallback
import com.github.varhastra.epicenter.domain.LocationDataSource
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Position
import com.google.android.gms.location.LocationServices
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.warn

class LocationProvider(val context: Context = App.instance) : LocationDataSource {

    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val logger = AnkoLogger(this.javaClass)

    override fun getLastLocation(callback: DataSourceCallback<Position>) {
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
}