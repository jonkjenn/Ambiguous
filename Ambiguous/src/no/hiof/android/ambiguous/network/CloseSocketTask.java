package no.hiof.android.ambiguous.network;

import java.io.IOException;
import java.net.Socket;

import android.os.AsyncTask;

public class CloseSocketTask extends AsyncTask<Socket,Void,Void> {

	@Override
	protected Void doInBackground(Socket... params) {
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
