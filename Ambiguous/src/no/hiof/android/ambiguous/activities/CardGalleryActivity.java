package no.hiof.android.ambiguous.activities;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.adapter.GalleryAdapter;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.GridView;

//TODO: rename to gallery, fix which cards shown.
/**
 * Card gallery, shows all the cards available in game.
 */
public class CardGalleryActivity extends Activity {
	
	private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deckmanager);
        
        this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
        
        GridView deckmanager = (GridView)findViewById(R.id.deckmanager_grid);
        GalleryAdapter adapter = new GalleryAdapter(db);
        deckmanager.setAdapter(adapter);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.deck_manager, menu);
        return true;
    }
}
