package me.alex.pet.apps.epicenter.presentation.settings


import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import me.alex.pet.apps.epicenter.R

class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.prefs_app_settings)
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
