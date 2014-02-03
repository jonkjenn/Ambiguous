package no.hiof.android.ambiguous;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class DeckmanagerAdapter extends BaseAdapter {
	
	List<Card> cards;

	public DeckmanagerAdapter(SQLiteDatabase db)
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
		Card card = getItem(position);
		
		LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(parent.getContext().LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.card,parent,false);

		TextView id = (TextView)view.findViewById(R.id.card_id);
		TextView cost = (TextView)view.findViewById(R.id.card_cost);
		TextView name = (TextView)view.findViewById(R.id.card_name);
		TextView description = (TextView)view.findViewById(R.id.card_description);
		ImageView image = (ImageView)view.findViewById(R.id.card_image);

		id.setText(Integer.toString(card.getId()));
		cost.setText(Integer.toString(card.getCost()));
		name.setText(card.getName());
		description.setText(card.getDescription());
		
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
		
		return view;

	/*
		TextView id = new TextView(parent.getContext());
		TextView cost = new TextView(parent.getContext());
		TextView name = new TextView(parent.getContext());
		TextView description = new TextView(parent.getContext());
		ImageView image = new ImageView(parent.getContext());
		
		id.setText(Integer.toString(card.getId()));
		name.setText(card.getName());
		cost.setText(Integer.toString(card.getCost()));
		description.setText(card.getDescription());
		
		LinearLayout layout = new LinearLayout(parent.getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(id);
		layout.addView(cost);
		layout.addView(name);
		layout.addView(description);
		layout.addView(image);

		
		int imageId = parent.getResources().getIdentifier(card.getImage(), "drawable",parent.getContext().getPackageName());
		if(imageId>0)
		{
                image.setImageResource(imageId);
		}
		
		List<Effect> e = card.getEffects();
		LinearLayout l = new LinearLayout(parent.getContext());
		l.setOrientation(LinearLayout.VERTICAL);
		l.removeAllViews();//For fixing double effects when resume activity
		
		for(int i=0;i<e.size();i++)
		{
			l.addView(getEffectView(e.get(i),parent.getContext()));
		}
		
		final Button button = new Button(parent.getContext());
		button.setOnClickListener(new CardOnClickListener(parent.getContext(), card.getId()));
		
		layout.addView(l);
		layout.addView(button);
		
		return layout;*/
	}

	private TextView getEffectView(Effect e,Context context)
	{
		String text = "Type: " + e.getType() + " Target: " + e.getTarget(); 

		text += " " + Integer.toString(e.getMinValue());
        if(e.getMaxValue()!=0)
		{
			text += "-" + Integer.toString(e.getMaxValue());
		}
        text += "(" + Integer.toString(e.getCrit()) + ")";
        
        TextView t = new TextView(context);
        t.setText(text);
        return t;
	}


}
