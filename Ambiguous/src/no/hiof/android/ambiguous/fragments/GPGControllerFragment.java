package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.activities.GameActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GPGControllerFragment extends Fragment {

	GooglePlayGameFragment gPGHandler;
	boolean gPGSVisible = false;
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("gPGVisible", gPGSVisible);
	}

	 @Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		gPGSVisible = savedInstanceState.getBoolean("gPGVisible", false);
	}

	@Override
	public void onStart() {
		super.onStart();

		int e = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getActivity());

		if (e != ConnectionResult.SUCCESS) {
			GooglePlayServicesUtil.getErrorDialog(e, getActivity(), 0).show();
		}

		gPGHandler = (GooglePlayGameFragment) getActivity()
				.getSupportFragmentManager().findFragmentByTag("gpg");
		if (gPGHandler == null) {
			showGooglePlayGameFragment();
		} else if (!gPGSVisible) {
			hideGooglePlayGameFragment();
		}
	}

	/**
	 * Make the fragment visible or create a new if does not exist.
	 */
	void showGooglePlayGameFragment() {
		gPGSVisible = true;
		FragmentManager manager = getActivity().getSupportFragmentManager();
		gPGHandler = (GooglePlayGameFragment) manager.findFragmentByTag("gpg");
		FragmentTransaction transaction = manager.beginTransaction();
		if (gPGHandler == null) {
			gPGHandler = new GooglePlayGameFragment();
			transaction.add(R.id.game_layout_container, gPGHandler, "gpg");
		} else {
			transaction.show(manager.findFragmentByTag("gpg"));
		}
		transaction.commit();
	}

	/**
	 * Temporarily hide the fragment
	 */
	void hideGooglePlayGameFragment() {
		FragmentManager manager = getActivity().getSupportFragmentManager();
		manager.popBackStack("GPG", FragmentManager.POP_BACK_STACK_INCLUSIVE);
		FragmentTransaction t = manager.beginTransaction();
		Fragment f = manager.findFragmentByTag("gpg");
		if (f != null) {
			gPGSVisible = false;
			t.hide(f).addToBackStack("GPG");
			t.commit();
		}
	}

	/**
	 * Close the fragment for good.
	 */
	void closeGooglePlayGameFragment() {
		gPGSVisible = false;
		FragmentManager manager = getActivity().getSupportFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		t.remove(manager.findFragmentByTag("gpg"));
		t.commit();
		gPGHandler = null;
	}

	void startGameMachine() {

		GameActivity.gameMachine.delay = 50;
	}
}
