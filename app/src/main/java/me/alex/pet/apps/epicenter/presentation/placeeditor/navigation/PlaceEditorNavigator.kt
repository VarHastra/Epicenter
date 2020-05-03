package me.alex.pet.apps.epicenter.presentation.placeeditor.navigation

import me.alex.pet.apps.epicenter.presentation.common.navigation.NavigationCommand

interface PlaceEditorNavigator {

    fun processNavCommand(command: NavigationCommand)
}