package no.hiof.android.ambiguous.cardlistener;

import no.hiof.android.ambiguous.EmptyDragShadowBuilder;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Handles touch events on cards in the players hand. Is the start of drag events.
 */
public class CardOnTouchListener implements OnTouchListener{
	
	int position;
	
	/**
	 * @param position The position in the players hand the touched card has.
	 */
	public CardOnTouchListener(int position)
	{
		this.position = position;
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {
		//Empty shadow since we display our own manually to avoid the transparency.
        EmptyDragShadowBuilder shadow = new EmptyDragShadowBuilder(v);

        //Start a drag event with the touched card.
        v.startDrag(null, shadow, new CardTouchData((int)me.getRawY(),position,v.getHeight(),(int)me.getX(),(int)me.getY()),0);
		return false;
	}
	
	/**
	 * Data passed with the card touch event.
	 */
	public class CardTouchData
	{
		public int screenY; // The y coordinate of the touch in reference to the screen.
		public int position; // The position of the card in the player's hand.
		public int viewHeight; // The height of the view.
		public int localX; // The x coordinate of the touch inside the view.
		public int localY; // The y coordinate of the touch inside the view.
		
		public CardTouchData(int screenY, int position, int viewHeight,int localX, int localY)
		{
			this.screenY = screenY;
			this.position = position;
			this.viewHeight = viewHeight;
			this.localX = localX;
			this.localY = localY;
		}
	}

}
