package me.alex.pet.apps.epicenter.presentation.common.navigation

interface Navigator {
    fun navigateTo(destination: Destination)

    fun navigateBack()
}