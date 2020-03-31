package com.androidlabs.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.androidlabs.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        addPreferencesFromResource(R.xml.preferences)
    }
}