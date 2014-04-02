package no.hiof.android.ambiguous;

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.os.Build;
import android.view.View;
import android.view.View.DragShadowBuilder;

/**
 * DragShadoBuilder with empty drawShadow.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB) //This is not used in lower API levels, see DeckManager.getView
public class EmptyDragShadowBuilder extends DragShadowBuilder {
	public EmptyDragShadowBuilder(View view) {
		super(view);
	}

	@Override
	public void onDrawShadow(Canvas canvas) {
		// This being empty is the reason for extending this class.
	}
}