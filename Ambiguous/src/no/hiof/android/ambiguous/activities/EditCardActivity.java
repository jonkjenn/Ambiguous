package no.hiof.android.ambiguous.activities;

import java.util.List;

import no.hiof.android.ambiguous.Card;
import no.hiof.android.ambiguous.CardDataSource;
import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.Effect;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.SaveCardOnClickListener;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EditCardActivity extends Activity {

	private SQLiteDatabase db;
	
	Card card;
	TextView id;
	EditText cost;
	EditText name;
	EditText description;
	EditText image;
	LinearLayout effects;
	
	CardDataSource cs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editcard);
		
		this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
		
		
		this.cs = new CardDataSource(db);
		
        int cardId = getIntent().getIntExtra("id", -1);

        if(cardId <0)
        {
        	this.card = new Card();
        }else
        {
        	this.card = cs.getCard(cardId);
        }
        
        loadCard(this.card);
        final Button save = (Button)findViewById(R.id.editcard_save);
        save.setOnClickListener(new SaveCardOnClickListener(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_card, menu);
		return true;
	}
	
	public void SaveCard()
	{
		saveCard();		
	}
	
	private void saveCard()
	{
		this.card.setName(this.name.getText().toString());
		this.card.setDescription(this.description.getText().toString());
		this.card.setImage(this.image.getText().toString());
		this.card.setCost(Integer.parseInt(this.cost.getText().toString()));
		
		cs.updateCard(this.card);
	}
	
	private void loadCard(Card card)
	{
		this.id = (TextView)findViewById(R.id.editcard_id);				
		this.name = (EditText)findViewById(R.id.editcard_name);
		this.description = (EditText)findViewById(R.id.editcard_description);
		this.image = (EditText)findViewById(R.id.editcard_image);
		this.effects = (LinearLayout)findViewById(R.id.editcard_effects);
		this.cost = (EditText)findViewById(R.id.editcard_cost);
		
		id.setText(Integer.toString(card.getId()));
		name.setText(card.getName());
		description.setText(card.getDescription());
		image.setText(card.getImage());
		cost.setText(Integer.toString(card.getCost()));
		
		List<Effect> effects = card.getEffects();
		
		for(int i=0;i<effects.size();i++)
		{
			this.effects.addView(getEffectLayout(effects.get(i)));			
		}
	}
	
	private LinearLayout getEffectLayout(Effect effect)
	{
		LinearLayout ll = new LinearLayout(this);
		EditText min = new EditText(this);
		EditText max = new EditText(this);
		EditText crit = new EditText(this);
		ll.addView(min);
		ll.addView(max);
		ll.addView(crit);
		
		min.setText(Integer.toString(effect.getMinValue()));
		max.setText(Integer.toString(effect.getMaxValue()));
		crit.setText(Integer.toString(effect.getCrit()));
		
		return ll;
	}
}
