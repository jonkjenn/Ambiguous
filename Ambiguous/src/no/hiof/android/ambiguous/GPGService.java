package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.activities.GameActivity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class GPGService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				mainLoop();
			}
		}, "no.hiof.ambiguous.GPGService").start();
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void mainLoop()
	{
		startForeground(1, buildNotification());
	}
	
	Notification buildNotification()
	{
		Intent ni = new Intent(this, GameActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, ni, 0);
		Notification n = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.smiley_drawing_small).setContentTitle("Ambiguous").setContentText("Test")
				.setContentIntent(pi).build();
		return n;
	}
	
	private void getData()
	{

	}
}
