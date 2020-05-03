package me.alex.pet.apps.epicenter.presentation.placeeditor.navigation

import androidx.fragment.app.Fragment
import me.alex.pet.apps.epicenter.presentation.common.navigation.Destination
import me.alex.pet.apps.epicenter.presentation.placeeditor.locationpicker.LocationPickerFragment
import me.alex.pet.apps.epicenter.presentation.placeeditor.namepicker.NamePickerFragment

class PlaceEditorDestinations private constructor() {

    class LocationPicker : Destination() {
        override val fragment: Fragment = LocationPickerFragment.newInstance()
    }

    class NamePicker : Destination() {
        override val fragment: Fragment = NamePickerFragment.newInstance()
    }
}