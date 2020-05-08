package me.alex.pet.apps.epicenter.presentation.common.navigation

sealed class NavigationCommand {
    data class To(val destination: Destination) : NavigationCommand()
    object Back : NavigationCommand()
    object FinishFlow : NavigationCommand()
}