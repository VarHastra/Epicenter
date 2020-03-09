package com.github.varhastra.epicenter.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.common.functionaltypes.Either
import com.github.varhastra.epicenter.common.functionaltypes.flatMap
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.failures.Failure
import com.github.varhastra.epicenter.domain.model.failures.Failure.GeocoderFailure
import com.github.varhastra.epicenter.domain.model.failures.Failure.LocationFailure
import com.github.varhastra.epicenter.domain.repos.LocationRepository
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.threeten.bp.Duration
import timber.log.Timber
import kotlin.coroutines.resume

class LocationProvider(val context: Context = App.instance) : LocationRepository {

    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val settingsClient = LocationServices.getSettingsClient(context)

    private val geocoder = Geocoder(context)

    private val oneTimeLocationRequest
        get() = LocationRequest.create().apply {
            numUpdates = 1
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            setExpirationDuration(Duration.ofSeconds(5).toMillis())
        }

    override val isLocationPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override suspend fun getCoordinates(): Either<Coordinates, Failure> {
        if (!isLocationPermissionGranted) return Either.failure(LocationFailure.PermissionDenied)
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

    override suspend fun getLastCoordinates(): Either<Coordinates, Failure> {
        if (!isLocationPermissionGranted) return Either.failure(LocationFailure.PermissionDenied)
        return getLastLocation().map { it.toCoordinates() }
    }

    private suspend fun getLastLocation(): Either<Location, Failure> {
        return try {
            val coordinates = locationProviderClient.lastLocation.await()
            Either.of(coordinates) {
                Timber.w("getLastLocation() > FusedLocationProvider.getLastLocation() has returned null.")
                LocationFailure.NotAvailable
            }
        } catch (t: Throwable) {
            Timber.w(t, "getLastLocation() > Exception encountered.")
            Either.Failure(LocationFailure.ProviderFailure(t))
        }
    }

    private suspend fun getFreshCoordinates(): Either<Coordinates, Failure> {
        return getFreshLocation().map { it.toCoordinates() }
    }

    private suspend fun getFreshLocation(): Either<Location, Failure> = withContext(Dispatchers.Main) {
        val locationRequest = oneTimeLocationRequest
        val settingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(false)
                .setNeedBle(false)
                .build()
        getLocationSettings(settingsRequest)
                .flatMap { getOneTimeLocationUpdate(locationRequest) }
    }

    private suspend fun getLocationSettings(settingsRequest: LocationSettingsRequest): Either<LocationSettingsResponse, Failure> {
        return try {
            val settingsResponse = settingsClient.checkLocationSettings(settingsRequest).await()
            Either.Success(settingsResponse)
        } catch (t: Throwable) {
            Timber.w(t, "getLocationSettings() > Exception encountered.")
            Either.Failure(LocationFailure.ProviderFailure(t))
        }
    }

    private suspend fun getOneTimeLocationUpdate(locationRequest: LocationRequest): Either<Location, Failure> = suspendCancellableCoroutine { continuation ->
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                locationProviderClient.removeLocationUpdates(this)
                continuation.resume(Either.of(lastLocation) { LocationFailure.NotAvailable })
            }
        }

        continuation.invokeOnCancellation { locationProviderClient.removeLocationUpdates(locationCallback) }
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override suspend fun getLocationName(coordinates: Coordinates): Either<String, Failure> {
        if (!Geocoder.isPresent()) {
            return Either.Failure(GeocoderFailure.NotAvailable)
        }

        val (lat, lon) = coordinates
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        return if (addresses.isEmpty()) {
            Timber.w("getLocationName() > Geocoder.getFromLocation() has returned an empty address list.")
            Either.Failure(GeocoderFailure.UnableToGeocode(coordinates))
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


    companion object {
        private val LOCATION_OBSOLESCENCE_THRESHOLD_NS = Duration.ofMinutes(5).toNanos()
    }
}


private fun Location.toCoordinates() = Coordinates(this.latitude, this.longitude)