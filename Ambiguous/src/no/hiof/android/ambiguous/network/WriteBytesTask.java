package no.hiof.android.ambiguous.network;

import java.io.DataOutputStream;
import java.io.IOException;

import android.os.AsyncTask;

/**
 * Writes bytes to a DataOutpuStream. 
 */
public class WriteBytesTask extends AsyncTask<byte[],Void,Void> {
	
	private DataOutputStream out;

	public WriteBytesTask Setup(DataOutputStream out)
	{
		this.out = out;
		return this;
	}
	
	@Override
	protected Void doInBackground(byte[]... params) {
		try {
			out.write(params[0]);
		} catch (IOException e) {
			//TODO: Handle network error.
		}
		return null;
	}

}
