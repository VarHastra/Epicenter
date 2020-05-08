package me.alex.pet.apps.epicenter.presentation

import androidx.fragment.app.Fragment
import me.alex.pet.apps.epicenter.presentation.common.navigation.Destination
import me.alex.pet.apps.epicenter.presentation.details.DetailsFragment
import me.alex.pet.apps.epicenter.presentation.main.MainFragment
import me.alex.pet.apps.epicenter.presentation.placeeditor.PlaceEditorFragment
import me.alex.pet.apps.epicenter.presentation.places.PlacesFragment
import me.alex.pet.apps.epicenter.presentation.settings.SettingsFragment

class Destinations private constructor() {
    class Main : Destination() {
        override fun newFragment(): Fragment = MainFragment.newInstance()
    }

    class Details(private val eventId: String) : Destination() {
        override fun newFragment(): Fragment = DetailsFragment.newInstance(eventId)
    }

    class Places : Destination() {
        override fun newFragment(): Fragment = PlacesFragment.newInstance()
    }

    class PlaceEditor(private val placeId: Int?) : Destination() {
        override fun newFragment(): Fragment = PlaceEditorFragment.newInstance(placeId)
    }

    class Settings : Destination() {
        override fun newFragment(): Fragment = SettingsFragment.newInstance()
    }
}