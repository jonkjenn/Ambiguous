package no.hiof.android.ambiguous.network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;

public class Server {
	public Server(final String address, Handler outputHandler) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				do {
					startServer();
				}while(true);
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

					//BufferedWriter out = new BufferedWriter(
						//	new OutputStreamWriter(connection.getOutputStream()));
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();

	}
}
