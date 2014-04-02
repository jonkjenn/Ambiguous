package no.hiof.android.ambiguous.adapter;

import no.hiof.android.ambiguous.cardlistener.CardOnTouchListener;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Adapter for the view that shows the cards on the player's hand.
 */
public class GameDeckAdapter extends BaseAdapter {

	Card[] cards;

	public GameDeckAdapter(Card[] cards) {
		this.cards = cards;
	}

	@Override
	public int getCount() {
		return cards.length;
	}

	@Override
	public Card getItem(int position) {
		return cards[position];
	}

	@Override
	public long getItemId(int position) {
		return cards[position].getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Card card = getItem(position);
		// Make sure the old card is the exact same if we want to reuse it.
		if (convertView != null && convertView instanceof ImageView
				&& convertView.getTag() == card.getImage()) {
			return convertView;
		}

		// Return a empty card.
		if (card == null) {
			return CardLayout.getCardLayout(null, parent);
		}
		;

		View view = CardLayout.getCardLayout(card, parent);

		//Old versions does not support the drag drop, we use context menus instead there.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			view.setOnTouchListener(new CardOnTouchListener(position));
		}

		return view;
	}
}