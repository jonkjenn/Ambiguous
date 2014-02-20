package no.hiof.android.ambiguous.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;

public class Server {
	public static enum ServerStates{CONNECTION_FAILED, CONNECTED};
	public Server(final String address, final Handler outputHandler) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
					startServer();
			}
			
			private void startServer()
			{
				try {
					ServerSocket socket = new ServerSocket(19999, 1,
							InetAddress.getByName(address));
					//Log.d("Nettverk", "Listening");
					Socket connection = socket.accept();

					DataOutputStream out = new DataOutputStream(connection.getOutputStream());
					out.writeLong(0xA7B7C7D7);
					DataInputStream in = new DataInputStream(connection.getInputStream());
					if(in.readLong()==0xA7B7C7D7)
					{
						outputHandler.obtainMessage(ServerStates.CONNECTED.ordinal(),connection).sendToTarget();
					}
					else
					{
						out.close();
						in.close();
						connection.close();
						outputHandler.obtainMessage(ServerStates.CONNECTION_FAILED.ordinal()).sendToTarget();
					}

				} catch (IOException e) {
						outputHandler.obtainMessage(ServerStates.CONNECTION_FAILED.ordinal()).sendToTarget();
				}
			}
		});
		t.start();

	}
}
