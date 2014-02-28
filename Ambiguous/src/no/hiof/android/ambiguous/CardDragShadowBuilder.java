package no.hiof.android.ambiguous;

import android.graphics.Canvas;
import android.view.View;
import android.view.View.DragShadowBuilder;

public class CardDragShadowBuilder extends DragShadowBuilder{
	public CardDragShadowBuilder(View view)
	{
		super(view);
	}

	@Override
	public void onDrawShadow(Canvas canvas) {
		//super.onDrawShadow(canvas);
		//view.draw(canvas);
	}
}