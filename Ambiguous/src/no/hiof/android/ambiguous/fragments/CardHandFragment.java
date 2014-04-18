package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.EmptyDragShadowBuilder;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.adapter.PlayerHandAdapter;
import no.hiof.android.ambiguous.model.Card;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

public class CardHandFragment extends Fragment implements OnTouchListener {
	GridView hand;
	PlayerHandAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_cardhand, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		hand = (GridView) view.findViewById(R.id.game_grid);

		view.setOnTouchListener(this);
	}

	public void updateCards(Card[] cards) {
		adapter = new PlayerHandAdapter(cards);
		adapter.setOnTouchListener(this);
		hand.setAdapter(adapter);
	}

	@Override
	public boolean onTouch(View v, MotionEvent me) {

		Object tag = v.getTag();
		if (tag != null) {
			int position = (Integer) v.getTag();

			if (listener != null) {

				if (v instanceof ImageView) {
					ImageView iv = (ImageView) v;
					Bitmap b = ((BitmapDrawable) iv.getDrawable()).getBitmap();

					CardTouchData cardTouchData = new CardTouchData(
							(int) me.getRawY(), position, v.getHeight(),
							(int) me.getX(), (int) me.getY(), b);

					// Empty shadow since we display our own manually to avoid
					// the
					// transparency.
					EmptyDragShadowBuilder shadow = new EmptyDragShadowBuilder(
							v);

					// Start a drag event with the touched card.
					v.startDrag(null, shadow, cardTouchData, 0);
				}

			}
			return true;
		}
		return false;
	}

	public void showCard(int position) {
		hand.getChildAt(position).setVisibility(View.VISIBLE);
	}

	public void hideCard(int position) {
		hand.getChildAt(position).setVisibility(View.INVISIBLE);
	}
	
	OnCardTouchedListener listener;

	public interface OnCardTouchedListener {
		void onCardTouched(View v, CardTouchData cardTouchData);
	}

	public void setOnCardTouchedListener(OnCardTouchedListener listener) {
		this.listener = listener;
	}

	/**
	 * Data passed with the card touch event.
	 */
	public class CardTouchData {
		public int screenY; // The y coordinate of the touch in reference to the
							// screen.
		public int position; // The position of the card in the player's hand.
		public int viewHeight; // The height of the view.
		public int localX; // The x coordinate of the touch inside the view.
		public int localY; // The y coordinate of the touch inside the view.
		public Bitmap bitmap;// This bitmap of the image we touch

		public CardTouchData(int screenY, int position, int viewHeight,
				int localX, int localY, Bitmap bitmap) {
			this.screenY = screenY;
			this.position = position;
			this.viewHeight = viewHeight;
			this.localX = localX;
			this.localY = localY;
			this.bitmap = bitmap;
		}
	}
}
