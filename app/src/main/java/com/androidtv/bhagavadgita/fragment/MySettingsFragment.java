package com.androidtv.bhagavadgita.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.leanback.preference.LeanbackSettingsFragment;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.calendar.Language;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;

public class MySettingsFragment extends LeanbackSettingsFragment
        implements DialogPreference.TargetFragment {

    private static PreferenceFragment mPreferenceFragment;
    private final static String PREFERENCE_RESOURCE_ID = "preferenceResource";
    private final static String PREFERENCE_ROOT = "root";

    @Override
    public void onPreferenceStartInitialScreen() {
        mPreferenceFragment = buildPreferenceFragment(R.xml.settings, null);
        startPreferenceFragment(mPreferenceFragment);
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference) {
        PreferenceFragment frag = buildPreferenceFragment(R.xml.settings, preference.getKey());
        startPreferenceFragment(frag);
        return true;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment preferenceFragment, PreferenceScreen preferenceScreen) {
        PreferenceFragment frag = buildPreferenceFragment(R.xml.settings, preferenceScreen.getKey());
        startPreferenceFragment(frag);
        return true;
    }

    @Override
    public Preference findPreference(CharSequence charSequence) {
        return mPreferenceFragment.findPreference(charSequence);
    }

    private PreferenceFragment buildPreferenceFragment(int preferenceResId, String root) {
        PreferenceFragment fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putInt(PREFERENCE_RESOURCE_ID, preferenceResId);
        args.putString(PREFERENCE_ROOT, root);
        fragment.setArguments(args);
        return fragment;
    }

    public static class PrefFragment extends LeanbackPreferenceFragment {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            String root = getArguments().getString(PREFERENCE_ROOT, null);
            int prefResId = getArguments().getInt(PREFERENCE_RESOURCE_ID);
            if (root == null) {
                addPreferencesFromResource(prefResId);
            } else {
                setPreferencesFromResource(prefResId, root);
            }

            showMediaDecoder();
            showAutoplaySwitch();
            showCalendarEvents();
        }

        private void showMediaDecoder() {
            ListPreference keyLanguage = (ListPreference) findPreference("keyLanguage");
            if (keyLanguage != null) {
                String[] languageType = getResources().getStringArray(R.array.listArray);

                // Ensure both entries and entryValues are defined and match
                keyLanguage.setEntries(languageType);
                keyLanguage.setEntryValues(languageType); // Or use R.array.listValues if separate

                String selectedLanguage = SharePreferenceManager.getString("LANGUAGE");
                if (selectedLanguage == null || selectedLanguage.isEmpty()) {
                    keyLanguage.setValue(languageType[0]);
                } else {
                    keyLanguage.setValue(selectedLanguage);
                }

                keyLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        SharePreferenceManager.save("LANGUAGE", newValue.toString());
                        getActivity().finish();

                        return false;
                    }
                });
            }
        }

        private void showAutoplaySwitch() {
            SwitchPreference keyAutoplay = (SwitchPreference) findPreference("keyAutoplay");
            if (keyAutoplay != null) {

                boolean savedValue = SharePreferenceManager.getBoolean("AUTOPLAY", true);
                keyAutoplay.setChecked(savedValue);

                keyAutoplay.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean enabled = (Boolean) newValue;
                        SharePreferenceManager.save("AUTOPLAY", enabled);
                        return true; // true = accept the new checked state; your ListPreference returns false intentionally since it restarts the app instead
                    }
                });
            }
        }

        private void showCalendarEvents() {
            SwitchPreference keyCalendarEvents = (SwitchPreference) findPreference("keyCalendarEvents");
            if (keyCalendarEvents != null) {

                boolean savedValue = SharePreferenceManager.getBoolean("CAL_EVENTS", true);
                keyCalendarEvents.setChecked(savedValue);

                keyCalendarEvents.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean enabled = (Boolean) newValue;
                        SharePreferenceManager.save("CAL_EVENTS", enabled);
                        return true; // true = accept the new checked state; your ListPreference returns false intentionally since it restarts the app instead
                    }
                });
            }
        }
    }
}

