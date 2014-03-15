package no.hiof.android.ambiguous.network;

import java.io.IOException;
import java.net.ServerSocket;

import android.os.AsyncTask;

public class CloseServerSocketTask extends AsyncTask<ServerSocket,Void,Void> {
	
	@Override
	protected Void doInBackground(ServerSocket... params) {
		try {
			if(params.length>0 && params[0] != null)
			{
                params[0].close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}