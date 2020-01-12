package com.github.varhastra.epicenter.device

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.Position
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.github.varhastra.epicenter.domain.repos.RepositoryCallback
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import org.jetbrains.anko.*
import timber.log.Timber

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

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Either<Coordinates, Throwable> {
        return try {
            val coordinates = locationProviderClient.lastLocation.await()?.toCoordinates()
            Either.of(coordinates) {
                Timber.w("getLastLocation() > FusedLocationProvider.getLastLocation() has returned null.")
                NoLocationAvailableException()
            }
        } catch (e: Throwable) {
            Timber.w(e, "getLastLocation() > Exception encountered.")
            Either.Failure(e)
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

    override suspend fun getLocationName(coordinates: Coordinates): Either<String, Throwable> {
        val (lat, lon) = coordinates
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        return if (addresses.isEmpty()) {
            Timber.w("getLocationName() > Geocoder.getFromLocation() has returned an empty address list.")
            Either.Failure(EmptyAddressListException())
        } else {
            val address = addresses[0]
            Timber.d("getLocationName() > received address: $address.")
            val addrString = address.locality?.plus(", ").orEmpty() +
                    address.adminArea?.plus(", ").orEmpty() +
                    address.countryCode.orEmpty()
            Either.Success(addrString)
        }
    }

    override fun isGeoCodingAvailable(): Boolean {
        return Geocoder.isPresent()
    }


    class NoLocationAvailableException(
            message: String = "No location available.",
            cause: Throwable? = null
    ) : RuntimeException(message, cause)

    class EmptyAddressListException(
            message: String = "Empty address list.",
            cause: Throwable? = null
    ) : RuntimeException(message, cause)
}