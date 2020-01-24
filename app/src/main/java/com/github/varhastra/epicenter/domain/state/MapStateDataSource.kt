package com.github.varhastra.epicenter.domain.state

import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel

interface MapStateDataSource {

    var cameraState: CameraState

    var numberOfDaysToShow: Int

    var minMagnitude: MagnitudeLevel
}