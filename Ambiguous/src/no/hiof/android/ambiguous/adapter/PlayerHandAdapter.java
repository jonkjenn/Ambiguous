package no.hiof.android.ambiguous.adapter;

import no.hiof.android.ambiguous.Helper;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import android.os.Build;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Adapter for the view that shows the cards on the player's hand.
 */
public class PlayerHandAdapter extends BaseAdapter {

	Card[] cards;
	OnTouchListener onTouchListener;

	public PlayerHandAdapter(Card[] cards) {
		this.cards = cards;
	}

	public void setOnTouchListener(OnTouchListener onTouchListener) {
		this.onTouchListener = onTouchListener;
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
		return cards[position].id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Card card = getItem(position);
		// Make sure the old card is the exact same if we want to reuse it.
		if (convertView != null && convertView.getTag() != null
				&& Helper.getIdFromTag(convertView.getTag()) == card.id) {
			return convertView;
		}

		// Return a empty card.
		if (card == null) {
			return CardLayout.getCardLayout(null, parent);
		}
		;

		View view = CardLayout.getCardLayout(card, parent);

		// Old versions does not support the drag drop, we use context menus
		// instead there.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			view.setTag(Helper.putPositionInTag(view.getTag(), position));
			if (onTouchListener != null) {
				view.setOnTouchListener(onTouchListener);
			}
		}

		return view;
	}
}