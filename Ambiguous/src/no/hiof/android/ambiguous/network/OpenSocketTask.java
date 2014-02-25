package no.hiof.android.ambiguous.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import no.hiof.android.ambiguous.network.OpenSocketTask.OpenSocketListener;
import android.os.AsyncTask;

public class OpenSocketTask extends AsyncTask<OpenSocketListener,Void,Socket>{
	private boolean isServer;
	private String address;
	private int port;
	private IOException exception;
	private Socket socket;
	private ServerSocket server;
	
	public OpenSocketTask setup(String address, int port, boolean isServer)
	{
		this.address = address;
		this.port = port;
		this.isServer = isServer;
		return this;
	}
	
	@Override
	protected Socket doInBackground(OpenSocketListener... params) {
			for(int i=0;i<params.length;i++){listeners.add(params[i]);}
			if(address == null){return null;}
				try {
					if (isServer) {
						server = new ServerSocket(port, 1,
								InetAddress.getByName(address));
						socket = server.accept();
					} else {
						socket = new Socket(
								InetAddress.getByName(address), port);
					}
				}catch (IOException e) {
					exception = e;
					socket = null;
				}
		return socket;
	}
	
	@Override
	protected void onPostExecute(Socket socket) {
		super.onPostExecute(socket);
        notifyOpenSocketListeners();
	}
	
	private void notifyOpenSocketListeners()
	{
		for(int i=0;i<listeners.size();i++){listeners.get(i).onOpenSocketListener(this.socket,this.server, this.exception);}
	}
	
	private List<OpenSocketListener> listeners = new ArrayList<OpenSocketListener>(); 
	public interface OpenSocketListener
	{
		void onOpenSocketListener(Socket socket,ServerSocket server, Exception exception);
	}
}
