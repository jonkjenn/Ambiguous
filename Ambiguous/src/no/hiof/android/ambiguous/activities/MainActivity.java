package no.hiof.android.ambiguous.activities;

import java.io.ObjectOutputStream.PutField;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.model.Player;
import android.app.Activity;
import android.content.ContentValues;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Ensure application is properly initialized with default settings
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		this.db = Db.getDb(getApplicationContext()).getReadableDatabase();
		Cursor c = db.rawQuery("SELECT max(id) FROM " + "Session" + " Limit 1",
				null);
		savedSessionExists = (c.getCount() <= 0 ? false : true);
		c.close();
		
		if (savedSessionExists) {
			Button resumeButton = (Button) findViewById(R.id.resume_button);
			resumeButton.setEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
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
						c.getInt(c.getColumnIndexOrThrow("opponentCard")))
				.putExtra(
						"SessionOpponentDiscard",
						(c.getInt(c.getColumnIndexOrThrow("opponentDiscard")) != 0 ? true : false));

		c.close();
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
		if(this.db != null){
			Cursor c = db.rawQuery("SELECT max(id) FROM " + "Session" + " Limit 1",
					null);
			savedSessionExists = (c.getCount() <= 0 ? false : true);
			c.close();
		}
	}
	
	
}
