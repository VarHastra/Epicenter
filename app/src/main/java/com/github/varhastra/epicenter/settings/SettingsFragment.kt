package com.github.varhastra.epicenter.settings


import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.varhastra.epicenter.R

/**
 * Displays app preferences.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
