package com.github.varhastra.epicenter.device

import android.location.Location
import com.github.varhastra.epicenter.domain.model.Coordinates

fun Location.toCoordinates() = Coordinates(this.latitude, this.longitude)