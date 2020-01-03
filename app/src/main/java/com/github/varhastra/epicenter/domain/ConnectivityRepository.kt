package com.github.varhastra.epicenter.domain

interface ConnectivityRepository {

    fun isNetworkConnected(): Boolean
}