package com.github.varhastra.epicenter.domain

interface ConnectivityDataSource {

    fun isNetworkConnected(): Boolean
}