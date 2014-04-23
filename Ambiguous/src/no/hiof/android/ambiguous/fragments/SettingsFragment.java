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

		// Load the preferences from an XML resource
//		Context anAct = getActivity().getApplicationContext();
//        int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("pref-resource"),
//                "xml",anAct.getPackageName());
		//addPreferencesFromResource(thePrefRes);
        addPreferencesFromResource(R.xml.preferences);
		
		SharedPreferences sp = getPreferenceManager().getSharedPreferences();
		Preference usPref = findPreference(KEY_PREF_USER);
		usPref.setSummary(sp.getString(KEY_PREF_USER, ""));
		Preference bgPref = findPreference(KEY_PREF_BGColor);
		bgPref.setSummary(((ListPreference)bgPref).getEntry());
		NumberPickerDialogPreference chtPref = (NumberPickerDialogPreference)findPreference(KEY_PREF_CHEAT);
		int nind = sp.getInt(KEY_PREF_CHEAT, 0);
		chtPref.setSummary(String.valueOf(nind));
		
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
		if(pref instanceof NumberPickerDialogPreference){
			NumberPickerDialogPreference numberPref = (NumberPickerDialogPreference) pref;
			pref.setSummary(String.valueOf(numberPref.getValue()));
		}

	}
}
