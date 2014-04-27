package no.hiof.android.ambiguous.activities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.fragments.SettingsFragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	public static final String KEY_PREF_USER = "pref_user";
	public static final String KEY_PREF_BGColor = "pref_bgcolor";
	public static final String KEY_PREF_CHEAT = "pref_cheat";
	protected SettingsFragment settingsFragment;
	private OnSharedPreferenceChangeListener listener;
	protected Method mLoadHeaders = null;
	protected Method mHasHeaders = null;
	protected boolean boolTHing = false;

	/**
	 * Checks to see if using new v11+ way of handling PrefsFragments.
	 * 
	 * @return Returns false pre-v11, else checks to see if using headers.
	 */
	public boolean isNewV11Prefs() {
		if (mHasHeaders != null && mLoadHeaders != null) {
			try {
				return (Boolean) mHasHeaders.invoke(this);
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		// onBuildHeaders() will be called during super.onCreate()
		try {
			mLoadHeaders = getClass().getMethod("loadHeadersFromResource",
					int.class, List.class);
			mHasHeaders = getClass().getMethod("hasHeaders");
		} catch (NoSuchMethodException e) {
		}
		super.onCreate(savedInstanceState);
		// This test does not recognize that I am actually using headers (I
		// think)
		if (!isNewV11Prefs()) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				addPreferencesFromResource(R.xml.preferences);
			}

		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			
			//This isnt used??			
			/*String ss = getPreferences(MODE_PRIVATE).getString(KEY_PREF_USER,
					"");*/
			settingsFragment = new SettingsFragment();
			PreferenceManager.setDefaultValues(this, R.xml.pref_headers, false);

			// Display the fragment as the main content.
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, settingsFragment).commit();

		}
	}

	@Override
	public void onBuildHeaders(List<Header> aTarget) {
		try {
			mLoadHeaders.invoke(this, new Object[] { R.xml.pref_headers,
					aTarget });
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				getPreferences(MODE_MULTI_PROCESS)
						.registerOnSharedPreferenceChangeListener(
								SettingsActivity.this);
				return null;
			}

		}.execute();

	}

	@Override
	protected void onPause() {
		super.onPause();

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				getPreferences(MODE_MULTI_PROCESS)
						.unregisterOnSharedPreferenceChangeListener(
								SettingsActivity.this);
				return null;
			}
		};
	}

	// Never called in SettingsActivity, only in SettingsFragment.
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		System.out.println();
	}

}
