package no.hiof.android.ambiguous.network;

import java.io.Closeable;
import java.io.IOException;

import android.os.AsyncTask;

public class CloseSocket extends AsyncTask<Closeable,Void,Void> {

	@Override
	protected Void doInBackground(Closeable... params) {
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
