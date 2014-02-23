package no.hiof.android.ambiguous.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network {
	private static Socket socket;
	private static String address;
	private static int port = 19999;
	private static boolean isServer;
	private static int socketListeners = 0;

	public static Socket StartSocket(final String address, boolean isServer) {
		if (socket == null) {
			openSocket(address, isServer);
		}
		else if(Network.isServer != isServer)
		{
			closeSocket();			
			openSocket(address,isServer);
		}
		else if(socket.isClosed())
		{
			openSocket(address,isServer);
		}
        socketListeners++;
		return socket;
	}
	
	public static void StopSocket()
	{
		socketListeners--;
		if(socketListeners<=0){closeSocket();socket = null;}
	}

	private static void closeSocket()
	{
		socketListeners = 0;
		Thread t = new Thread(new Runnable(){

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
		t.start();
	}
	
	private static void openSocket(final String address, final boolean isServer) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				startNetwork();
			}

			private void startNetwork() {
				try {
					if (isServer) {
						ServerSocket server = new ServerSocket(port, 1,
								InetAddress.getByName(address));
						socket = server.accept();
					} else {
						socket = new Socket(
								InetAddress.getByName(address), port);
					}
					
					notifyConnected();

				} catch (IOException e) {
					notifyConnectionFailed(e.getMessage());
				}
			}
		});
		t.start();
	}
	
	private static void notifyConnected()
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onConnected(address,port, isServer);
		}
	}

	private static void notifyConnectionFailed(String reason)
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onConnectionFailed(reason);
		}
	}

	private static void notifyDisconnected(String reason)
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onDisconnected(reason);
		}
	}
	
	private static void notifyListeningForConnection()
	{
		for(int i=0;i<listeners.size();i++)
		{
			listeners.get(i).onListeningForConnection(address,port);
		}
	}
	
	private static List<NetworkConnectionListener> listeners = new ArrayList<Network.NetworkConnectionListener>();
	public static void setOnNetworkConnectionListener(NetworkConnectionListener listener)
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
