package no.hiof.android.ambiguous.activities;

import java.util.ArrayList;
import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.GameMachine.OnStateChangeListener;
import no.hiof.android.ambiguous.GameMachine.State;
import no.hiof.android.ambiguous.LayoutHelper;
import no.hiof.android.ambiguous.MyWidgetProvider;
import no.hiof.android.ambiguous.OpponentController;
import no.hiof.android.ambiguous.OpponentController.OpponentListener;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.fragments.CardHandFragment;
import no.hiof.android.ambiguous.fragments.DragFragment;
import no.hiof.android.ambiguous.fragments.DragFragment.OnDragStatusChangedListener;
import no.hiof.android.ambiguous.fragments.DragFragment.OnPlayerUsedCardListener;
import no.hiof.android.ambiguous.fragments.GPGControllerFragment;
import no.hiof.android.ambiguous.fragments.LANFragment;
import no.hiof.android.ambiguous.fragments.MinigameFragment;
import no.hiof.android.ambiguous.fragments.MinigameFragment.MinigameListener;
import no.hiof.android.ambiguous.fragments.PlayerStatsFragment;
import no.hiof.android.ambiguous.fragments.PlayerStatsFragment.OnLoadedListener;
import no.hiof.android.ambiguous.fragments.SinglePlayerFragment;
import no.hiof.android.ambiguous.fragments.TutorialFragment;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Initializes games and shows/handles the game UI.
 */
