package no.hiof.android.ambiguous;

import java.util.List;

import no.hiof.android.ambiguous.datasource.CardDataSource;
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


public class DeckmanagerAdapter extends BaseAdapter {
        
        List<Card> cards;
        private int cardLayout;

        public DeckmanagerAdapter(SQLiteDatabase db, int cardLayout)
        {
        	CardDataSource cd = new CardDataSource(db);
        	cards = cd.getCards();
        	this.cardLayout = cardLayout;  
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
                Card card = getItem(position);
                
                LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(parent.getContext().LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(this.cardLayout,parent,false);

                TextView id = (TextView)view.findViewById(R.id.card_id);
                TextView cost = (TextView)view.findViewById(R.id.card_cost);
                TextView name = (TextView)view.findViewById(R.id.card_name);
                TextView description = (TextView)view.findViewById(R.id.card_description);
                ImageView image = (ImageView)view.findViewById(R.id.card_image);

                id.setText(Integer.toString(card.getId()));
                cost.setText(Integer.toString(card.getCost()));
                name.setText(card.getName());
                if(description != null){description.setText(card.getDescription());}
                
                int imageId = parent.getContext().getResources().getIdentifier(card.getImage(), "drawable",parent.getContext().getPackageName());
                if(imageId>0)
                {
                image.setImageResource(imageId);
                }
                
                List<Effect> e = card.getEffects();
                LinearLayout l = (LinearLayout)view.findViewById(R.id.card_effects);
                l.removeAllViews();//For fixing double effects when resume activity
                
                for(int i=0;i<e.size();i++)
                {
                        l.addView(getEffectView(e.get(i),parent.getContext()));
                }
                
                final Button button = (Button) view.findViewById(R.id.card_edit);
                button.setOnClickListener(new CardOnClickListener(parent.getContext(), card.getId()));
                
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

        private TextView getEffectView(Effect e,Context context)
        {
                String text = ""; 

                String min = Integer.toString(e.getMinValue());
                String max = Integer.toString(e.getMaxValue());
                String crit = Integer.toString(e.getCrit());
                
                TextView t = new TextView(context);
                t.setTextColor(Color.WHITE);
                
                LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.leftMargin = 7;
                t.setLayoutParams(layout);
                t.setPadding(3, 0, 3, 0);
                
                switch(e.getType())
                {
                        case ARMOR:
                        	text = min;
                        	t.setBackgroundColor(Color.BLUE);
                        	break;
                        case DAMAGE:
                        	text = min + "-" + max + "(" + crit +")";
                        	t.setBackgroundColor(Color.RED);
                        	break;
                        case HEALTH:
                        	text = min + "-" + max + "(" + crit +")";
                        	t.setBackgroundColor(Color.rgb(45, 190, 50));
                        	break;
                        case RESOURCE:
                        	text = min;
                        	t.setBackgroundColor(Color.rgb(180,180,50));
                        	break;
                        default:
                        	 break;
                }

                t.setText(text);
                
                return t;
        }


}
