package no.hiof.android.ambiguous.floatingtext;

import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class FloatingHandler extends Handler{
	
	TextView textView;
	boolean fromRight;
	
	public FloatingHandler(TextView textView, boolean fromRight)
	{
		this.textView = textView;
		this.fromRight = fromRight;
	}

	@Override
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		Animation ani = AnimationUtils.makeOutAnimation(this.textView.getContext(),fromRight);		
		ani.setAnimationListener(new FloatingTextAnimationListener(this.textView,new RemoveFloatHandler(this.textView),TextView.GONE));
		textView.startAnimation(ani);
	}

}
