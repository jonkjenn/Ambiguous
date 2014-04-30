package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.NumberPickerDialogPreference;
import no.hiof.android.ambiguous.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_PREF_USER = "pref_user";
	public static final String KEY_PREF_BGColor = "pref_bgcolor";
	public static final String KEY_PREF_CHEAT = "pref_cheat";
	public static final String KEY_PREF_GPGService = "pref_GPGService";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sp = getPreferenceManager().getSharedPreferences();

		Preference usPref = findPreference(KEY_PREF_USER);
		usPref.setSummary(sp.getString(KEY_PREF_USER, ""));
		Preference bgPref = findPreference(KEY_PREF_BGColor);
		bgPref.setSummary(((ListPreference) bgPref).getEntry());
		NumberPickerDialogPreference chtPref = (NumberPickerDialogPreference) findPreference(KEY_PREF_CHEAT);
		int additionalDmg = sp.getInt(KEY_PREF_CHEAT, 0);
		chtPref.setSummary(String.valueOf(additionalDmg));

	}

	@Override
	public void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	/**
	 * When a setting is changed, find the Preference and update it's summary
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		Preference pref = findPreference(key);
		if (pref instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) pref;
			pref.setSummary(editTextPref.getText());
		}
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
		if (pref instanceof NumberPickerDialogPreference) {
			NumberPickerDialogPreference numberPref = (NumberPickerDialogPreference) pref;
			pref.setSummary(String.valueOf(numberPref.getValue()));
		}

	}
}