public class GameActivity extends ActionBarActivity implements
		GameMachine.GameMachineListener, OpponentListener,
		GameMachine.OnPlayerUpdates, MinigameListener,
		OnPlayerUsedCardListener, OnDragStatusChangedListener,
		OnStateChangeListener {
	private SQLiteDatabase db;

	public static OpponentController opponentController;

	// The outer layout around the whole game view.
	private RelativeLayout layoutContainer;
	public static GameMachine gameMachine;

	private TextView resultTextView;

	private ImageView opponentCard;

	LANFragment lanFragment;
	SinglePlayerFragment singlePlayerFragment;
	GPGControllerFragment gpgFragment;
	CardHandFragment cardHandFragment;
	DragFragment dragFragment;

	PlayerStatsFragment playerStats;
	PlayerStatsFragment opponentStats;

	// During minigame we lock the rotation, store the previous rotation in this
	// so we can reset it after minigame.
	// TODO: Store this during pause. Both in bundle and in database.
	private int previousRotation;

	// Google Play Game Service
	private boolean useGPGS = false;

	// Network settings
	private boolean isNetwork;

	CardDataSource cs;
	public static List<Card> cards;

	private Bundle thing;

	// We check API level in code
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		// Find all the views we use so we only have to find them once
		findViews();

		// We dont want the actionbar visible during the game
		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();

		// We load data after setting up UI hooks so that UI can react to
		// current state.
		if (savedInstanceState != null) {
			this.thing = savedInstanceState;
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		loadDb();

		if (gameMachine == null) {
			gameMachine = new GameMachine(cards);
			gameMachine.setOnPlayerUpdatesListener(this);
		}
		if (opponentController == null) {
			opponentController = new OpponentController();
		}
		setupUIListeners();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setupDragFragment();
			if ((!useGPGS) && (!isNetwork)) {
				SharedPreferences sp = PreferenceManager
						.getDefaultSharedPreferences(this);
				int dmgBuff = sp.getInt(SettingsActivity.KEY_PREF_CHEAT, -1);
				if (dmgBuff > 0) {
					if (dmgBuff == 249) {
						Toast.makeText(
								this,
								"Cheat is set to do 249 damage. Will not prevent this game being saved",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(
								this,
								"Warning: Cheat enabled and set to do "
										+ String.valueOf(dmgBuff)
										+ " extra damage. If you use a card now the result of this game will not be saved",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		}

		cardHandFragment.setOnPlayerUsedCardListener(this);

		loadPlayerStatsFragments();

		setBackground(PreferenceManager.getDefaultSharedPreferences(this));

		if (this.thing != null) {
			loadGameStateBundle(this.thing);
		}

		this.useGPGS = getIntent().getBooleanExtra("useGPGS", false);
		this.isNetwork = getIntent().getBooleanExtra("isNetwork", false);

		if (this.isNetwork) {// We start a LAN Network game
			startNetworkFragment();

		} else if (useGPGS) {// We start a game with Google Play Game Service
			startGPGFragment();

		} else {// Single player against AI
			startSinglePlayerFragment();
		}
	}

	void loadPlayerStatsFragments() {
		playerStats = (PlayerStatsFragment) getSupportFragmentManager()
				.findFragmentByTag("playerStatsFragment");

		// We create new fragments.
		if (playerStats == null) {

			// The stats window showing player's stats.
			playerStats = new PlayerStatsFragment();

			Bundle args = new Bundle();
			args.putBoolean("reverse", true);
			playerStats.setArguments(args);

			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.playerstats_fragment, playerStats,
							"playerStatsFragment").commit();

			// The stats window showing opponent's stats.
			opponentStats = new PlayerStatsFragment();

			// So we can update stats as soon as the fragment is done loading.
			opponentStats.setOnLoadedListener(new OnLoadedListener() {
				@Override
				public void onLoaded() {
					onStatsUpdateListener(gameMachine.opponent);
				}
			});

			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.opponentstats_fragment, opponentStats,
							"opponentStatsFragment").commit();
		} else {// We use the fragments that already exists
			opponentStats = (PlayerStatsFragment) getSupportFragmentManager()
					.findFragmentByTag("opponentStatsFragment");
		}

		// So we can update stats as soon as the fragment is done loading.
		playerStats.setOnLoadedListener(new OnLoadedListener() {
			@Override
			public void onLoaded() {
				onStatsUpdateListener(gameMachine.player);
			}
		});

		// So we can update stats as soon as the fragment is done loading.
		opponentStats.setOnLoadedListener(new OnLoadedListener() {
			@Override
			public void onLoaded() {
				onStatsUpdateListener(gameMachine.opponent);
				onStateChanged(gameMachine.state);
			}
		});
	}

	void loadDb() {
		if (this.db == null) {
			// Gets the db that will be reused throughout the game.
			this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
			cs = new CardDataSource(db);
			cards = cs.getCards();
		}
	}

	void setupDragFragment() {

		dragFragment = (DragFragment) getSupportFragmentManager()
				.findFragmentByTag("dragFragment");

		if (dragFragment == null) {
			// Have to start fragment in code since older version cant seem to
			// handle it being in xml
			dragFragment = new DragFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.drag_container, dragFragment, "dragFragment")
					.commit();

		}

		dragFragment.setPlayerUsedCardListener(this);
		dragFragment.setOnDragStatusChanged(this);
	}

	// TODO: Could be better type testing.
	/**
	 * Starts the network fragment
	 * 
	 */
	private void startNetworkFragment() {

		lanFragment = (LANFragment) getSupportFragmentManager()
				.findFragmentByTag("LANFragment");

		if (lanFragment == null) {

			if (!getIntent().hasExtra("address")
					|| !getIntent().hasExtra("port")
					|| !getIntent().hasExtra("isServer")) {
				return;
			}

			lanFragment = new LANFragment();
			lanFragment.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.add(lanFragment, "LANFragment").commit();
		}
	}

	void startGPGFragment() {
		gpgFragment = (GPGControllerFragment) getSupportFragmentManager()
				.findFragmentByTag("GPGControllerFragment");
		if (gpgFragment == null) {
			gpgFragment = new GPGControllerFragment();
			setOnActivityResultListener(gpgFragment);
			gpgFragment.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.add(gpgFragment, "GPGControllerFragment").commit();
		}
	}

	void startSinglePlayerFragment() {

		singlePlayerFragment = (SinglePlayerFragment) getSupportFragmentManager()
				.findFragmentByTag("singlePlayerFragment");
		if (singlePlayerFragment == null) {

			singlePlayerFragment = new SinglePlayerFragment();
			singlePlayerFragment.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.add(singlePlayerFragment, "singlePlayerFragment").commit();
		}
	}

	/**
	 * Find all the views we use so we only have to find them once
	 */
	void findViews() {
		layoutContainer = (RelativeLayout) findViewById(R.id.game_layout_container);
		opponentCard = (ImageView) findViewById(R.id.opponent_card);
		resultTextView = (TextView) findViewById(R.id.main_result_text);

		cardHandFragment = (CardHandFragment) getSupportFragmentManager()
				.findFragmentById(R.id.cardhand_fragment);
	}

	void setupUIListeners() {
		removeUIListeners();
		// Listen to gamemachine for changes that should be reflect in UI.
		gameMachine.setGameMachineListener(this);
		gameMachine.setOnStateChangeListener(this);

		// Listen to player and opponent for changes that should be reflected in
		// UI.
		gameMachine.setOnPlayerUpdatesListener(this);
		opponentController.setOpponentListener(gameMachine);
		opponentController.setOpponentListener(this);
	}

	/**
	 * Remove all the listeners, so we avoid leaks.
	 */
	void removeUIListeners() {

		if (gameMachine != null) {
			gameMachine.removeGameMachineListener(this);
			gameMachine.setOnPlayerUpdatesListener(null);
		}

		if (opponentController != null) {
			opponentController.removeOpponentListener(this);
			;
		}
	}

	/**
	 * Displays the tutorial
	 */
	public void showTutorialButton(View view) {
		// Cant use cards while tutorial shown.
		disableUseCards();

		final FragmentManager manager = getSupportFragmentManager();

		final TutorialFragment tutorial = new TutorialFragment();
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(R.id.game_layout_container, tutorial, "tutorial")
				.addToBackStack("showTutorial");
		transaction.commit();
	}

	/**
	 * Closes the tutorial fragment.
	 */
	public void closeTutorialButton(View view) {
		FragmentManager manager = getSupportFragmentManager();
		manager.popBackStack("showTutorial",
				FragmentManager.POP_BACK_STACK_INCLUSIVE);
		FragmentTransaction t = manager.beginTransaction();
		t.remove(manager.findFragmentByTag("tutorial"));
		t.commitAllowingStateLoss();// Since its only a
									// tutorial.

		onStateChanged(gameMachine.state);
	}

	// Used three lines rather than a switch, all values are safe to parse
	private void setBackground(SharedPreferences sp) {
		String string = sp.getString(SettingsActivity.KEY_PREF_BGColor, "none");
		if (!string.equals("none")) {
			try {
				int color = Color.parseColor(string);
				layoutContainer.setBackgroundColor(color);
			} catch (IllegalArgumentException e) {
				layoutContainer.setBackgroundColor(0);
			}

		} else {
			layoutContainer.setBackgroundColor(0);
		}

	}

	/**
	 * Updates GUI with the card the opponent has played.
	 * 
	 * @param card
	 */
	private void opponentPlayCard(Card card) {
		if (card == null) {
			opponentCard.setImageBitmap(null);
			return;
		}

		opponentCard.setImageBitmap(CardLayout.getCardBitmap(card,
				layoutContainer));
		// Hide the discard graphic since we're not discarding.
		findViewById(R.id.discard).setVisibility(View.INVISIBLE);
	}

	/**
	 * Updates GUI with the card the opponent has discarded.
	 * 
	 * @param card
	 */
	private void opponentDiscardCard(Card card) {
		if (card == null) {
			opponentCard.setImageBitmap(null);
			return;
		}
		opponentCard.setImageBitmap(CardLayout.getCardBitmap(card,
				layoutContainer));
		// Show the discard graphic since we're discarding.
		findViewById(R.id.discard).setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	/**
	 * Player plays a card.
	 * 
	 * Checks for the minigame card. Check if the phone access to the gravity
	 * sensor which is needed for the minigame. If not the minigame card is
	 * played as a regular card with random damage in its defined range.
	 * 
	 * @param position
	 *            The position of the card in the players hand.
	 */
	public void playCard(int position) {
		if (gameMachine.player.getCard(position).name.equals("Minigame!")
				&& getPackageManager().hasSystemFeature(
						PackageManager.FEATURE_SENSOR_ACCELEROMETER)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			startMinigame(position);
		} else {
			gameMachine.playerPlayCard(position);
		}
	}

	/**
	 * Start the minigame fragment.
	 * 
	 * @param cardPosition
	 *            The position of the card in the players hand.
	 */
	// With lower API versions we just randomize damage instead of use minigame
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void startMinigame(int cardPosition) {
		// The minigame can only be played in portrait mode.
		// TODO: Implement locking according to the current screen rotation, the
		// important part is that orientation does not change during the
		// minigame.
		previousRotation = getRequestedOrientation();

		int rotation = getWindowManager().getDefaultDisplay().getRotation();

		// API8 version is missing the constants for reverse screen layouts.
		int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
				: 8);
		int SCREEN_ORIENTATION_REVERSE_PORTRAIT = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
				: 9);

		// We try to lock the screen into its current rotation to prevent screen
		// rotations happening during the minigame.
		switch (getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			setRequestedOrientation(rotation == Surface.ROTATION_90 ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
					: SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			setRequestedOrientation(rotation == Surface.ROTATION_0 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
					: SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			break;
		}

		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		Effect e = gameMachine.player.getCard(cardPosition).effects.get(0);
		MinigameFragment minigame = new MinigameFragment();
		Bundle b = new Bundle();
		b.putInt("min", e.minValue);
		b.putInt("max", e.maxValue);
		b.putInt("pos", cardPosition);
		minigame.setArguments(b);

		transaction.add(R.id.game_layout_container, minigame, "minigame");
		transaction.commit();
	}

	// If try to use a card we dont have enough resources for etc.
	@Override
	public void onCouldNotPlayCardListener(int position) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			dragFragment.stopDrag(position);
		} else {
			cardHandFragment.enableUseCards();
		}

	}

	// Background behind player's name turns red on their turn.
	// Version is checked in code.
	@SuppressLint("NewApi")
	@Override
	public void onPlayerTurnListener() {
	}

	@SuppressLint("NewApi")
	// We check in code
	@Override
	public void onPlayerDoneListener() {
		// To make winning easier while testing, opponent can be set to take
		// additional dmg each turn.
		// Will prevent saving a victory if the cheat has been used.
		// For later reference simply search the tag below to jump here directly
		// TAG: damage CHEAT
		if ((!useGPGS) && (!isNetwork)) {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);
			int dmg = sp.getInt(SettingsActivity.KEY_PREF_CHEAT, -1);
			// Only enable cheat if we are in a local game, and the damage is
			// set to a positive number of significance
			if (dmg > 0) {
				int dmgBuff = sp.getInt(SettingsActivity.KEY_PREF_CHEAT, -1);
				// Only enable cheat if we are in a local game, and the damage
				// is set to a positive number of any significance
				if (dmgBuff > 0) {
					// Added workaround, selecting dmg 249 will now trigger the
					// cheat, but still allow the game to be saved
					// This is purely for testing purposes
					if (dmgBuff == 249) {
						gameMachine.opponent.damage(dmgBuff);
					} else {
						gameMachine.opponent.damage(dmgBuff);
						sp.edit().putBoolean("cheatUsed", true).commit();

						if (gameMachine.cheatUsed != true) {
							gameMachine.cheatUsed = true;
							Toast.makeText(
									this,
									"Warning: "
											+ getResources()
													.getString(
															R.string.toast_message_disregard_outcome),
									Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		}
	}

	/**
	 * Disables using cards by dragdrop or longclick.
	 */
	void disableUseCards() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			dragFragment.disableDrag();
		}
		cardHandFragment.disableUseCards();
	}

	/**
	 * Enables using cards by dragdrop or longclick.
	 */
	void enableUseCards() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			dragFragment.enableDrag();
		}
		cardHandFragment.enableUseCards();
	}

	/**
	 * Resets the layout.
	 */
	public void resetLayout() {
		LayoutHelper.hideResult(resultTextView);
		playerStats.notMyTurn();
		opponentStats.notMyTurn();
		opponentCard.setImageBitmap(null);
		// Hide the discard graphic since we're not discarding.
		findViewById(R.id.discard).setVisibility(View.INVISIBLE);
	}

	@Override
	public void onOpponentTurnListener() {
	}

	@Override
	public void onPlayerDeadListener(Player player) {
		LayoutHelper.showResult(resultTextView, false);
		cardHandFragment.disableUseCards();
	}

	@Override
	public void onOpponentDeadListener(Player opponent) {
		LayoutHelper.showResult(resultTextView, true);
		cardHandFragment.disableUseCards();
		if (!isNetwork && !useGPGS) {
			if (!PreferenceManager.getDefaultSharedPreferences(
					getApplicationContext()).getBoolean("cheatUsed",
					GameActivity.gameMachine.cheatUsed)) {
				saveVictory();
			} else {
				Toast.makeText(
						this,
						"Reminder: "
								+ getResources()
										.getString(
												R.string.toast_message_disregard_outcome),
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	private void saveVictory() {
		int victory = -1;
		int prevVictory = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getInt("WIN", -1);
		try {
			String whereClause = "WHERE id = (SELECT id FROM Statistics ORDER BY id DESC LIMIT 1)";
			db.beginTransaction();
			Cursor c = db.rawQuery("SELECT win FROM Statistics " + whereClause,
					null);
			if (c.moveToFirst()) {
				victory = c.getInt(c.getColumnIndex("win"));
				victory++;
			}
			c.close();
			db.execSQL("UPDATE Statistics SET win = " + String.valueOf(victory)
					+ " " + whereClause);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		// Store the amount of victories in sharedpreferences for ease of access
		PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
				.edit().putInt("WIN", victory).commit();
		if (victory > 0 && prevVictory == victory - 1 && victory % 10 == 0) {
			NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(
					this)
					.setSmallIcon(R.drawable.plus_drawing)
					.setContentTitle("Congratulations!")
					.setContentText(
							"You have managed to win "
									+ String.valueOf(victory)
									+ " games, good job!");

			// Creates an explicit intent for an Activity in your app
			Intent intent = new Intent(this, MainActivity.class).addCategory(
					Intent.CATEGORY_LAUNCHER).setAction(Intent.ACTION_MAIN);

			PendingIntent pendingIntent = PendingIntent.getActivity(this,
					victory, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			nBuilder.setContentIntent(pendingIntent);
			NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nManager.notify(victory, nBuilder.build());

		}

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		RemoteViews remoteViews = new RemoteViews(this.getPackageName(),
				R.layout.widget_layout);
		ComponentName thisWidget = new ComponentName(this,
				MyWidgetProvider.class);
		remoteViews.setTextViewText(R.id.widget_layout_textview,
				"Total victories: " + String.valueOf(victory));
		appWidgetManager.updateAppWidget(thisWidget, remoteViews);

	}

	// We check in code
	@SuppressLint("NewApi")
	@Override
	public void onPlayerPlayedCard(Card card) {
	}

	@Override
	public void onPlayerDiscardCard(Card card) {
	}

	@Override
	public void onOpponentPlayCard(Card card, boolean generateDamage) {
		opponentPlayCard(card);
	}

	@Override
	public void onOpponentDiscardCard(int card) {
		opponentDiscardCard(CardDataSource.getCard(card));
	}

	@Override
	public void onCardsUpdateListener(Player player, Card[] cards) {
		// Update the cards on the screen if the player's cards change.
		if (player == gameMachine.player) {
			cardHandFragment.updateCards(cards);
		}
	}

	@Override
	public void onStatsUpdateListener(Player player) {
		if (playerStats == null || opponentStats == null) {
			return;
		}
		if (player == gameMachine.player) {

			// playerName.setText(player.name);
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);

			playerStats.setStats(
					sp.getString(SettingsActivity.KEY_PREF_USER, "Player"),
					player.health, player.armor, player.resources);

		} else {
			opponentStats.setStats(player.name, player.health, player.armor,
					player.resources);
		}
	}

	@Override
	public void onStatChange(Player player, int amount, EffectType type) {
		if (player == gameMachine.player) {
			playerStats.updateStat(type, amount);
		} else {
			opponentStats.updateStat(type, amount);
		}
	}

	@Override
	public void onOpponentUsedEffect(EffectType type, Player target,
			int amount, boolean onlyDisplay) {
		if (!onlyDisplay) {
			return;
		}
		onStatChange(target, amount, type);
	}

	@Override
	public void onOpponentTurnDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerUsedeffect(EffectType type, Player target, int amount) {
		// TODO Auto-generated method stub

	}

	// We check in code
	@SuppressLint("NewApi")
	@Override
	public void onGameEnd(int amount, int position) {
		gameMachine.playerPlayCard(position, amount);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			FragmentTransaction tr = getSupportFragmentManager()
					.beginTransaction();
			tr.remove(getSupportFragmentManager().findFragmentByTag("minigame"));
			tr.commitAllowingStateLoss();
		}
		// Set the requested orientation back to what it was before minigame.
		setRequestedOrientation(previousRotation);
	}

	@Override
	protected void onStop() {
		super.onStop();

		removeUIListeners();

		if (isFinishing()) {
			// Clear the static cache, this could possibly be tied to activity
			// life cycle either directly or by fragment
			cs.purge();

			gameMachine = null;
			opponentController = null;
		}
	}

	public static void showGenericDialog(Context context, String title,
			String message) {
		Builder b = new Builder(context);
		b.setCancelable(false).setPositiveButton("OK", null).setTitle(title)
				.setMessage(message).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		notifyActivityResult(requestCode, resultCode, data);
	}

	// Get from DragFragment if player use or discard card.
	@Override
	public void onPlayerUsedCard(int card, boolean discard) {
		if (!discard) {
			playCard(card);
		} else {
			gameMachine.playerDiscardCard(card);
		}
	}

	@Override
	public void onDragStatusChanged(int card, int status) {
		switch (status) {
		case DragFragment.DRAG_STARTED:
			cardHandFragment.hideCard(card);
			break;
		case DragFragment.DRAG_STOPPED:
			cardHandFragment.showCard(card);
			break;
		}

	}

	@Override
	public void previousCardPlayed(Card card, boolean discarded) {
		if (!discarded) {
			opponentPlayCard(card);
		} else {
			opponentDiscardCard(card);
		}
	}

	@Override
	public void onStateChanged(State state) {
		if (state == State.PLAYER_TURN) {
			enableUseCards();
			playerStats.myTurn();
			opponentStats.notMyTurn();

		} else {
			disableUseCards();
			playerStats.notMyTurn();
			opponentStats.myTurn();
		}
	}

	/**
	 * Problems with fragments not getting OnActivityResults so we pass it
	 * "manually"
	 */
	public interface OnActivityResultListener {
		void onGameActivityResult(int requestCode, int resultCode, Intent data);
	}

	void notifyActivityResult(int requestCode, int resultCode, Intent data) {
		for (OnActivityResultListener l : onActivityResultListeners) {
			l.onGameActivityResult(requestCode, resultCode, data);
		}
	}

	final List<OnActivityResultListener> onActivityResultListeners = new ArrayList<OnActivityResultListener>();

	public void setOnActivityResultListener(OnActivityResultListener listener) {
		if (!onActivityResultListeners.contains(listener)) {
			onActivityResultListeners.add(listener);
		}
	}

	public void unsetOnActivityResultListener(OnActivityResultListener listener) {
		onActivityResultListeners.remove(listener);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (gameMachine != null && gameMachine.player != null
				&& gameMachine.opponent != null && gameMachine.state != null) {
			// We store stuff so that can resume later.
			outState.putParcelable("Player", GameActivity.gameMachine.player);
			outState.putParcelable("Opponent",
					GameActivity.gameMachine.opponent);
			outState.putInt("State", GameActivity.gameMachine.state.ordinal());
			outState.putParcelable("OpponentCard",
					GameActivity.gameMachine.currentOpponentCard);
			outState.putBoolean("OpponentDiscarded",
					GameActivity.gameMachine.opponentCardIsDiscarded);
		}
	}

	void loadGameStateBundle(Bundle extras) {
		gameMachine.player.updatePlayer((Player) extras.get("Player"));
		gameMachine.opponent.updatePlayer((Player) extras.get("Opponent"));
		gameMachine.state = GameMachine.State.values()[extras.getInt("State")];
		Card currentOpponentCard = (Card) extras.getParcelable("OpponentCard");
		boolean opponentCardIsDiscarded = extras.getBoolean("OpponentDiscard",
				false);

		if (currentOpponentCard != null) {
			opponentController.previousCardPlayed(currentOpponentCard,
					opponentCardIsDiscarded);
		}
	}
}
