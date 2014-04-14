package no.hiof.android.ambiguous;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DataService extends Service {

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
		}, "no.hiof.ambiguous.DataService").start();
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void mainLoop()
	{
	}
	
	private void getData()
	{

	}
	}
}
