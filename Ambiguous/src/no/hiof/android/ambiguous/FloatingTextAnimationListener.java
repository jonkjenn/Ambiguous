package no.hiof.android.ambiguous;

import java.util.Timer;

import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

public class FloatingTextAnimationListener implements AnimationListener{
	TextView floatingText;
	Handler handler;
	int visibility;

	public FloatingTextAnimationListener(TextView floatingText, Handler handler, int visibility)
	{
		this.handler = handler;
		this.floatingText = floatingText;
		this.visibility = visibility;
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
        this.floatingText.setVisibility(visibility);
		Timer t = new Timer();
		t.schedule(new FloatingTextTimerTask(this.handler),1000);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	}

}
