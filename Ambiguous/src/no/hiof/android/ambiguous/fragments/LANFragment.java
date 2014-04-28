package no.hiof.android.ambiguous.fragments;

import java.net.ServerSocket;
import java.net.Socket;

import no.hiof.android.ambiguous.GameMachine.OnPlayerDeadListener;
import no.hiof.android.ambiguous.GameMachine.State;
import no.hiof.android.ambiguous.Helper;
import no.hiof.android.ambiguous.NetworkOpponent;
import no.hiof.android.ambiguous.OnNetworkErrorListener;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.model.Player;
import no.hiof.android.ambiguous.network.CloseServerSocketTask;
import no.hiof.android.ambiguous.network.CloseSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask.OpenSocketListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * Handles setting up for network games. LAN network games are limited in that
 * event changing screen orientation will break the game.
 * 
 */
public class LANFragment extends Fragment implements OpenSocketListener,
		OnNetworkErrorListener, OnPlayerDeadListener {
	private boolean isServer;
	private Socket socket;
	private ServerSocket server;

	private OpenSocketTask openSocketTask;

	private AlertDialog waitingForNetwork;

	private String address;
	private int port;

	private NetworkOpponent networkOpponent;

	boolean die = false;

	boolean closing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			die = savedInstanceState.getBoolean("die", false);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (die) {
			return;
		}

		Bundle args = getArguments();

		if (!args.containsKey("address") || !args.containsKey("port")
				|| !args.containsKey("isServer")) {
			Log.e("ambiguous", "Need these arguments");
		}

		address = args.getString("address");
		port = args.getInt("port");
		isServer = args.getBoolean("isServer", false);

		startNetwork();
	}

	/**
	 * Launches the network connection to opponent.
	 */
	private void startNetwork() {
		openSocketTask = new OpenSocketTask().setup(address, port, isServer);
		openSocketTask.execute(this);

		lostConnectionDialog();
	}

	void lostConnectionDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String connectMsg = String.format(
				getResources().getString(
						(isServer ? R.string.network_server_listening_text
								: R.string.network_client_connecting_text)),
				address, port);
		builder.setTitle(R.string.connect).setMessage(connectMsg)
				.setNegativeButton(R.string.abort, new OnClickListener() {

					// Close the activity.
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		waitingForNetwork = builder.create();
		waitingForNetwork.show();
	}

	@Override
	public void onStop() {
		super.onStop();
		closeSockets();
		removeListeners();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("die", true);
	}

	@Override
	public void onOpenSocketListener(Socket socket, ServerSocket server,
			Exception exception) {
		if (server != null) {
			this.server = server;
		}
		if (socket != null) {
			// Hide the waiting for network dialog and show message that we're
			// connected.
			waitingForNetwork.cancel();
			Toast t = Toast.makeText(getActivity(), R.string.connected,
					Toast.LENGTH_LONG);
			t.show();
			// When start as a network game, the game will wait for this until
			// it start.
			this.socket = socket;
			onConnected();
		}
	}

	/*
	 * What we do when connected.
	 */
	void onConnected() {
		setupOpponent();
		setupListeners();
		startGameMachine();
	}

	void startGameMachine() {
		GameActivity.gameMachine.delay = 50;

		// If not server we just wait for server.
		if (isServer) {
			GameActivity.gameMachine.startRandom();
		} else {
			GameActivity.gameMachine.startGame(State.OPPONENT_TURN);
		}
	}

	/**
	 * Sets up the generic OpponenController and the NetworkOpponent which
	 * handles network communication.
	 */
	void setupOpponent() {
		networkOpponent = new NetworkOpponent(GameActivity.opponentController,
				GameActivity.gameMachine.player,
				GameActivity.gameMachine.opponent, socket);
		networkOpponent.setOnNetworkErrorListener(this);
		networkOpponent.start();
	}

	/**
	 * Adds all the listeners
	 */
	void setupListeners() {
		GameActivity.gameMachine.setGameMachineListener(networkOpponent);
		GameActivity.gameMachine.setOnPlayerDeadListener(this);
	}

	/**
	 * Removes all the listeners
	 */
	void removeListeners() {
		GameActivity.gameMachine.removeGameMachineListener(networkOpponent);
		GameActivity.gameMachine.removeOnPlayerDeadListener(this);
	}

	/**
	 * Close the network sockets.
	 */
	private void closeSockets() {
		closing = true;
		if (openSocketTask != null) {
			openSocketTask.cancel(true);
		}

		if (socket != null) {
			new CloseSocketTask().execute(this.socket);
		}
		if (server != null) {
			new CloseServerSocketTask().execute(this.server);
		}
	}

	void reconnect() {

	}

	@Override
	public void onNetworkError(String error) {
		// Avoid displaying errors when we're already leaving.
		if (closing) {
			return;
		}
		Helper.showError(R.string.network_error_abort_game, getActivity(),
				new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						// We want to close the activity if there's any network
						// error.
						getActivity().finish();
					}
				});
	}

	@Override
	public void onPlayerDeadListener(Player player) {
		closeSockets();
	}

	@Override
	public void onOpponentDeadListener(Player opponent) {
		closeSockets();
	}

}
