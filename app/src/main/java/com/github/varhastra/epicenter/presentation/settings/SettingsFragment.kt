package com.github.varhastra.epicenter.presentation.settings


import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.github.varhastra.epicenter.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = getString(R.string.prefs_app_settings)
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
