package no.hiof.android.ambiguous.layouts;

import java.util.List;

import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardLayout {

	public static View getCardLayout(Card card, ViewGroup parent) {
                
		parent.getContext();
		LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.card_game,parent,false);
		if(card == null){return view;}
		
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

		return view;
	}

        private static TextView getEffectView(Effect e,Context context)
        {
                String text = ""; 

                String min = Integer.toString(e.getMinValue());
                String max = Integer.toString(e.getMaxValue());
                String crit = Integer.toString(e.getCrit());
                
                TextView t = new TextView(context);
                t.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
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
