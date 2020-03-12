package me.alex.pet.apps.epicenter.domain.repos

interface ConnectivityRepository {

    fun isNetworkConnected(): Boolean
}