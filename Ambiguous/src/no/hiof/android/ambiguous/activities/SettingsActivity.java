package no.hiof.android.ambiguous.activities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.fragments.SettingsFragment;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

	public static final String KEY_PREF_USER = "pref_user";
	public static final String KEY_PREF_BGColor = "pref_bgcolor";
	public static final String KEY_PREF_CHEAT = "pref_cheat";
	protected SettingsFragment settingsFragment;
	protected Method mLoadHeaders = null;
	protected Method mHasHeaders = null;

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

	// We use deprecated method only for older implementation.
	@SuppressWarnings("deprecation")
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
		// This test does not recognize that I am actually using headers
		// (Or it might just be, seeing as I only have one category,
		// I'm technically not using headers)
		if (!isNewV11Prefs()) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				addPreferencesFromResource(R.xml.preferences);
			}
		}

		// The effect is the same as intended: if using old version,
		// run code above, if using newer version, run code below
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

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
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	// Note: Removed imported onSharedPreferenceChanged method.
	// We do not use it for older build versions, therefore
	// it is never called in SettingsActivity, only in SettingsFragment.

}
