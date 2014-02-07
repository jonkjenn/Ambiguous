package no.hiof.android.ambiguous.adapter;

import no.hiof.android.ambiguous.CardOnTouchListener;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class GameDeckAdapter extends BaseAdapter {
        
		Card[] cards;

        public GameDeckAdapter(Card[] cards)
        {
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
                
                if(card == null){
                TextView t = new TextView(parent.getContext());
                t.setVisibility(TextView.GONE);
                return t;}

                View view = CardLayout.getCardLayout(card, parent);

                view.setOnTouchListener(new CardOnTouchListener(position));
                
                return view;
        }



}