package no.hiof.android.ambiguous.activities;

import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

//TODO: Fix or remove this.
/**
 * This class is not used, probably will never be used.
 * For adding and editing cards in the database. Adding and editing effects is missing.
 */
public class EditCardActivity extends Activity implements OnClickListener{

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
        	this.card = CardDataSource.getCard(cardId);
        }
        
        loadCard(this.card);
        final Button save = (Button)findViewById(R.id.editcard_save);
        save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				EditCardActivity.this.saveCard();				
			}
		});
        final Button addEffect = (Button)findViewById(R.id.editcard_addeffect);
        addEffect.setOnClickListener(this);
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
		this.card.name = (this.name.getText().toString());
		this.card.description = (this.description.getText().toString());
		this.card.image = (this.image.getText().toString());
		this.card.cost = (Integer.parseInt(this.cost.getText().toString()));
		
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
		
		id.setText(Integer.toString(card.id));
		name.setText(card.name);
		description.setText(card.description);
		image.setText(card.image);
		cost.setText(Integer.toString(card.cost));
		
		loadEffects();
		
	}
	
	private void loadEffects()
	{
		this.effects.removeAllViews();
		List<Effect> effects = card.effects;
		
		for(int i=0;i<effects.size();i++)
		{
			this.effects.addView(getEffectLayout(effects.get(i)));			
		}
	}
	
	private void addEffect()
	{
		Effect effect = new Effect();
		this.card.effects.add(effect);
		loadEffects();
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
		
		min.setText(Integer.toString(effect.minValue));
		max.setText(Integer.toString(effect.maxValue));
		crit.setText(Integer.toString(effect.crit));
		
		return ll;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.editcard_addeffect:
				addEffect();		
		}
		
	}
}
