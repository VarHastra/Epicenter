package me.alex.pet.apps.epicenter.presentation.common.navigation

import androidx.fragment.app.Fragment

abstract class Destination {
    abstract val fragment: Fragment

    open val tag: String = this::class.java.simpleName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Destination

        if (fragment != other.fragment) return false
        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fragment.hashCode()
        result = 31 * result + tag.hashCode()
        return result
    }

    override fun toString(): String {
        return "Destination(fragment=$fragment, tag='$tag')"
    }
}