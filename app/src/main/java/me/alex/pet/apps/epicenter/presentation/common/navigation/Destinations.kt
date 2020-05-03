package me.alex.pet.apps.epicenter.presentation.common.navigation

import androidx.fragment.app.Fragment
import me.alex.pet.apps.epicenter.presentation.details.DetailsFragment
import me.alex.pet.apps.epicenter.presentation.main.MainFragment
import me.alex.pet.apps.epicenter.presentation.placeeditor.PlaceEditorFragment
import me.alex.pet.apps.epicenter.presentation.places.PlacesFragment
import me.alex.pet.apps.epicenter.presentation.settings.SettingsFragment

class Destinations private constructor() {
    class Main : Destination() {
        override val fragment: Fragment = MainFragment.newInstance()

        override val tag: String
            get() = "MAIN"
    }

    class Details(private val eventId: String) : Destination() {
        override val fragment: Fragment = DetailsFragment.newInstance(eventId)

        override val tag: String
            get() = "DETAILS"
    }

    class Places : Destination() {
        override val fragment: Fragment = PlacesFragment.newInstance()

        override val tag: String
            get() = "PLACES"
    }

    class PlaceEditor(placeId: Int?) : Destination() {
        override val fragment: Fragment = PlaceEditorFragment.newInstance(placeId)
    }

    class Settings : Destination() {
        override val fragment: Fragment = SettingsFragment.newInstance()

        override val tag: String
            get() = "SETTINGS"
    }
}