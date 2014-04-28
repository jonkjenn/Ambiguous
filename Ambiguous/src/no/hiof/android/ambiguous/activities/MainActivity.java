package no.hiof.android.ambiguous.activities;

import java.util.ArrayList;
import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GPGService;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.datasource.CardDataSource.OnLoadCompleteListener;
import no.hiof.android.ambiguous.fragments.SettingsFragment;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Player;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * The main menu that starts at startup of the app.
 */
public class MainActivity extends Activity {
	private SQLiteDatabase db;
	private Button resumeButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Ensure application is properly initialized with default settings
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		resumeButton = (Button) findViewById(R.id.resume_button);
		if (resumeButton != null) {
			this.db = Db.getDb(getApplicationContext()).getReadableDatabase();
		}

		//We disable the google play part from API levels below 16 because we have not tested for it.
		//Google play on emulator is not supported well in lower versions so we have not been able to test it.
		//It might very well work on lower API levels.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			findViewById(R.id.play_google_button).setVisibility(View.GONE);
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

		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				loadData();
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
			}
		}.execute();

	}

	void loadData() {

		Cursor c = db
				.rawQuery(
						"SELECT id,player,opponent,turn,opponentCard,opponentDiscard,cheatUsed FROM Session WHERE id = (SELECT max(id) FROM Session) LIMIT 1",
						null);
		if (!c.moveToFirst()) {
			c.close();
			abortResumeGame();
			return;
		}
		// Extract all the data from the session row and then close the
		// cursor
		final int playerId = c.getInt(c.getColumnIndexOrThrow("player"));
		final int opponentId = c.getInt(c.getColumnIndexOrThrow("opponent"));
		final int opponentCardId = c.getInt(c
				.getColumnIndexOrThrow("opponentCard"));
		final int sessionId = c.getInt(c.getColumnIndexOrThrow("id"));
		final int sessionTurn = c.getInt(c.getColumnIndexOrThrow("turn"));
		final int opponentDiscard = c.getInt(c
				.getColumnIndexOrThrow("opponentDiscard"));
		final int cheatUsed = c.getInt(c.getColumnIndexOrThrow("cheatUsed"));

		c.close();

		// CardDataSource has to be initialized at least once before calling the
		// static method CardDataSource.getCard(id), or else the app throws a
		// nullpointerException
		CardDataSource cds = new CardDataSource(db);
		cds.setOnLoadCompleteListener(new OnLoadCompleteListener() {

			@Override
			public void onLoadComplete() {
				// Put everything together and pass it along as extras in the
				// intent
				final Card sessionOpponentCard = CardDataSource
						.getCard(opponentCardId);
				final boolean sessionOpponentDiscard = (opponentDiscard != 0 ? true
						: false);

				new AsyncTask<Void, Void, Player[]>() {

					@Override
					protected Player[] doInBackground(Void... params) {
						Player player = getPlayerFromDb(playerId);
						Player opponent = getPlayerFromDb(opponentId);
						if (player == null || opponent == null) {
							abortResumeGame();
							return null;
						}
						return new Player[] { player, opponent };
					}

					@Override
					protected void onPostExecute(Player[] players) {
						super.onPostExecute(players);
						if (players != null) {
							boolean sessionCheatUsed = (cheatUsed != 0 ? true
									: false);

							loadPreferenceName(players[0]);
							loadCheatUsed(sessionCheatUsed);
							startActivity(createMainActivityIntent(sessionId,
									players[0], players[1], sessionTurn,
									sessionOpponentCard,
									sessionOpponentDiscard, sessionCheatUsed));

						}
					}
				}.execute();

			}
		});
		cds.loadData();
	}

	Intent createMainActivityIntent(int sessionId, Player player,
			Player opponent, int sessionTurn, Card sessionOpponentCard,
			boolean sessionOpponentDiscard, boolean sessionCheatUsed) {
		return new Intent(MainActivity.this,
				no.hiof.android.ambiguous.activities.GameActivity.class)
				.putExtra("SessionId", sessionId)
				.putExtra("SessionPlayer", (Parcelable) player)
				.putExtra("SessionOpponent", (Parcelable) opponent)
				.putExtra("SessionTurn", sessionTurn)
				.putExtra("SessionOpponentCard",
						(Parcelable) sessionOpponentCard)
				.putExtra("SessionOpponentDiscard", sessionOpponentDiscard)
				.putExtra("SessionCheatUsed", sessionCheatUsed);

	}

	void loadPreferenceName(Player player) {
		// If there is a stored name in sharedpreferences, use that
		// instead.
		player.name = PreferenceManager.getDefaultSharedPreferences(
				MainActivity.this).getString(SettingsFragment.KEY_PREF_USER,
				player.name);
	}

	boolean loadCheatUsed(boolean defaultValue) {
		// Use sharedpreference if it's set, else rely on the db
		return PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
				.getBoolean("cheatUsed", defaultValue);
	}

	/**
	 * Called if a check confirms that a game is unable to be resumed, disables
	 * the resume button and informs the user with a toast
	 */
	private void abortResumeGame() {
		resumeButton.setEnabled(false);
		Toast.makeText(this, "Could not resume game, sorry", Toast.LENGTH_LONG)
				.show();

	}

	/**
	 * Attempts to build a player object from the db, will return null if there
	 * is insufficient data
	 * 
	 * @param playerId
	 * @return
	 */
	public Player getPlayerFromDb(int playerId) {
		String name;
		int health;
		int armor;
		int resources;
		Card[] hand;
		List<Card> deck;

		Cursor c = db
				.rawQuery(
						"SELECT name,health,armor,resources,deckid,handid FROM Player WHERE id = ?",
						new String[] { String.valueOf(playerId) });
		if (!c.moveToFirst()) {
			c.close();
			return null;
		}

		name = c.getString(c.getColumnIndexOrThrow("name"));
		health = c.getInt(c.getColumnIndexOrThrow("health"));
		armor = c.getInt(c.getColumnIndexOrThrow("armor"));
		resources = c.getInt(c.getColumnIndexOrThrow("resources"));

		int handId = c.getInt(c.getColumnIndexOrThrow("handid"));
		int deckId = c.getInt(c.getColumnIndexOrThrow("deckid"));
		c.close();

		List<Card> handCards = getCardsFromDb(handId);
		if (handCards == null) {
			return null;
		}
		// hand is currently stored as a Card[] in Player.hand
		hand = handCards.toArray(new Card[handCards.size()]);

		deck = getCardsFromDb(deckId);
		if (deck == null) {
			return null;
		}

		return new Player(name, health, armor, resources, hand, deck);
	}

	/**
	 * playerCardListId is the id associated with a specific player's list of
	 * cards, which is either the list of cards in the player's hand, or the
	 * list of cards in the player's deck i.e handid and deckid from the Player
	 * table.
	 * 
	 * @param playerCardListId
	 * @return
	 */
	private List<Card> getCardsFromDb(int playerCardListId) {
		List<Card> tempList;
		// sessioncardlistid is synonymous with playercardlistid
		Cursor c = db
				.rawQuery(
						"SELECT cardid FROM Playercard WHERE sessioncardlistid = ? ORDER BY position ASC",
						new String[] { String.valueOf(playerCardListId) });
		if (!c.moveToFirst()) {
			c.close();
			return null;
		}
		tempList = new ArrayList<Card>();
		while (!c.isAfterLast()) {
			int cardId = c.getInt(c.getColumnIndexOrThrow("cardid"));
			tempList.add(CardDataSource.getCard(cardId));
			c.moveToNext();
		}
		c.close();
		return tempList;
	}

	/** Called when the user clicks the Game button, starts up a new game vs AI. **/
	public void goToGame(View view) {
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.edit().putBoolean("cheatUsed", false).commit();
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

	public void goToGoogle(View view) {
		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra("useGPGS", true);
		startActivity(intent);
	}

	/**
	 * 
	 */
	@Override
	protected void onResume() {
		super.onResume();

		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				if (MainActivity.this.db != null) {
					Cursor c = db.rawQuery("SELECT id FROM " + "Session"
							+ " ORDER BY id DESC Limit 1", null);
					boolean exist = c.moveToFirst();
					c.close();

					return exist;
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (resumeButton != null) {
					resumeButton.setEnabled(result);
				}
			}

		}.execute();

		if (GPGService.isRunning) {
			findViewById(R.id.close_gpg_service_button).setVisibility(
					View.VISIBLE);
		}
	}

	public void stopGPGService(View view) {
		Intent close = new Intent(this, GPGService.class);
		close.setAction(GPGService.CLOSE);
		startService(close);

		findViewById(R.id.close_gpg_service_button).setVisibility(View.GONE);
	}
}
