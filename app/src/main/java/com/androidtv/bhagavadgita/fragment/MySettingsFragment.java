package com.androidtv.bhagavadgita.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.leanback.preference.LeanbackListPreferenceDialogFragment;
import androidx.leanback.preference.LeanbackPreferenceFragment;
import androidx.leanback.preference.LeanbackSettingsFragment;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.RecyclerView;

import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;

import org.jspecify.annotations.NonNull;

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

    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragment caller, Preference pref) {
        if (pref instanceof ListPreference) {
            ListDialogFragment f = ListDialogFragment.newInstance(pref.getKey());
            f.setTargetFragment(caller, 0);
            startPreferenceFragment(f);
            return true;
        }
        return super.onPreferenceDisplayDialog(caller, pref);
    }

    public static class ListDialogFragment extends LeanbackListPreferenceDialogFragment {

        public static ListDialogFragment newInstance(String key) {
            ListDialogFragment f = new ListDialogFragment();
            Bundle args = new Bundle();
            args.putString("key", key);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            int color = Color.parseColor(SharePreferenceManager.getString("KEY_THEME_COLOR"));
            view.setBackgroundColor(color);

            RecyclerView list = view.findViewById(android.R.id.list);
            if (list != null) list.setBackgroundColor(color);

            final ColorStateList radioTint = new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{Color.WHITE, Color.WHITE});

            if (list != null) {
                list.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                    @Override
                    public void onChildViewAttachedToWindow(View child) {
                        View button = child.findViewById(androidx.leanback.preference.R.id.button);
                        if (button instanceof CompoundButton) {
                            ((CompoundButton) button).setButtonTintList(radioTint);
                        } else if (button instanceof ImageView) {
                            ((ImageView) button).setImageTintList(radioTint);
                        } else if (button != null) {
                            button.setBackgroundTintList(radioTint);
                        }
                    }

                    @Override
                    public void onChildViewDetachedFromWindow(View child) { }
                });
            }

            TextView title = view.findViewById(R.id.decor_title);
            if (title != null && title.getParent() instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) title.getParent();

                View header = LayoutInflater.from(getActivity())
                        .inflate(R.layout.leanback_preference_fragment, parent, false);
                ((TextView) header.findViewById(R.id.decor_title)).setText(title.getText()); // "Language"
                header.setBackgroundColor(color);

                int index = parent.indexOfChild(title);
                title.setVisibility(View.GONE);
                parent.addView(header, index);
            }

            TextView decorSummary = view.findViewById(R.id.decor_summary);
            if (decorSummary != null) {
                decorSummary.setText("v1.0");
            }
        }
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
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            String activeColor = SharePreferenceManager.getString("KEY_THEME_COLOR");
            view.setBackgroundColor(Color.parseColor(activeColor));

            final TextView decorSummary = view == null
                    ? null : (TextView) view.findViewById(R.id.decor_summary);

            if (decorSummary != null) {
                decorSummary.setText("v1.0");
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            String root = getArguments().getString(PREFERENCE_ROOT, null);
            int prefResId = getArguments().getInt(PREFERENCE_RESOURCE_ID);
            if (root == null) {
                addPreferencesFromResource(prefResId);
            } else {
                setPreferencesFromResource(prefResId, root);
            }

            showAutoplaySwitch();
            showCalendarEvents();
            showCalendarAP();
            showLanguage();
        }

        private void showLanguage() {
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
                        keyLanguage.setValue(newValue.toString());
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

        private void showCalendarAP() {
            SwitchPreference keyCalendarAP = (SwitchPreference) findPreference("keyCalendarAP");
            if (keyCalendarAP != null) {

                boolean savedValue = SharePreferenceManager.getBoolean("CAL_AP", true);
                keyCalendarAP.setChecked(savedValue);

                keyCalendarAP.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean enabled = (Boolean) newValue;
                        SharePreferenceManager.save("CAL_AP", enabled);
                        return true; // true = accept the new checked state; your ListPreference returns false intentionally since it restarts the app instead
                    }
                });
            }
        }
    }
}

