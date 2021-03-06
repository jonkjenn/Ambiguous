package no.hiof.android.ambiguous;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;

public class Helper {

	/**
	 * Id is stored in second byte (second most right byte)
	 * 
	 * @param tag
	 *            Never send null
	 * 
	 * @return Id stored in tag
	 */
	public static int getIdFromTag(Object tag) {
		return ((Integer) tag) >> 8;
	}

	/**
	 * Position stored in first byte, really the most right byte
	 * 
	 * @param tag
	 *            Should never be null
	 * @return Position stored in tag.
	 */
	public static int getPositionFromTag(Object tag) {
		return ((Integer) tag) & 255;
	}

	/**
	 * Put id in first byte, its really the second most right byte.
	 * 
	 * Blank out the bytes above the most right byte. Then add id as the second
	 * most right byte.
	 * 
	 * @param tag
	 *            Should not be null
	 * @param id
	 * @return The tag object with the id inserted.
	 */
	public static Object putIdInTag(Object tag, int id) {
		return (((Integer) tag) & 255) + id << 8;
	}

	/**
	 * 
	 * 65281 = 2^16 - 255 We blank out the right most byte, then add the
	 * position which has to be <= 255.
	 * 
	 * @param tag
	 *            Should not be null
	 * @param position
	 * @return The tag object with the position inserted.
	 */
	public static Object putPositionInTag(Object tag, int position) {
		return (((Integer) tag) & 65281) + position;
	}

	/**
	 * Builds a generic error dialog.
	 * 
	 * @param messageId
	 *            All messages should come from R.string
	 * @param context
	 * @param onDismissListener
	 *            Optional listener to when the dialog gets dismissed. Pass null
	 *            to ignore.
	 * @return
	 */
	public static AlertDialog showError(int messageId, Context context,
			OnDismissListener onDismissListener) {
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setMessage(messageId).setTitle(R.string.error)
				.setNegativeButton(R.string.close, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		//For compatibility reasons we set listener on the AlertDialog instead with the builder.
		AlertDialog d = b.create();
		if (onDismissListener != null) {
			d.setOnDismissListener(onDismissListener);
		}
		return d;
	}
}