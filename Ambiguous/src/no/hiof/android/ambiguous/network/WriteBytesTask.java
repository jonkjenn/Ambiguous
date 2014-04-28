package no.hiof.android.ambiguous.network;

import java.io.DataOutputStream;
import java.io.IOException;

import no.hiof.android.ambiguous.OnNetworkErrorListener;
import android.os.AsyncTask;

/**
 * Writes bytes to a DataOutpuStream. Use setup method for setting up.
 */
public class WriteBytesTask extends AsyncTask<byte[], Void, Boolean> {

	DataOutputStream out;
	OnNetworkErrorListener onNetworkErrorListener;

	/**
	 * Setup our task
	 * @param out An open DataOutPutStream ready to send on.
	 * @param onNetworkErrorListener A listener that will receive error messages.
	 * @return
	 */
	public WriteBytesTask Setup(DataOutputStream out,
			OnNetworkErrorListener onNetworkErrorListener) {
		this.onNetworkErrorListener = onNetworkErrorListener;
		this.out = out;
		return this;
	}

	@Override
	protected Boolean doInBackground(byte[]... params) {
		try {
			out.write(params[0]);
		} catch (IOException e) {
			notifyNetworkError(e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Notifies listeners to errors.
	 * @param error
	 */
	void notifyNetworkError(String error) {
		if (this.onNetworkErrorListener != null) {
			this.onNetworkErrorListener.onNetworkError(error);
		}
	}

}
