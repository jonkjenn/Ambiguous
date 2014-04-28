package no.hiof.android.ambiguous;

/**
 * Notifies about any errors during network operation. Network gets shut down on
 * all errors.
 */
public interface OnNetworkErrorListener {
	void onNetworkError(String error);
}
