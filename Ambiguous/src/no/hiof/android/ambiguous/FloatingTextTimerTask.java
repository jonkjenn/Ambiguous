package no.hiof.android.ambiguous;

import java.util.TimerTask;

import android.os.Handler;
import android.widget.TextView;

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
