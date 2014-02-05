package no.hiof.android.ambiguous.activities;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.DeckmanagerAdapter;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.R.id;
import no.hiof.android.ambiguous.R.layout;
import no.hiof.android.ambiguous.R.menu;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.GridView;

public class GameActivity extends Activity {
	private SQLiteDatabase db;
	private CardDataSource cs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

        this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
        this.cs = new CardDataSource(db);
        
        GridView deckmanager = (GridView)findViewById(R.id.game_grid);
        DeckmanagerAdapter adapter = new DeckmanagerAdapter(db,R.layout.card_game);
        deckmanager.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

}
