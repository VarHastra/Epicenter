package me.alex.pet.apps.epicenter.presentation.common.navigation

import androidx.fragment.app.Fragment

abstract class Destination {
    abstract val fragment: Fragment

    open val tag: String = this::class.java.simpleName
}