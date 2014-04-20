package no.hiof.android.ambiguous.fragments;

import java.net.ServerSocket;
import java.net.Socket;

import no.hiof.android.ambiguous.NetworkOpponent;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.network.CloseServerSocketTask;
import no.hiof.android.ambiguous.network.CloseSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask.OpenSocketListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public class LANFragment extends Fragment implements OpenSocketListener {
	private boolean isServer;
	private Socket socket;
	private ServerSocket server;

	private OpenSocketTask openSocketTask;

	private AlertDialog waitingForNetwork;

	private String address;
	private int port;

	private NetworkOpponent networkOpponent;

	@Override
	public void onStart() {
		super.onStart();
		
		Bundle args = getArguments();
		
		if(!args.containsKey("address") || !args.containsKey("port") || !args.containsKey("isServer"))
		{
			Log.e("ambiguous", "Need these arguments");
		}
		
		address = args.getString("address");
		port = args.getInt("port");
		isServer = args.getBoolean("isServer",false);
		
		startNetwork();
	}

	/**
	 * Launches the network connection to opponent.
	 */
	private void startNetwork() {
		openSocketTask = new OpenSocketTask().setup(address, port, isServer);
		openSocketTask.execute(this);

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
	
	void startGameMachine()
	{
		GameActivity.gameMachine.delay = 50;
		
		//If not server we just wait for server.
		if(isServer)
		{
			GameActivity.gameMachine.startRandom();
		}
	}
	
	/**
	 * Sets up the generic OpponenController and the NetworkOpponent which handles network communication.
	 */
	void setupOpponent()
	{
		networkOpponent = new NetworkOpponent(GameActivity.opponentController,
				GameActivity.gameMachine.player,
				GameActivity.gameMachine.opponent, socket);
	}
	
	/**
	 * Adds all the listeners
	 */
	void setupListeners()
	{
		GameActivity.gameMachine.setGameMachineListener(networkOpponent);
	}
	
	/**
	 * Removes all the listeners
	 */
	void removeListeners()
	{
		GameActivity.gameMachine.removeGameMachineListener(networkOpponent);
	}
	
	/**
	 * Close the network sockets.
	 */
	private void closeSockets() {
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

}
