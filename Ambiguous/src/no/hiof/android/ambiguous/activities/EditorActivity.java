package no.hiof.android.ambiguous.activities;

import java.util.List;

import no.hiof.android.ambiguous.Card;
import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.R.id;
import no.hiof.android.ambiguous.R.layout;
import no.hiof.android.ambiguous.R.menu;
import no.hiof.android.ambiguous.layouts.CardLayout;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;

public class EditorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Db db = Db.getDb(this.getApplicationContext());
        //db.dropTables();
        //db.createTables();
        List<Card> cards = db.getCards();
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
}
