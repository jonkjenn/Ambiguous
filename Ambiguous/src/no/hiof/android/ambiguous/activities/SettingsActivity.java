package no.hiof.android.ambiguous.activities;

import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.fragments.SettingsFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	public static final String KEY_PREF_USER = "pref_user";
	public static final String KEY_PREF_BGColor = "pref_bgcolor";
	protected SettingsFragment settingsFragment;
	private OnSharedPreferenceChangeListener listener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		
//		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
//			@Override
//			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//				
//				if (key.equals(KEY_PREF_USER)) {
//					String username = sharedPreferences.getString(key, "");
//
//					// Set summary to be the user-description for the selected value
//					EditTextPreference editTextPref = (EditTextPreference) settingsFragment
//							.findPreference(key);
//					editTextPref.setSummary(username);
//					//getFragmentManager().beginTransaction()
//					//.replace(android.R.id.content, new SettingsFragment()).commit();
//					
//
//				} else if (key.equals(KEY_PREF_BGColor)) {
//					// Preference backgroundColor = findPreference(key);
//					// // Set summary to be the user-description for the selected value
//					// backgroundColor.setSummary(sharedPreferences.getString(key, ""));
//				}
//
//			}
//			
//		};
		
		
		// Add a button to the header list.
		/*
		 * if (hasHeaders()) { Button button = new Button(this);
		 * button.setText("Some action"); setListFooter(button); }
		 */
		//String ss = getPreferences(MODE_PRIVATE).getString(KEY_PREF_USER, "");
		settingsFragment = new SettingsFragment();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		/*if (!ss.isEmpty()) {
			EditTextPreference editTextPref = (EditTextPreference) settingsFragment
					.findPreference(KEY_PREF_USER);
			editTextPref.setSummary(ss);
		}*/

		// Display the fragment as the main content.
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, settingsFragment).commit();

	
//		PreferenceScreen ps2 = (PreferenceScreen) findPreference(KEY_PREF_BGColor);
//		if(ps2 != null)
//	    ps2.setOnPreferenceClickListener(this);
	}

	
//	@Override
//	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//		
//		if (key.equals(KEY_PREF_USER)) {
//			String username = sharedPreferences.getString(key, "");
//
//			// Set summary to be the user-description for the selected value
//			EditTextPreference editTextPref = (EditTextPreference) settingsFragment
//					.findPreference(key);
//			editTextPref.setSummary(username);
//			//getFragmentManager().beginTransaction()
//			//.replace(android.R.id.content, new SettingsFragment()).commit();
//			
//
//		} else if (key.equals(KEY_PREF_BGColor)) {
//			// Preference backgroundColor = findPreference(key);
//			// // Set summary to be the user-description for the selected value
//			// backgroundColor.setSummary(sharedPreferences.getString(key, ""));
//		}
//
//	}

	@Override
	protected void onResume() {
		super.onResume();
		/*if(getPreferenceScreen() != null)
		if(((BaseAdapter)getPreferenceScreen().getRootAdapter() != null)){
			((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
		}*/
		//getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
		getPreferences(MODE_MULTI_PROCESS).registerOnSharedPreferenceChangeListener(
				this);
//		PreferenceScreen ps2 = (PreferenceScreen) findPreference(KEY_PREF_BGColor);
//		if(ps2 != null)
//	    ps2.setOnPreferenceClickListener(this);
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		//getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
		
		getPreferences(MODE_MULTI_PROCESS)
				.unregisterOnSharedPreferenceChangeListener(this);
		
//		PreferenceScreen ps2 = (PreferenceScreen) findPreference(KEY_PREF_BGColor);
//		if(ps2 != null)
//	    ps2.setOnPreferenceClickListener(this);
	}

	

	// Never called in SettingsActivity, only in SettingsFragment. Need to work out compatibility for < HONEYCOMB
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		System.out.println();
	}


}
