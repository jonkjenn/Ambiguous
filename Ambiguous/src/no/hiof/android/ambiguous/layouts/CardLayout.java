package no.hiof.android.ambiguous.layouts;

import java.util.List;

import no.hiof.android.ambiguous.CardOnClickListener;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CardLayout extends LinearLayout {
	private Card card;

	public CardLayout(Context context) {
		super(context);
	}
	
	public CardLayout(Context context, AttributeSet attributeSet)
	{
		super(context, attributeSet);
	}
	
	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
		TextView id = (TextView)findViewById(R.id.card_id);
		TextView cost = (TextView)findViewById(R.id.card_cost);
		TextView name = (TextView)findViewById(R.id.card_name);
		TextView description = (TextView)findViewById(R.id.card_description);
		ImageView image = (ImageView)findViewById(R.id.card_image);

		id.setText(Integer.toString(this.card.getId()));
		cost.setText(Integer.toString(this.card.getCost()));
		name.setText(this.card.getName());
		description.setText(this.card.getDescription());
		
		int imageId = getResources().getIdentifier(this.card.getImage(), "drawable",getContext().getPackageName());
		if(imageId>0)
		{
                image.setImageResource(imageId);
		}
		
		List<Effect> e = card.getEffects();
		LinearLayout l = (LinearLayout)findViewById(R.id.card_effects);
		l.removeAllViews();//For fixing double effects when resume activity
		
		for(int i=0;i<e.size();i++)
		{
			l.addView(getEffectView(e.get(i)));
		}
		
		final Button button = (Button) findViewById(R.id.card_edit);
		button.setOnClickListener(new CardOnClickListener(getContext(), card.getId()));
	}
	
	private TextView getEffectView(Effect e)
	{
		String text = "Type: " + e.getType() + " Target: " + e.getTarget(); 

		text += " " + Integer.toString(e.getMinValue());
        if(e.getMaxValue()!=0)
		{
			text += "-" + Integer.toString(e.getMaxValue());
		}
        text += "(" + Integer.toString(e.getCrit()) + ")";
        
        TextView t = new TextView(getContext());
        t.setText(text);
        return t;
	}
}
