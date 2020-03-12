package me.alex.pet.apps.epicenter.domain.state

import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel

interface MapStateDataSource {

    var cameraState: CameraState

    var numberOfDaysToShow: Int

    var minMagnitude: MagnitudeLevel
}