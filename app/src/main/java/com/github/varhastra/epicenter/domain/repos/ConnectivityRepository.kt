package com.github.varhastra.epicenter.domain.repos

interface ConnectivityRepository {

    fun isNetworkConnected(): Boolean
}