package me.alex.pet.apps.epicenter.presentation.common.navigation

import androidx.fragment.app.Fragment

abstract class Destination {

    open val tag: String = this::class.java.simpleName

    abstract fun newFragment(): Fragment

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Destination

        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }

    override fun toString(): String {
        return "Destination(tag='$tag')"
    }
}