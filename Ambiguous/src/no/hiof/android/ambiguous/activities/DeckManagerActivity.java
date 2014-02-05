package no.hiof.android.ambiguous.activities;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.DeckmanagerAdapter;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.GridView;

public class DeckManagerActivity extends Activity {
	
	private CardDataSource cs;
	private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deckmanager);
        
        this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
        this.cs = new CardDataSource(db);
        
        GridView deckmanager = (GridView)findViewById(R.id.deckmanager_grid);
        DeckmanagerAdapter adapter = new DeckmanagerAdapter(db,R.layout.card);
        deckmanager.setAdapter(adapter);
        
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
    }
}
