package no.hiof.android.ambiguous.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import no.hiof.android.ambiguous.network.OpenSocketTask.OpenSocketListener;
import android.os.AsyncTask;

/**
 * Opens a network socket either as server or client.
 */
public class OpenSocketTask extends AsyncTask<OpenSocketListener, Void, Socket> {
	private boolean isServer;
	private String address;
	private int port;
	private IOException exception;
	private Socket socket;
	private ServerSocket server;

	public OpenSocketTask setup(String address, int port, boolean isServer) {
		this.address = address;
		this.port = port;
		this.isServer = isServer;
		return this;
	}

	@Override
	protected Socket doInBackground(OpenSocketListener... params) {
		// Add the listeners that wants updates from this class.
		for (int i = 0; i < params.length; i++) {
			listeners.add(params[i]);
		}
		// Cannot connect without a address
		if (address == null) {
			return null;
		}
		try {
			if (isServer) {
				server = new ServerSocket(port, 1,
						InetAddress.getByName(address));
				// We use a timeout to prevent blocking infinitively.
				server.setSoTimeout(1000);
				// We check if user has cancelled between each timeout.
				while (!isCancelled() && null == (socket = startServer(server))) {
				}
			} else {
				while(!isCancelled() && null == (socket = connectSocket(address,port))){};
			}
		} catch (IOException e) {
			// Exception is passed to listeners in onPostExecute.
			exception = e;
			socket = null;
		}

		if (isCancelled()) {
			closeSilently();
		}

		return socket;
	}

	private Socket connectSocket(String address, int port) {
		Socket socket;
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(address, port),2000);
		}
		catch (IOException e) {//Return null so that the loop will keep trying, the user has to cancel it before it will stop.
			return null;
		}
		return socket;
	}

	/*
	 * Closes the socket connections without handling eventual exceptions. This
	 * should be fine at least for our use case.
	 */
	private void closeSilently() {
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				// Do nothing
			}
		}

		if (socket != null)
			try {
				socket.close();
			} catch (IOException e) {
				// Do nothing
			}
	}

	/**
	 * Starts a blocking call to wait for client to connect. The ServerSocket
	 * should have a timeout when using this. If the blocking call times out the
	 * function returns false so that the calling loop can decide if it wants to
	 * keep trying.
	 * 
	 * @return A Socket if client connected, null if timed out.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private Socket startServer(ServerSocket server)
			throws UnknownHostException, IOException {
		Socket socket = null;
		try {
			socket = server.accept();
		} catch (SocketTimeoutException e) {
			return null;
		}
		return socket;
	}

	@Override
	protected void onPostExecute(Socket socket) {
		notifyOpenSocketListeners();
	}

	/**
	 * Notify the listeners with the sockets and eventual exceptions.
	 */
	private void notifyOpenSocketListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onOpenSocketListener(this.socket, this.server,
					this.exception);
		}
	}

	private List<OpenSocketListener> listeners = new ArrayList<OpenSocketListener>();

	/**
	 * Interface for those that want updates on socket connection outcomes.
	 */
	public interface OpenSocketListener {
		void onOpenSocketListener(Socket socket, ServerSocket server,
				Exception exception);
	}
}
