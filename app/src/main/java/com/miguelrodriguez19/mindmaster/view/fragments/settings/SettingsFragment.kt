package com.miguelrodriguez19.mindmaster.view.fragments.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.miguelrodriguez19.mindmaster.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}