package no.hiof.android.ambiguous.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import android.os.Handler;
import android.os.Message;

public class Client {
	private String address;
	private Handler outThreadHandler;
	
	public static enum ClientStates{CONNECTION_FAILED, CONNECTED};
	
	public Client(String address, Handler outThreadHandler) {
		this.address = address;
		this.outThreadHandler = outThreadHandler;
	}
	
	public void Connect()
	{
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					Socket connection = new Socket(InetAddress
							.getByName(address), 19999);
					outThreadHandler.obtainMessage(ClientStates.CONNECTED.ordinal(),connection);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(connection.getInputStream()));
					String message = in.readLine();


				} catch (IOException e) {
					outThreadHandler.obtainMessage(ClientStates.CONNECTION_FAILED.ordinal()).sendToTarget();

				}
			}
		});
		t.start();
	}
}
