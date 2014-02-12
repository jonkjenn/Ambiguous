package no.hiof.android.ambiguous.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Client {
	public Client()
	{
            try {
				Socket connection = new Socket(InetAddress.getByName(""),19999);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
	}
}
