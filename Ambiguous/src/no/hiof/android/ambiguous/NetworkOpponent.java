package no.hiof.android.ambiguous;

import java.io.IOException;
import java.net.Socket;

public class NetworkOpponent {
	
	private OpponentController oc;
	private Socket socket;
	
	public NetworkOpponent(OpponentController oc, Socket socket)
	{
		this.oc = oc;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
