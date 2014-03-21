package no.hiof.android.ambiguous.adapter;

import java.util.List;

import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class GalleryAdapter extends BaseAdapter {
        
        List<Card> cards;

        public GalleryAdapter(SQLiteDatabase db)
        {
        	CardDataSource cd = new CardDataSource(db);
        	cards = cd.getCards();
        }
        
        @Override
        public int getCount() {
                return cards.size();
        }

        @Override
        public Card getItem(int position) {
                return cards.get(position);
        }

        @Override
        public long getItemId(int position) {
                return cards.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return CardLayout.getCardLayout(getItem(position),parent);
        }



}
