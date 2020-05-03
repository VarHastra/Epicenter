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
        override val fragment: Fragment = MainFragment.newInstance()
    }

    class Details(eventId: String) : Destination() {
        override val fragment: Fragment = DetailsFragment.newInstance(eventId)
    }

    class Places : Destination() {
        override val fragment: Fragment = PlacesFragment.newInstance()
    }

    class PlaceEditor(placeId: Int?) : Destination() {
        override val fragment: Fragment = PlaceEditorFragment.newInstance(placeId)
    }

    class Settings : Destination() {
        override val fragment: Fragment = SettingsFragment.newInstance()
    }
}