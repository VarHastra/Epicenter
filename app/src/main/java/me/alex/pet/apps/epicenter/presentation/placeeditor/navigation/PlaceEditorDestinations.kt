package me.alex.pet.apps.epicenter.presentation.placeeditor.navigation

import androidx.fragment.app.Fragment
import me.alex.pet.apps.epicenter.presentation.common.navigation.Destination
import me.alex.pet.apps.epicenter.presentation.placeeditor.locationpicker.LocationPickerFragment
import me.alex.pet.apps.epicenter.presentation.placeeditor.namepicker.NamePickerFragment

interface PlaceEditorDestinations {

    class LocationPicker : Destination() {
        override val fragment: Fragment
            get() = LocationPickerFragment.newInstance()
    }

    class NamePicker : Destination() {
        override val fragment: Fragment
            get() = NamePickerFragment.newInstance()
    }
}