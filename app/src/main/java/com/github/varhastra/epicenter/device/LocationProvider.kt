package com.github.varhastra.epicenter.device

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.SystemClock
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.flatMap
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await
import org.threeten.bp.Duration
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationProvider(val context: Context = App.instance) : LocationRepository {

    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val settingsClient = LocationServices.getSettingsClient(context)

    private val geocoder = Geocoder(context)


    override suspend fun getCoordinates(): Either<Coordinates, Throwable> {
        return when (val lastLocationResult = getLastLocation()) {
            is Either.Success -> {
                val location = lastLocationResult.data
                if (location.isObsolete) {
                    getFreshCoordinates()
                } else {
                    Either.success(location.toCoordinates())
                }
            }
            is Either.Failure -> getFreshCoordinates()
        }
    }

    override suspend fun getLastCoordinates(): Either<Coordinates, Throwable> {
        return getLastLocation().map { it.toCoordinates() }
    }

    private suspend fun getLastLocation(): Either<Location, Throwable> {
        return try {
            val coordinates = locationProviderClient.lastLocation.await()
            Either.of(coordinates) {
                Timber.w("getLastLocation() > FusedLocationProvider.getLastLocation() has returned null.")
                NoLocationAvailableException()
            }
        } catch (e: Throwable) {
            Timber.w(e, "getLastLocation() > Exception encountered.")
            Either.Failure(e)
        }
    }

    private suspend fun getFreshCoordinates(): Either<Coordinates, Throwable> {
        return getFreshLocation().map { it.toCoordinates() }
    }

    private suspend fun getFreshLocation(): Either<Location, Throwable> {
        val locationRequest = LocationRequest.create().apply {
            numUpdates = 1
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            setExpirationDuration(Duration.ofSeconds(5).toMillis())
        }
        val settingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(false)
                .setNeedBle(false)
                .build()
        return getLocationSettings(settingsRequest)
                .flatMap { getOneTimeLocationUpdate(locationRequest) }
    }

    private suspend fun getLocationSettings(settingsRequest: LocationSettingsRequest): Either<LocationSettingsResponse, Throwable> {
        return try {
            val settingsResponse = settingsClient.checkLocationSettings(settingsRequest).await()
            Either.Success(settingsResponse)
        } catch (t: Throwable) {
            Timber.w(t, "getLocationSettings() > Exception encountered.")
            Either.Failure(t)
        }
    }

    private suspend fun getOneTimeLocationUpdate(locationRequest: LocationRequest): Either<Location, Throwable> = suspendCoroutine { continuation ->
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                locationProviderClient.removeLocationUpdates(this)
                continuation.resume(Either.of(lastLocation) { NoLocationAvailableException() })
            }
        }

        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override suspend fun getLocationName(coordinates: Coordinates): Either<String, Throwable> {
        if (!Geocoder.isPresent()) {
            return Either.Failure(GeocoderIsNotAvailableException())
        }

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


    private val Location.nanosSinceCreation get() = SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos

    private val Location.isObsolete get() = nanosSinceCreation > LOCATION_OBSOLESCENCE_THRESHOLD_NS


    class NoLocationAvailableException(
            message: String = "No location available.",
            cause: Throwable? = null
    ) : RuntimeException(message, cause)

    class EmptyAddressListException(
            message: String = "Empty address list.",
            cause: Throwable? = null
    ) : RuntimeException(message, cause)

    class GeocoderIsNotAvailableException(
            message: String = "Geocoder is not available.",
            cause: Throwable? = null
    ) : RuntimeException(message, cause)


    companion object {
        private val LOCATION_OBSOLESCENCE_THRESHOLD_NS = Duration.ofMinutes(5).toNanos()
    }
}