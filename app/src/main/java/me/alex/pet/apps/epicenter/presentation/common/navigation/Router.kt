package me.alex.pet.apps.epicenter.presentation.common.navigation

interface Router {
    fun navigateTo(destination: Destination)
    fun navigateBack()
    fun attachNavigator(navigator: Navigator)
    fun detachNavigator()
}