package no.hiof.android.ambiguous.activities;

import java.util.ArrayList;
import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Player;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * The main menu that starts at startup of the app.
 */
public class MainActivity extends Activity {
	private SQLiteDatabase db;
	private boolean savedSessionExists;
	private Button resumeButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Ensure application is properly initialized with default settings
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		resumeButton = (Button) findViewById(R.id.resume_button);
		if(resumeButton != null){
			this.db = Db.getDb(getApplicationContext()).getReadableDatabase();
			Cursor c = db.rawQuery("SELECT id FROM " + "Session" + " ORDER BY id DESC Limit 1",
					null);
			c.moveToFirst();
			savedSessionExists = (c.getCount() <= 0 ? false : true);
			c.close();
			resumeButton.setEnabled(savedSessionExists);
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	/**
	 * Called when the user clicks the Resume button, resumes a previous not
	 * finished game
	 **/
	public void goToResumeGame(View view) {
		Cursor c = db
				.rawQuery(
						"SELECT id,player,opponent,turn,opponentCard,opponentDiscard FROM Session WHERE id = (SELECT max(id) FROM Session) LIMIT 1",
						null);
		c.moveToFirst();
		int playerId = c.getInt(c.getColumnIndexOrThrow("player"));
		int opponentId = c.getInt(c.getColumnIndexOrThrow("opponent"));
		int opponentCardId = c.getInt(c.getColumnIndexOrThrow("opponentCard"));
		Intent intent = new Intent(this,
				no.hiof.android.ambiguous.activities.GameActivity.class)
				.putExtra("SessionId", 
						c.getInt(c.getColumnIndexOrThrow("id")))
				.putExtra("SessionPlayer",
						c.getInt(c.getColumnIndexOrThrow("player")))
				.putExtra("SessionOpponent",
						c.getInt(c.getColumnIndexOrThrow("opponent")))
				.putExtra("SessionTurn",
						c.getInt(c.getColumnIndexOrThrow("turn")))
				.putExtra("SessionOpponentCard", 
						opponentCardId)
				.putExtra("SessionOpponentDiscard",
						(c.getInt(c.getColumnIndexOrThrow("opponentDiscard")) != 0 ? true
								: false));

		c.close();
		
		int playerUser = 0;
		int playerOpponent = 1;
		String[] name = new String[2];
		int[] health = new int[2];
		int[] armor = new int[2];
		int[] resources = new int[2];
		List<Card[]> hand = new ArrayList<Card[]>();
		List<Card> deckUser = new ArrayList<Card>();
		List<Card> deckOpponent = new ArrayList<Card>();
		
		c = db.rawQuery("SELECT name,health,armor,resources,deckid,handid FROM Player WHERE id = ?", new String[]{String.valueOf(playerId)});
		c.moveToFirst();

		name[playerUser] = c.getString(c.getColumnIndexOrThrow("name"));
		health[playerUser] = c.getInt(c.getColumnIndexOrThrow("health"));
		armor[playerUser] = c.getInt(c.getColumnIndexOrThrow("armor"));
		resources[playerUser] = c.getInt(c.getColumnIndexOrThrow("resources"));
		
		int handIdUser = c.getInt(c.getColumnIndexOrThrow("handid"));
		int deckIdUser = c.getInt(c.getColumnIndexOrThrow("deckid"));
		c.close();
		
		// Get the cards in the Player's hand
		c = db.rawQuery("SELECT cardid FROM Playercard WHERE sessioncardlistid = ? ORDER BY position ASC", 
				new String[]{String.valueOf(handIdUser)});
		if(c.moveToFirst()){
			CardDataSource cds = new CardDataSource(db);
			List<Card> tempHand = new ArrayList<Card>();
			while(!c.isAfterLast()){
				tempHand.add(cds.getCard(c.getInt(c.getColumnIndexOrThrow("cardid"))));
				c.moveToNext();
			}
			hand.add(0,tempHand.toArray(new Card[tempHand.size()]));
		}
		c.close();
		
		// Get the cards in the Player's deck
				c = db.rawQuery("SELECT cardid FROM Playercard WHERE sessioncardlistid = ? ORDER BY position ASC", 
						new String[]{String.valueOf(deckIdUser)});
				if(c.moveToFirst()){
					CardDataSource cds = new CardDataSource(db);
					List<Card> tempDeck = new ArrayList<Card>();
					while(!c.isAfterLast()){
						deckUser.add(cds.getCard(c.getInt(c.getColumnIndexOrThrow("cardid"))));
						c.moveToNext();
					}
				}
				c.close();
		
		Player player = new Player(	name[playerUser], health[playerUser],
									armor[playerUser], resources[playerUser], 
									hand.get(playerUser), deckUser);
		
		
		startActivity(intent);
	}

	/** Called when the user clicks the Game button, starts up a new game vs AI. **/
	public void goToGame(View view) {
		Intent intent = new Intent(this,
				no.hiof.android.ambiguous.activities.GameActivity.class);
		startActivity(intent);
	}

	/**
	 * Called when the user clicks the Cards button. Starts the "gallery"
	 * activity.
	 **/
	public void goToDeckManager(View view) {
		Intent intent = new Intent(this, CardGalleryActivity.class);
		startActivity(intent);
	}

	/** Called when the user clicks the network action from the action bar **/
	// TODO: Do something else with the actionbar.
	public void onActionNetworkClicked(MenuItem menuItem) {
		Intent intent = new Intent(this, NetworkActivity.class);
		startActivity(intent);
	}

	public void onSettingsClicked(MenuItem menuItem) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	/**
	 * Network button, starts a new network game.
	 * 
	 * @param view
	 */
	public void goToNetwork(View view) {
		Intent intent = new Intent(this, NetworkActivity.class);
		startActivity(intent);
	}

	/**
	 * 
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (this.db != null){
			Cursor c = db.rawQuery("SELECT id FROM " + "Session"
					+ " ORDER BY id DESC Limit 1", null);
			c.moveToFirst();
			savedSessionExists = (c.getCount() <= 0 ? false : true);
			c.close();
			if(resumeButton != null){
				resumeButton.setEnabled(savedSessionExists);
			}
		}
	}

}
