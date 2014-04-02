package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.activities.GameActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Reacts to broadcasts and generates a notification for the user. The
 * notification reminds the user he has a game running, and clicking on the
 * notification brings the user back into his game.
 */
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {

		//The notification that on click will bring the user back into the game.
		NotificationCompat.Builder b = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Ambiguous")
				.setAutoCancel(true)
				.setContentText(
						context.getResources().getString(R.string.anoying_notification));

		//Intent that launches game
		Intent target = new Intent(context, GameActivity.class);
		target.setAction(Intent.ACTION_MAIN);
		target.addCategory(Intent.CATEGORY_LAUNCHER);
		target.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		NotificationManager m = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent p = PendingIntent.getActivity(context, 0, target,
				PendingIntent.FLAG_CANCEL_CURRENT);

		b.setContentIntent(p);
		m.notify(0, b.build());
	}

}
