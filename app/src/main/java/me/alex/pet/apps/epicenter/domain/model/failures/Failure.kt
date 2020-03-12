package me.alex.pet.apps.epicenter.domain.model.failures

import me.alex.pet.apps.epicenter.domain.model.Coordinates

sealed class Failure {

    sealed class NetworkFailure : Failure() {
        object NoConnection : NetworkFailure()
        object BadResponse : NetworkFailure()
        object Unknown : NetworkFailure()
    }

    sealed class EventsFailure : Failure() {
        data class NoSuchEvent(val eventId: String) : EventsFailure()
    }

    sealed class PlacesFailure : Failure() {
        data class NoSuchPlace(val placeId: Int) : PlacesFailure()
    }

    sealed class LocationFailure : Failure() {
        object PermissionDenied : LocationFailure()
        object NotAvailable : LocationFailure()
        object Unknown : LocationFailure()
        data class ProviderFailure(val t: Throwable) : LocationFailure()
    }

    sealed class GeocoderFailure : Failure() {
        object NotAvailable : GeocoderFailure()
        data class UnableToGeocode(val coordinates: Coordinates) : GeocoderFailure()
    }
}