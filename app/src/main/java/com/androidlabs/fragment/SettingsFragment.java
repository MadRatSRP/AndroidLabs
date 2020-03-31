package com.androidlabs.fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.androidlabs.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
