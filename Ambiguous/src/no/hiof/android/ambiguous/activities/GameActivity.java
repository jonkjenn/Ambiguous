package no.hiof.android.ambiguous.activities;

import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.DeckBuilder;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.adapter.DeckmanagerAdapter;
import no.hiof.android.ambiguous.adapter.GameDeckAdapter;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Player;
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

        List<Card> cards = cs.getCards();

        Player player = new Player("Jon");
        player.SetDeck(DeckBuilder.StandardDeck(cards));
        
        GridView deckView = (GridView)findViewById(R.id.game_grid);
        GameDeckAdapter adapter = new GameDeckAdapter(player.GetCards());
        deckView.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

}
