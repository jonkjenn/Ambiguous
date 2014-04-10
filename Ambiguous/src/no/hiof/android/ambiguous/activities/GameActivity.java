package no.hiof.android.ambiguous.activities;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

import no.hiof.android.ambiguous.AlarmReceiver;
import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.GameMachine.State;
import no.hiof.android.ambiguous.HandDragListener;
import no.hiof.android.ambiguous.OpponentController.OpponentListener;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.adapter.GameDeckAdapter;
import no.hiof.android.ambiguous.datasource.SessionDataSource;
import no.hiof.android.ambiguous.fragments.MinigameFragment;
import no.hiof.android.ambiguous.fragments.MinigameFragment.MinigameListener;
import no.hiof.android.ambiguous.fragments.TutorialFragment;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;
import no.hiof.android.ambiguous.model.Player.PlayerUpdateListener;
import no.hiof.android.ambiguous.network.CloseServerSocketTask;
import no.hiof.android.ambiguous.network.CloseSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask.OpenSocketListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Initializes games and shows/handles the game UI.
 */
public class GameActivity extends ActionBarActivity implements
		GameMachine.GameMachineListener, OpponentListener,
		PlayerUpdateListener, OpenSocketListener, MinigameListener {
	private SQLiteDatabase db;

	// Shows the cards on players "hand"
	private GridView handView;

	// The outer layout around the whole game view.
	private RelativeLayout layoutContainer;
	private GameMachine gameMachine;

	private TextView playerName;
	private TextView playerHealth;
	private TextView playerArmor;
	private TextView playerResource;
	private ViewGroup floatingHealthPlayer;
	private ViewGroup floatingArmorPlayer;
	private ViewGroup floatingResourcehPlayer;

	private ImageView opponentCard;

	// We keep track of current Card the opponent has played so we can restore
	// the view on orientation changes and pauses.
	private Card currentOpponentCard;
	private boolean opponentCardIsDiscarded = false;

	private TextView opponentName;
	private TextView opponentHealth;
	private TextView opponentArmor;
	private TextView opponentResource;
	private ViewGroup floatingHealthOpponent;
	private ViewGroup floatingArmorOpponent;
	private ViewGroup floatingResourcehOpponent;

	// TODO: Possibly recode so don't need these as
	// fields.http://stackoverflow.com/questions/5088856/how-to-detect-landscape-left-normal-vs-landscape-right-reverse-with-support?rq=1
	private Player savedPlayer;
	private Player savedOpponent;
	private State savedState;
	private int savedSessionId = -1;

	private HandDragListener handDragListener;

	// TODO: playerStatus currently used for status messages, opponentStatus
	// indicates turns. Should rename!
	TextView playerStatus;

	// During minigame we lock the rotation, store the previous rotation in this
	// so we can reset it after minigame.
	// TODO: Store this during pause. Both in bundle and in database.
	private int previousRotation;

	// Network settings
	private boolean isNetwork;
	private String address;
	private int port;
	private boolean isServer;
	private Socket socket;
	private ServerSocket server;

	private OpenSocketTask openSocketTask;

	private AlertDialog waitingForNetwork;

	/*
	 * private static final String KEY_TEXT_PLAYER_VALUE = "playerTextValue";
	 * private static final String KEY_TEXT_OPPONENT_VALUE =
	 * "opponentTextValue";
	 */

	// We check API level in code
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		cancelAnnoyingNotification();

		layoutContainer = (RelativeLayout) findViewById(R.id.game_layout_container);
		playerStatus = (TextView) findViewById(R.id.stats_player);
		handView = (GridView) findViewById(R.id.game_grid);

		opponentCard = (ImageView) findViewById(R.id.opponent_card);

		// TODO: What?
		playerStatus.setText(" ");
		setBackground(PreferenceManager.getDefaultSharedPreferences(this));

		if (savedInstanceState != null) {
			loadSavedData(savedInstanceState);
		}

		// Find all views so we only have to find them once.

		playerName = (TextView) findViewById(R.id.stat_player_name);
		playerHealth = (TextView) findViewById(R.id.stat_player_health);
		playerArmor = (TextView) findViewById(R.id.stat_player_armor);
		playerResource = (TextView) findViewById(R.id.stat_player_resource);
		floatingHealthPlayer = (ViewGroup) findViewById(R.id.floating_health_player);
		floatingArmorPlayer = (ViewGroup) findViewById(R.id.floating_armor_player);
		floatingResourcehPlayer = (ViewGroup) findViewById(R.id.floating_resource_player);

		opponentName = (TextView) findViewById(R.id.stat_opponent_name);
		opponentHealth = (TextView) findViewById(R.id.stat_opponent_health);
		opponentArmor = (TextView) findViewById(R.id.stat_opponent_armor);
		opponentResource = (TextView) findViewById(R.id.stat_opponent_resource);
		floatingHealthOpponent = (ViewGroup) findViewById(R.id.floating_health_opponent);
		floatingArmorOpponent = (ViewGroup) findViewById(R.id.floating_armor_opponent);
		floatingResourcehOpponent = (ViewGroup) findViewById(R.id.floating_resource_opponent);

		ActionBar actionBar = getSupportActionBar();
		actionBar.hide();

		// Gets the db that will be reused throughout the game.
		this.db = Db.getDb(getApplicationContext()).getWritableDatabase();

		this.isNetwork = getIntent().getBooleanExtra("isNetwork", false);
		if (this.isNetwork && loadNetworkInfo()) {
			startNetwork();
		} else {
			setupGameMachine();
		}

		if (!hideTutorial()) {
			showTutorialFragment(false);
		}
	}

	/**
	 * If the player has told us we should hide the startup tutorial.
	 * 
	 * @return Should we hide the tutorial?
	 */
	private boolean hideTutorial() {
		SharedPreferences s = getPreferences(Context.MODE_PRIVATE);
		return s.getBoolean("hideTutorial", false);
	}
	
	public void showTutorialButton(View view)
	{
		showTutorialFragment(true);
	}

	/**
	 * Displays the tutorial
	 */
	private void showTutorialFragment(boolean hideNeverButton) {
		final FragmentManager manager = getSupportFragmentManager();

		final TutorialFragment tutorial = new TutorialFragment();
		if(hideNeverButton)
		{
			Bundle b = new Bundle();
			b.putBoolean("hideNeverShow", true);
			tutorial.setArguments(b);
		}
		FragmentTransaction transaction = manager.beginTransaction();
		transaction.add(R.id.game_layout_container, tutorial, "tutorial");
		transaction.commit();
	}

	public void closeAndNeverShowTutorialButton(View view) {
		disableTutorial();
		closeTutorial();
	}

	public void closeTutorialButton(View view) {
		closeTutorial();
	}

	/**
	 * Closes the tutorial fragment.
	 */
	private void closeTutorial() {
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction t = manager.beginTransaction();
		t.remove(manager.findFragmentByTag("tutorial"));
		t.commitAllowingStateLoss();// Since its only a
									// tutorial.
	}

	/**
	 * Disables the tutorial from showing up on startup.
	 */
	public void disableTutorial() {
		SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean("hideTutorial", true);
		editor.commit();
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

	// TODO: Could be better type testing.
	/**
	 * Sets the network connection info fields.
	 * 
	 * @return False if info is missing.
	 */
	private boolean loadNetworkInfo() {
		if (!getIntent().hasExtra("address") || !getIntent().hasExtra("port")
				|| !getIntent().hasExtra("isServer")) {
			return false;

		}
		this.address = getIntent().getStringExtra("address");
		this.port = getIntent().getIntExtra("port", 19999);
		this.isServer = getIntent().getBooleanExtra("isServer", false);
		return true;
	}

	/**
	 * Launches the network connection to opponent.
	 */
	private void startNetwork() {
		openSocketTask = new OpenSocketTask().setup(GameActivity.this.address,
				GameActivity.this.port, GameActivity.this.isServer);
		openSocketTask.execute(GameActivity.this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String connectMsg = String
				.format(getResources()
						.getString(
								(GameActivity.this.isServer ? R.string.network_server_listening_text
										: R.string.network_client_connecting_text)),
						GameActivity.this.address, GameActivity.this.port);
		builder.setTitle(R.string.connect).setMessage(connectMsg)
				.setNegativeButton(R.string.abort, new OnClickListener() {

					// Close the activity.
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						finish();
					}
				});

		waitingForNetwork = builder.create();
		waitingForNetwork.show();
	}

	/**
	 * Loads saved state data into fields and UI when resuming a game.
	 * 
	 * @param savedInstanceState
	 */
	private void loadSavedData(Bundle savedInstanceState) {
		// TODO: Fix saving status objects displayed on screen.
		/*
		 * String savedPlayerText = savedInstanceState
		 * .getString(KEY_TEXT_PLAYER_VALUE);
		 * playerStatus.setText(savedPlayerText); String savedOpponentText =
		 * savedInstanceState .getString(KEY_TEXT_OPPONENT_VALUE);
		 */
		savedPlayer = savedInstanceState.getParcelable("Player");
		savedOpponent = savedInstanceState.getParcelable("Opponent");
		savedState = GameMachine.State.values()[savedInstanceState
				.getInt("State")];
		currentOpponentCard = savedInstanceState.getParcelable("OpponentCard");
		opponentCardIsDiscarded = savedInstanceState.getBoolean(
				"OpponentCardDiscarded", false);
		// Restore the last Card the opponent played.
		if (currentOpponentCard != null) {
			if (opponentCardIsDiscarded) {
				opponentDiscardCard(currentOpponentCard);
			} else {
				opponentPlayCard(currentOpponentCard);
			}
		}
		savedSessionId = savedInstanceState.getInt("Session");
	}

	/**
	 * Sets up the game machine that "drives" the game.
	 */
	@SuppressLint("NewApi")
	// Version is checked in code
	private void setupGameMachine() {
		// Resume gamemachine with previous player states.
		if (savedPlayer != null && savedOpponent != null) {
			gameMachine = new GameMachine(this.db, socket, isServer,
					savedPlayer, savedOpponent, savedState);
		} else { // Start fresh new gamemachine.
			gameMachine = new GameMachine(this.db, socket, isServer);
		}

		// Listen to gamemachine for changes that should be reflect in UI.
		gameMachine.setGameMachineListener(this);
		gameMachine.opponentController.setOpponentListener(this);

		// Listen to player and opponent for changes that should be reflected in
		// UI.
		gameMachine.player.setPlayerUpdateListeners(this);
		gameMachine.opponent.setPlayerUpdateListeners(this);

		// If high enough API level we use drag drop on cards, with lower
		// versions we use a long click context menu.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			handDragListener = new HandDragListener(gameMachine, this);
			layoutContainer.setOnDragListener(handDragListener);
		} else {
			registerForContextMenu(handView);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// We store stuff so that can resume later.
		outState.putParcelable("Player", gameMachine.player);
		outState.putParcelable("Opponent", gameMachine.opponent);
		outState.putInt("State", gameMachine.state.ordinal());
		outState.putParcelable("OpponentCard", currentOpponentCard);
		outState.putBoolean("OpponentCardDiscarded", opponentCardIsDiscarded);
		outState.putInt("Session", savedSessionId);
		// TODO: Implementing storing state to database so can resume even if
		// app is destroyed.
		// save sessionid?
	}

	/**
	 * Updates GUI with the card the opponent has played.
	 * 
	 * @param card
	 */
	private void opponentPlayCard(Card card) {
		currentOpponentCard = card;
		opponentCardIsDiscarded = false;
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
		currentOpponentCard = card;
		opponentCardIsDiscarded = true;
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
		if (gameMachine.player.getCard(position).getName().equals("Minigame!")
				&& getPackageManager().hasSystemFeature(
						PackageManager.FEATURE_SENSOR_ACCELEROMETER)
				&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			startMinigame(position);
		} else {
			gameMachine.playerPlayCard(position);
		}
	}

	// Should only be called on lower API versions, since we use drag and drop
	// otherwise.
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.click_card_on_hand, menu);
	}

	// When a player selects a card to use.
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.card_context_use:
			playCard(info.position);
			return true;
		case R.id.card_context_discard:
			gameMachine.playerDiscardCard(info.position);
			return true;
		default:
			return super.onContextItemSelected(item);
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

		Effect e = gameMachine.player.getCard(cardPosition).getEffects().get(0);
		MinigameFragment minigame = new MinigameFragment();
		Bundle b = new Bundle();
		b.putInt("min", e.getMinValue());
		b.putInt("max", e.getMaxValue());
		b.putInt("pos", cardPosition);
		minigame.setArguments(b);

		transaction.add(R.id.game_layout_container, minigame, "minigame");
		transaction.commit();
	}

	// If try to use a card we dont have enough resources for etc.
	@Override
	public void onCouldNotPlayCardListener(int position) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
				&& handDragListener != null) {
			handDragListener.stopDrag(position);
		}
	}

	// Background behind player's name turns red on their turn.
	// Version is checked in code.
	@SuppressLint("NewApi")
	@Override
	public void onPlayerTurnListener() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
				&& handDragListener != null) {
			layoutContainer.setOnDragListener(handDragListener);
		} else {
			registerForContextMenu(handView);
		}
		playerName.setBackgroundColor(Color.RED);
		opponentName.setBackgroundColor(Color.TRANSPARENT);
	}

	@SuppressLint("NewApi")
	// We check in code
	@Override
	public void onPlayerDoneListener() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			layoutContainer.setOnDragListener(null);
		}
		playerName.setBackgroundColor(Color.TRANSPARENT);
		opponentName.setBackgroundColor(Color.RED);
	}

	@Override
	public void onOpponentTurnListener() {
	}

	@Override
	public void onPlayerDeadListener(Player player) {
		playerStatus.setText("Player dead");
	}

	@Override
	public void onOpponentDeadListener(Player opponent) {
		playerStatus.setText("Opponent dead");
	}

	// We check in code
	@SuppressLint("NewApi")
	@Override
	public void onPlayerPlayedCard(Card card) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
				&& handDragListener != null) {
			handDragListener.removeDrag();
		} else {
			unregisterForContextMenu(handView);
		}
	}

	@Override
	public void onPlayerDiscardCard(Card card) {
	}

	@Override
	public void onOpponentPlayCard(Card card, boolean generateDamage) {
		opponentPlayCard(card);
	}

	@Override
	public void onOpponentDiscardCard(Card card) {
		opponentDiscardCard(card);
	}

	@Override
	public void onCardsUpdateListener(Player player, Card[] cards) {
		// Update the cards on the screen if the player's cards change.
		if (player == gameMachine.player) {
			GameDeckAdapter adapter = new GameDeckAdapter(cards);
			handView.setAdapter(adapter);
		}
	}

	@Override
	public void onStatsUpdateListener(Player player) {
		if (player == gameMachine.player) {
			// playerName.setText(player.getName());
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(this);
			playerName
					.setText(sp.getString(SettingsActivity.KEY_PREF_USER, ""));

			playerHealth.setText(String.valueOf(player.getHealth()));
			playerArmor.setText(String.valueOf(player.getArmor()));
			playerResource.setText(String.valueOf(player.getResources()));

		} else if (player == gameMachine.opponent) {
			opponentName.setText(player.getName());
			opponentHealth.setText(String.valueOf(player.getHealth()));
			opponentArmor.setText(String.valueOf(player.getArmor()));
			opponentResource.setText(String.valueOf(player.getResources()));
		}
	}

	@Override
	public void onStatChange(Player player, int amount, EffectType type) {

		final boolean isPlayer = player == gameMachine.player;

		final ViewGroup viewGroup;
		final TextView floatingText;

		// Find the correct viewgroup for the floating text and set the correct
		// color on the text
		int color;
		switch (type) {
		case ARMOR:
			viewGroup = (isPlayer ? floatingArmorPlayer : floatingArmorOpponent);
			color = Color.BLUE;
			break;
		case DAMAGE:
			viewGroup = (isPlayer ? floatingHealthPlayer
					: floatingHealthOpponent);
			color = Color.RED;
			break;
		case HEALTH:
			viewGroup = (isPlayer ? floatingHealthPlayer
					: floatingHealthOpponent);
			color = Color.rgb(45, 190, 50);
			break;
		case RESOURCE:
			viewGroup = (isPlayer ? floatingResourcehPlayer
					: floatingResourcehOpponent);
			color = Color.rgb(180, 180, 50);
			break;
		default:
			viewGroup = null;
			color = Color.WHITE;
			break;
		}

		int index = -1;

		// Find the first empty textview so we can put the new stat text there.
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			if (((TextView) viewGroup.getChildAt(i)).getText().length() == 0) {
				index = i;
				break;
			}
		}

		// We have to set this outside the loop because its final, is a better
		// way to do this?
		floatingText = (index >= 0 ? (TextView) viewGroup.getChildAt(index)
				: null);

		// TODO: Improve this?
		// If there is no empty text fields the stat wont be shown. Could
		// improve this.
		if (floatingText == null || viewGroup == null) {
			return;
		}

		// Adds the + sign if positive damage, negative shows automatically.
		floatingText.setText((amount >= 0 ? "+" : "")
				+ Integer.toString(amount));
		floatingText.setTextColor(color);

		// So text is removed after a delay
		Runnable r = new Runnable() {
			@Override
			public void run() {
				floatingText.setText("");
			}
		};
		new Handler().postDelayed(r, 2000);
	}

	@Override
	public void onOpponentUsedEffect(EffectType type, Player target, int amount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOpponentTurnDone() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerUsedeffect(EffectType type, Player target, int amount) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOpenSocketListener(Socket socket, ServerSocket server,
			Exception exception) {
		if (server != null) {
			this.server = server;
		}
		if (socket != null) {
			// Hide the waiting for network dialog and show message that we're
			// connected.
			waitingForNetwork.cancel();
			Toast t = Toast.makeText(this, R.string.connected,
					Toast.LENGTH_LONG);
			t.show();
			// When start as a network game, the game will wait for this until
			// it start.
			this.socket = socket;
			setupGameMachine();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (gameMachine == null
				|| (gameMachine.player.isAlive() && gameMachine.opponent
						.isAlive())) {
			sendAnnoyingNotification();
		}

		// Move sessionDataSource out to be a global field
		SessionDataSource sds = new SessionDataSource(db);
		if (savedSessionId != -1) {
			sds.setSessionId(savedSessionId);
		}
		if (gameMachine != null) {
			sds.saveSession(gameMachine.state.ordinal(), gameMachine.player,
					gameMachine.opponent,
					(currentOpponentCard != null ? currentOpponentCard.getId()
							: -1), opponentCardIsDiscarded);
		}

		if (isNetwork) {
			closeSockets();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO: Network reconnect?
		cancelAnnoyingNotification();
	}

	/**
	 * Close the network sockets.
	 */
	private void closeSockets() {
		if (openSocketTask != null) {
			openSocketTask.cancel(true);
		}

		if (socket != null) {
			new CloseSocketTask().execute(this.socket);
		}
		if (server != null) {
			new CloseServerSocketTask().execute(this.server);
		}
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

	@SuppressLint("NewApi")
	/**
	 * If the player closes the game during a match we notify our AlarmReceiver so that we can notify the player that he has a game running.
	 * This is mostly implemented to show-case use of AlarmManager and Notification.
	 */
	private void sendAnnoyingNotification() {
		AlarmManager a = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarm = PendingIntent.getBroadcast(this, 0, new Intent(
				this, AlarmReceiver.class), 0);

		a.set(AlarmManager.RTC,
				Calendar.getInstance().getTimeInMillis() + 600000, alarm);
	}

	/**
	 * Cancels a previously set alarm
	 */
	private void cancelAnnoyingNotification() {
		AlarmManager a = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarm = PendingIntent.getBroadcast(this, 0, new Intent(
				this, AlarmReceiver.class), 0);
		a.cancel(alarm);
	}

	// Version checked in code.
	@SuppressLint("NewApi")
	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			layoutContainer.setOnDragListener(null);
		} else {
			unregisterForContextMenu(handView);
		}
	}

}
