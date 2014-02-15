package no.hiof.android.ambiguous.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

public class Client {
	public Client() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Socket connection = new Socket(InetAddress
							.getByName("192.168.1.44"), 19999);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					Log.d("Test", in.readLine());
					connection.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();

	}
}
