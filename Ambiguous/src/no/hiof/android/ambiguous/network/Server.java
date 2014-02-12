package no.hiof.android.ambiguous.network;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public Server()
	{
            try {
				ServerSocket socket = new ServerSocket(19999);
				Socket connection = socket.accept();
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
	}
}
