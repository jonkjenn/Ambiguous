package no.hiof.android.ambiguous.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

public class Network {
	private Socket socket;
	private ServerSocket server;
	private String address;
	private int port = 19999;
	private boolean isServer;

	public Network(final String address, boolean isServer, NetworkConnectionListener listener) {
		this.listeners.add(listener);
		this.address = address;
		this.isServer = isServer;
		
		openSocket();
	}
	
	public void StopSocket()
	{
		closeSocket();
	}

	private void closeSocket()
	{
					if(socket != null)
					{
                        try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else if(server != null)
					{
						try {
							server.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return;
		/*Thread t = new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					if(socket != null)
					{
                        socket.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}
		});
		t.start();*/
	}
	
	private void openSocket() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				startNetwork();
			}

			private void startNetwork() {
				try {
					if (isServer) {
						server = new ServerSocket(port, 1,
								InetAddress.getByName(address));
						notifyListeningForConnection();
						socket = server.accept();
					} else {
						socket = new Socket(
								InetAddress.getByName(address), port);
					}
					
					notifyConnected();
				}
				catch(ClosedChannelException e)
				{
				} catch (IOException e) {
					notifyConnectionFailed(e.getMessage());
				}
			}
		});
		t.start();
	}
	
	private void notifyConnected()
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onConnected(address,port, isServer);
		}
	}

	private void notifyConnectionFailed(String reason)
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onConnectionFailed(reason);
		}
	}

	private void notifyDisconnected(String reason)
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onDisconnected(reason);
		}
	}
	
	private void notifyListeningForConnection()
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onListeningForConnection(address,port);
		}
	}
	
	private List<NetworkConnectionListener> listeners = new ArrayList<Network.NetworkConnectionListener>();
	public void setOnNetworkConnectionListener(NetworkConnectionListener listener)
	{
		for(int i=0;i<listeners.size();i++)
		{
			if(listeners.get(i) == listener)
			{
				return;
			}
		}
		listeners.add(listener);				
	}
	
	public interface NetworkConnectionListener
	{
		void onConnected(String address, int port, boolean isServer);
		void onConnectionFailed(String reason);
		void onDisconnected(String reason);
		void onListeningForConnection(String address, int port);
		void onTryingToConnectToServer(String address, int port);
	}
}
