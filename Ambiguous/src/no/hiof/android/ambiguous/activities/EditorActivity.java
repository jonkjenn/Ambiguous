package no.hiof.android.ambiguous.activities;

import java.util.List;

import no.hiof.android.ambiguous.Card;
import no.hiof.android.ambiguous.CardDataSource;
import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.layouts.CardLayout;

import android.os.Bundle;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MotionEvent;

public class EditorActivity extends Activity {
	
	private CardDataSource cs;
	private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        
        this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
        this.cs = new CardDataSource(db);
    }
    
    private void loadCards()
    {
        List<Card> cards = this.cs.getCards();
        CardLayout card = (CardLayout)findViewById(R.id.card1);
        card.setCard(cards.get(0));
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	// TODO Auto-generated method stub
    	return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	loadCards();
    }
}
