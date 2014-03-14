package no.hiof.android.ambiguous;

import android.graphics.Canvas;
import android.view.View;
import android.view.View.DragShadowBuilder;

/**
 * DragShadoBuilder with empty drawShadow.
 */
public class EmptyDragShadowBuilder extends DragShadowBuilder{
	public EmptyDragShadowBuilder(View view)
	{
		super(view);
	}

	@Override
	public void onDrawShadow(Canvas canvas) {
		//This being empty is the reason for extending this class.
	}
}