package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.adapter.GameDeckAdapter;
import no.hiof.android.ambiguous.model.Card;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TableLayout;

public class DeckView extends TableLayout {
	
	public DeckView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private GameDeckAdapter adapter;

	public void setAdapter(GameDeckAdapter adapter) {
		this.adapter = adapter;
		
		((ViewGroup)(findViewById(R.id.deck_card1))).addView(adapter.getView(0, null, this));
		((ViewGroup)(findViewById(R.id.deck_card2))).addView(adapter.getView(1, null, this));
		((ViewGroup)(findViewById(R.id.deck_card3))).addView(adapter.getView(2, null, this));
		((ViewGroup)(findViewById(R.id.deck_card4))).addView(adapter.getView(3, null, this));
		((ViewGroup)(findViewById(R.id.deck_card5))).addView(adapter.getView(4, null, this));
		((ViewGroup)(findViewById(R.id.deck_card6))).addView(adapter.getView(5, null, this));
		((ViewGroup)(findViewById(R.id.deck_card7))).addView(adapter.getView(6, null, this));
		((ViewGroup)(findViewById(R.id.deck_card8))).addView(adapter.getView(7, null, this));
	}

	public Card getItemAtPosition(int position) {
		return adapter.getItem(position);
	}
}
