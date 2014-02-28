package no.hiof.android.ambiguous.cardlistener;

import no.hiof.android.ambiguous.CardDragShadowBuilder;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class CardOnTouchListener implements OnTouchListener{
	
	int position;
	
	public CardOnTouchListener(int position)
	{
		this.position = position;
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {
        CardDragShadowBuilder shadow = new CardDragShadowBuilder(v);
        //DragShadowBuilder shadow = new DragShadowBuilder(new View());
        //v.startDrag(null, shadow, new int[]{(int)me.getRawY(),position, v.getHeight()}, 0);

        v.startDrag(null, shadow, new int[]{(int)me.getRawY(),position, v.getHeight(),(int)me.getX(),(int)me.getY()}, 0);
		return false;
	}

}
