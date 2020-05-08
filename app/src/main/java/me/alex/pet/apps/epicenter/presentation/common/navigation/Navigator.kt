package me.alex.pet.apps.epicenter.presentation.common.navigation

interface Navigator {
    fun processNavCommand(command: NavigationCommand)
}