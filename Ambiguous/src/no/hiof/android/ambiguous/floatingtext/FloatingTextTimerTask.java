package no.hiof.android.ambiguous.floatingtext;

import java.util.TimerTask;

import android.os.Handler;

public class FloatingTextTimerTask extends TimerTask{
	
	Handler handler;
	
	public FloatingTextTimerTask(Handler handler)
	{
		this.handler = handler;
	}

	@Override
	public void run() {
		this.handler.obtainMessage().sendToTarget();
	}

}
