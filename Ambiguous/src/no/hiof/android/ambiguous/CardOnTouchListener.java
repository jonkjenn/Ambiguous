package no.hiof.android.ambiguous;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;

public class CardOnTouchListener implements OnTouchListener{
	
	int position;
	
	public CardOnTouchListener(int position)
	{
		this.position = position;
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {
        View.DragShadowBuilder shadow = new DragShadowBuilder(v);
        v.startDrag(null, shadow, new int[]{(int)me.getRawY(),position}, 0);
		return false;
	}

}
