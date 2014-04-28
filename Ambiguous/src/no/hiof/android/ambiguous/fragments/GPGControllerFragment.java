package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.DelayedStart;
import no.hiof.android.ambiguous.DelayedStart.OnReadyTostartListener;
import no.hiof.android.ambiguous.GPGCallbackInterface;
import no.hiof.android.ambiguous.GPGService;
import no.hiof.android.ambiguous.GPGService.GPGBinder;
import no.hiof.android.ambiguous.GameMachine.OnStateChangeListener;
import no.hiof.android.ambiguous.GameMachine.State;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.activities.GameActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Et abstraksjonslag mellom GameActivity og GPG delen. Håndterer visning av GPG
 * meny.
 */
public class GPGControllerFragment extends Fragment implements
		GPGCallbackInterface, GooglePlayGameFragment.OnGPGConnectedListener,
		OnStateChangeListener, GameActivity.OnActivityResultListener,
		OnReadyTostartListener {

	GooglePlayGameFragment gPGHandler;
	boolean gPGSVisible = false;
	GPGBinder binder;
	boolean bound = false;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("gPGVisible", gPGSVisible);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check if the GPG fragment should be visible
		if (savedInstanceState != null) {
			gPGSVisible = savedInstanceState.getBoolean("gPGVisible", false);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// We use a delayed start to make sure the game is setup correctly
		// before we start. This might not be needed anymore because of changes
		// made but keep it until have time to test it differently.
		new DelayedStart(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);



		gPGHandler = (GooglePlayGameFragment) getActivity()
				.getSupportFragmentManager().findFragmentByTag("gpg");

		if (getArguments().containsKey("matchId")) {
			gPGSVisible = false;
		}

		if (gPGHandler == null) {
			createGooglePlayFragment(getArguments());
		} else {
			gPGHandler.setGPGConnectedListener(this);
		}

		if (gPGSVisible) {
			showGPGFragment();
		} else {
			hideGPGFragment();
		}
	}

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			bound = true;
			binder = (GPGBinder) service;
			binder.setActivityCallback(GPGControllerFragment.this);
			binder.setGPGServiceListener(gPGHandler);
		}
	};

	/**
	 * Create new GPG Fragment.
	 */
	void createGooglePlayFragment(Bundle arguments) {
		gPGSVisible = true;
		FragmentManager manager = getActivity().getSupportFragmentManager();
		Fragment f = manager.findFragmentByTag("gpg");
		if (f == null) {
			FragmentTransaction transaction = manager.beginTransaction();
			gPGHandler = new GooglePlayGameFragment();
			gPGHandler.setArguments(arguments);
			transaction.add(R.id.game_layout_container, gPGHandler, "gpg");
			transaction.commit();
		} else {
			gPGHandler = (GooglePlayGameFragment) f;
		}
		gPGHandler.setGPGConnectedListener(this);
	}

	/**
	 * Show hidden GPG Fragment.
	 */
	void showGPGFragment() {
		if (getActivity() == null) {
			return;
		}
		FragmentManager manager = getActivity().getSupportFragmentManager();

		Fragment f = manager.findFragmentByTag("gpg");
		if (f == null) {
			return;
		}

		try {
			gPGHandler = (GooglePlayGameFragment) f;
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.show(f);
			transaction.commit();
		} catch (IllegalStateException e) {

		}
	}

	/**
	 * Temporarily hide the fragment
	 */
	void hideGPGFragment() {
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
		Fragment f = manager.findFragmentByTag("gpg");

		// Probably do not have to remove this since we're destroying it anyway
		if (f != null) {
			((GooglePlayGameFragment) f).setGPGConnectedListener(null);
		}

		t.remove(f);
		// The state is not important since it's all on Google server anyway.
		t.commitAllowingStateLoss();
		gPGHandler = null;
	}

	void startGameMachine() {
		GameActivity.gameMachine.delay = 50;
	}

	@Override
	public void onGPGPConnected(GoogleApiClient client) {
		if (binder != null) {
			binder.setGoogleApiClient(client);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (binder != null) {
			binder.setGPGServiceListener(null);
			getActivity().unbindService(connection);
		}
	}

	@Override
	public void onStateChanged(State state) {
		/*
		 * if (state == State.GAME_OVER) { showGPGFragment(); } else {
		 */
		hideGPGFragment();
	}

	@Override
	public void onGameActivityResult(int requestCode, int resultCode,
			Intent data) {
		if (gPGHandler != null) {
			gPGHandler.onGameActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (GameActivity.gameMachine != null) {
			GameActivity.gameMachine.removeOnStateChangedListener(this);
		}
	}

	@Override
	public void startLoad() {
		GameActivity.gameMachine.setOnStateChangeListener(this);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(getActivity()
						.getApplicationContext());

		if (sp.contains(SettingsFragment.KEY_PREF_GPGService)
				&& sp.getBoolean(SettingsFragment.KEY_PREF_GPGService, false)) {
			Intent i = new Intent(getActivity(), GPGService.class);
			getActivity().startService(i);
			getActivity().bindService(i, connection, 0);
		}
	}

	@Override
	public void gaveUp() {
		// TODO Auto-generated method stub

	}

}