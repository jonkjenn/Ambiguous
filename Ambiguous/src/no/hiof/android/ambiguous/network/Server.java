package no.hiof.android.ambiguous.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class Server {
	public Server(final String address) {
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
					Log.d("Nettverk", "Listening");
					Socket connection = socket.accept();
					BufferedWriter out = new BufferedWriter(
							new OutputStreamWriter(connection.getOutputStream()));
					out.write("Nettverk test");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();

	}
}
