package no.hiof.android.ambiguous.activities;

import no.hiof.android.ambiguous.Card;
import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.SaveCardOnClickListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditCardActivity extends Activity {

	Card card;
	TextView id;
	EditText name;
	EditText description;
	EditText image;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editcard);
		
		Db db = Db.getDb(getApplicationContext());
		
        int cardId = getIntent().getIntExtra("id", -1);

        if(cardId <0)
        {
        	this.card = new Card();
        }else
        {
        	this.card = db.getCard(cardId);
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
		
		Db db = Db.getDb(this.getApplicationContext());
		db.SaveCard(this.card);
		db.close();
	}
	
	private void loadCard(Card card)
	{
		this.id = (TextView)findViewById(R.id.editcard_id);				
		this.name = (EditText)findViewById(R.id.editcard_name);
		this.description = (EditText)findViewById(R.id.editcard_description);
		this.image = (EditText)findViewById(R.id.editcard_image);
		
		id.setText(Integer.toString(card.getId()));
		name.setText(card.getName());
		description.setText(card.getDescription());
		image.setText(card.getImage());
	}
}
