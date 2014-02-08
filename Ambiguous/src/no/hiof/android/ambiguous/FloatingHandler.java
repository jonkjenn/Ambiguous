package no.hiof.android.ambiguous;

import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class FloatingHandler extends Handler{
	
	TextView textView;
	
	public FloatingHandler(TextView textView)
	{
		this.textView = textView;
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		Animation ani = AnimationUtils.makeOutAnimation(this.textView.getContext(),true);		
		ani.setAnimationListener(new FloatingTextAnimationListener(this.textView,new RemoveFloatHandler(this.textView),TextView.GONE));
		textView.startAnimation(ani);
	}

}
