package com.github.varhastra.epicenter.domain.state

interface MapStateDataSource {

    fun saveMapState(mapState: MapState)

    fun getMapState(): MapState
}