package no.hiof.android.ambiguous.adapter;

import java.util.List;

import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

                View view = CardLayout.getCardLayout(card, parent);
                
                ((View)parent.getParent()).setOnDragListener(new OnDragListener() {
					
					@Override
					public boolean onDrag(View v, DragEvent event) {
						//Log.d("test",event.getAction() + " ");
						
						switch(event.getAction())
						{
                            case DragEvent.ACTION_DRAG_STARTED:
                            	Log.d("test", "Drag started");
                                return true;
                
						    case DragEvent.ACTION_DROP:
                        		float y = event.getY();
                        		if(event.getLocalState() != null)
                        		{
                        			float starty = Float.parseFloat(event.getLocalState().toString());
                        			Log.d("test",y + " " + starty);
                        			if(y < starty){Log.d("test","Oppover");}
                        			else{Log.d("test", "Nedover");}
                        		}
                        		return true;
						}
						
						return false;
					}
				});
                
                view.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent me) {
						View.DragShadowBuilder shadow = new DragShadowBuilder(v);
						v.startDrag(null, shadow, me.getY(), 0);
						return false;
					}
				});
					
                return view;
        }



}
