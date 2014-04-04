package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_PREF_USER = "pref_user";
	public static final String KEY_PREF_BGColor = "pref_bgcolor";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		
		Preference usPref = findPreference(KEY_PREF_USER);
		usPref.setSummary(getPreferenceManager().getSharedPreferences().getString(KEY_PREF_USER, ""));
		Preference bgPref = findPreference(KEY_PREF_BGColor);
		bgPref.setSummary(((ListPreference)bgPref).getEntry());
		//getPreferenceScreen().getSharedPreferences();
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		Preference pref = findPreference(key);
		if (pref instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) pref;
			if (pref.getKey().equalsIgnoreCase(KEY_PREF_USER)) {
				//pref.setSummary(sharedPreferences.getString(key, "w"));
				pref.setSummary(editTextPref.getText());
			}
			else
				pref.setSummary(editTextPref.getText());
		}
		if (pref instanceof ListPreference){
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
		// if (key.equals(KEY_PREF_USER)) {
		// String username = sharedPreferences.getString(key, "");
		//
		// // Set summary to be the user-description for the selected value
		// EditTextPreference editTextPref = (EditTextPreference)
		// settingsFragment
		// .findPreference(key);
		// editTextPref.setSummary(username);
		// //getFragmentManager().beginTransaction()
		// //.replace(android.R.id.content, new SettingsFragment()).commit();
		//
		//
		// } else if (key.equals(KEY_PREF_BGColor)) {
		// // Preference backgroundColor = findPreference(key);
		// // // Set summary to be the user-description for the selected value
		// // backgroundColor.setSummary(sharedPreferences.getString(key, ""));
		// }

	}

	// @Override
	// public void onResume() {
	// super.onResume();
	// /*if(getPreferenceScreen() != null)
	// if(((BaseAdapter)getPreferenceScreen().getRootAdapter() != null)){
	// ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
	// }*/
	// //getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
	// //getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(
	// // (OnSharedPreferenceChangeListener) this);
	//
	// }
	//
	// @Override
	// public void onPause() {
	// super.onPause();
	// //getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
	//
	// //getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener)
	// this);
	// }
}
