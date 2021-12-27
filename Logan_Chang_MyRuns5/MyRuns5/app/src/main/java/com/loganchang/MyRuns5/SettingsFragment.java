package com.loganchang.MyRuns5;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;


public class SettingsFragment extends PreferenceFragmentCompat {
    /**
     * Load saved preferences
     * @param savedInstanceState    SavedInstanceState Bundle
     * @param rootKey               Root Key
     */
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //load preferences from xml
        setPreferencesFromResource(R.xml.preference, rootKey);
    }
}