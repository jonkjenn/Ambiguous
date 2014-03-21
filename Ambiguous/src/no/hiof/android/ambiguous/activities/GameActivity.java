package no.hiof.android.ambiguous.activities;

import java.net.ServerSocket;
import java.net.Socket;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.GameMachine.State;
import no.hiof.android.ambiguous.MinigameFragment;
import no.hiof.android.ambiguous.MinigameFragment.MinigameListener;
import no.hiof.android.ambiguous.OpponentController.OpponentListener;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.adapter.GameDeckAdapter;
import no.hiof.android.ambiguous.cardlistener.CardOnTouchListener;
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
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.DragEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Initializes games and shows/handles the game UI.
 */
public class GameActivity extends Activity implements OnDragListener,
		GameMachine.GameMachineListener, OpponentListener,
		PlayerUpdateListener, OpenSocketListener, MinigameListener {
	private SQLiteDatabase db;

	// Shows the cards on players "hand"
	private GridView deckView;

	// The outer layout around the whole game view.
	private RelativeLayout layoutContainer;
	private GameMachine gameMachine;

	private View useCardNotificationView;
	private View discardCardNotificationView;

	private TextView playerName;
	private TextView playerHealth;
	private TextView playerArmor;
	private TextView playerResource;
	private ViewGroup floatingHealthPlayer;
	private ViewGroup floatingArmorPlayer;
	private ViewGroup floatingResourcehPlayer;

	private TextView opponentName;
	private TextView opponentHealth;
	private TextView opponentArmor;
	private TextView opponentResource;
	private ViewGroup floatingHealthOpponent;
	private ViewGroup floatingArmorOpponent;
	private ViewGroup floatingResourcehOpponent;

	// TODO: Possibly recode so don't need these as fields.
	private Player savedPlayer;
	private Player savedOpponent;
	private State savedState;

	// TODO: playerStatus currently used for status messages, opponentStatus
	// indicates turns. Should rename!
	TextView playerStatus;

	// Network settings
	private boolean isNetwork;
	private boolean stopNetwork;
	private String address;
	private int port;
	private boolean isServer;
	private Socket socket;
	private ServerSocket server;

	/*
	 * private static final String KEY_TEXT_PLAYER_VALUE = "playerTextValue";
	 * private static final String KEY_TEXT_OPPONENT_VALUE =
	 * "opponentTextValue";
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		layoutContainer = (RelativeLayout) findViewById(R.id.game_layout_container);
		playerStatus = (TextView) findViewById(R.id.stats_player);
		deckView = (GridView) findViewById(R.id.game_grid);
		layoutContainer.setOnDragListener(this);

		// TODO: What?
		playerStatus.setText(" ");

		if (savedInstanceState != null) {
			loadSavedData(savedInstanceState);
		}
		useCardNotificationView = findViewById(R.id.gameview_use);
		discardCardNotificationView = findViewById(R.id.gameview_discard);

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

		ActionBar actionBar = getActionBar();
		actionBar.hide();

		// Gets the db that will be reused throughout the game.
		this.db = Db.getDb(getApplicationContext()).getWritableDatabase();

		this.isNetwork = getIntent().getBooleanExtra("isNetwork", false);
		if (this.isNetwork && loadNetworkInfo()) {
			startNetwork();
		} else {
			setupGameMachine();
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
		this.isServer = getIntent().getBooleanExtra("isServer{", false);
		return true;
	}

	/**
	 * Launches the network connection to opponent.
	 */
	private void startNetwork() {
		new OpenSocketTask().setup(GameActivity.this.address,
				GameActivity.this.port, GameActivity.this.isServer).execute(
				GameActivity.this);
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
	}

	/**
	 * Sets up the game machine that "drives" the game.
	 */
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
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// We store stuff so that can resume later.
		outState.putParcelable("Player", gameMachine.player);
		outState.putParcelable("Opponent", gameMachine.opponent);
		outState.putInt("State", gameMachine.state.ordinal());
		// TODO: Implementing storing state to database so can resume even if
		// app is destroyed.
	}

	/**
	 * Updates GUI with the card the opponent has played.
	 * 
	 * @param card
	 */
	private void opponentPlayCard(Card card) {
		ImageView parent = (ImageView) findViewById(R.id.opponent_card);
		parent.setImageBitmap(CardLayout.getCardBitmap(card,
				(ViewGroup) findViewById(R.id.game_layout_container)));
		// Hide the discard graphic since we're not discarding.
		findViewById(R.id.discard).setVisibility(View.INVISIBLE);
	}

	/**
	 * Updates GUI with the card the opponent has discarded.
	 * 
	 * @param card
	 */
	private void opponentDiscardCard(Card card) {
		ImageView parent = (ImageView) findViewById(R.id.opponent_card);
		parent.setImageBitmap(CardLayout.getCardBitmap(card,
				(ViewGroup) findViewById(R.id.game_layout_container)));
		// Show the discard graphic since we're discarding.
		findViewById(R.id.discard).setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	/**
	 * Moves the card we're dragging around the screen to a new position.
	 * 
	 * @param card
	 *            The position of the card in the players hand.
	 * @param x
	 *            The X coordinate of where we are touching/clicking the screen.
	 * @param y
	 *            The Y coordinate of where we are touching/clicking the screen.
	 * @param insideX
	 *            The X coordinate of where we're pushing on the card to drag
	 *            it.
	 * @param insideY
	 *            The Y coordinate of where we're pushing on the card to drag
	 *            it.
	 */
	private void drag(int card, int x, int y, int insideX, int insideY) {
		// Hide the original card on players deck.
		deckView.getChildAt(card).setVisibility(View.INVISIBLE);
		// Set the view we actually drag around the screen visible.
		ImageView parent = (ImageView) findViewById(R.id.drag_card);
		parent.setVisibility(ImageView.VISIBLE);

		RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(
				parent.getLayoutParams());
		// So we dont move the card outside the game view.
		if ((par.leftMargin + par.width) > deckView.getWidth()) {
			return;
		}

		// Move the card by changing the left and top margins
		par.setMargins(x - insideX, y - insideY, 0, 0);
		parent.setLayoutParams(par);
	}

	/**
	 * We drag cards by creating a copy of the image used on the card and move
	 * this around while the stationary actual card is hidden.
	 * 
	 * @param card
	 *            The position of the card in the players hand.
	 */
	private void startDrag(int card) {
		ImageView layout = (ImageView) findViewById(R.id.drag_card);
		ImageView i = (ImageView) deckView.getChildAt(card);
		// Get the bitmap used on the stationary card and use this as bitmap on
		// our "drag card".
		layout.setImageBitmap(((BitmapDrawable) i.getDrawable()).getBitmap());

		layout.setVisibility(ImageView.GONE);
	}

	/**
	 * Reshow the stationary card we have previously dragged, typically used
	 * when change our mind on wich card to use.
	 * 
	 * @param card
	 */
	private void stopDrag(int card) {
		deckView.getChildAt(card).setVisibility(View.VISIBLE);
		removeDrag();
	}

	/**
	 * Hide only the card we drag around, typically called when we have used or
	 * discarded a card.
	 */
	private void removeDrag() {
		findViewById(R.id.drag_card).setVisibility(View.INVISIBLE);
	}

	/**
	 * Handles when a drag action moves and generate new coordinates.
	 * 
	 * @param touchData
	 *            The state we pass with the event.
	 * @param eventX
	 * @param eventY
	 */
	private void handleDragToLocation(
			CardOnTouchListener.CardTouchData touchData, int eventX, int eventY) {
		// For updating the card we drag around.
		drag(touchData.position, eventX, eventY, touchData.localX,
				touchData.localY);

		// If have moved the card upwards enough we display the top bar that
		// tells the user he can now drop the card to use it.
		if (touchData.viewHeight / 2 + eventY < touchData.screenY - 100) {
			useCardNotificationView.setVisibility(View.VISIBLE);
		} else {
			useCardNotificationView.setVisibility(View.INVISIBLE);
		}

		// If have moved the card downwards enough we display the bottom bar
		// that tells the user he can now drop the card to discard it.
		if (touchData.viewHeight / 2 + eventY > this.layoutContainer
				.getHeight()) {
			discardCardNotificationView.setVisibility(View.VISIBLE);
		} else {
			discardCardNotificationView.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Handles the drop of a card dragdrop.
	 * 
	 * @param touchData
	 * @param eventX
	 * @param eventY
	 */
	private void handleDropEvent(CardOnTouchListener.CardTouchData touchData,
			int eventX, int eventY) {

		// If moved the card "enough" upwards use the card.
		if (touchData.viewHeight / 2 + eventY < touchData.screenY - 100) {
			useCardNotificationView.setVisibility(View.INVISIBLE);
			removeDrag();

			// Checks for the minigame card. Check if the phone access to the
			// gravity sensor which is needed for the minigame. If not the
			// minigame card is played as a regular card with random damage in
			// its defined range.
			if (gameMachine.player.GetCard(touchData.position).getName()
					.equals("Minigame!")
					&& this.getPackageManager().hasSystemFeature(
							PackageManager.FEATURE_SENSOR_ACCELEROMETER)
					&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				startMinigame(touchData.position);
			} else {
				gameMachine.playerPlayCard(touchData.position);
			}
		}// If moved the card enough downwards discard the card.
		else if (touchData.viewHeight / 2 + eventY > this.layoutContainer
				.getHeight()) {
			findViewById(R.id.gameview_discard).setVisibility(
					TextView.INVISIBLE);
			removeDrag();
			gameMachine.playerDiscardCard(touchData.position);
		} else // Cancel the dragdrop so that user can pick a different card.
		{
			stopDrag(touchData.position);
		}
	}

	/**
	 * Start the minigame fragment.
	 * @param cardPosition The position of the card in the players hand.
	 */
	private void startMinigame(int cardPosition) {
		FragmentManager manager = getFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		Effect e = gameMachine.player.GetCard(cardPosition).getEffects().get(0);
		MinigameFragment minigame = new MinigameFragment();
		minigame.setMinigameListener(this);
		Bundle b = new Bundle();
		b.putInt("min", e.getMinValue());
		b.putInt("max", e.getMaxValue());
		b.putInt("pos", cardPosition);
		minigame.setArguments(b);

		transaction.add(R.id.game_layout_container, minigame, "minigame");
		transaction.commit();
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		// Can only drag on players turn.
		if (!gameMachine.isPlayersTurn()) {
			return false;
		}

		// Data passed from the touch even that starts the drag.
		CardOnTouchListener.CardTouchData cardTouchData;

		// Convert the object passed from card touch
		if (event.getLocalState() != null
				&& event.getLocalState() instanceof CardOnTouchListener.CardTouchData) {
			cardTouchData = (CardOnTouchListener.CardTouchData) event
					.getLocalState();

			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				if (event.getLocalState() != null) {
					startDrag(cardTouchData.position);
				}
				return true;

			case DragEvent.ACTION_DRAG_LOCATION:
				if (event.getLocalState() != null) {
					handleDragToLocation(cardTouchData, (int) event.getX(),
							(int) event.getY());
				}
				break;

			case DragEvent.ACTION_DROP:
				if (event.getLocalState() != null) {
					handleDropEvent(cardTouchData, (int) event.getX(),
							(int) event.getY());
				}
				return true;
			}
		}
		return false;
	}

	// If try to use a card we dont have enough resources for etc.
	@Override
	public void onCouldNotPlayCardListener(int position) {
		stopDrag(position);
	}

	// Background behind player's name turns red on their turn.
	@Override
	public void onPlayerTurnListener() {
		layoutContainer.setOnDragListener(this);
		playerName.setBackgroundColor(Color.RED);
		opponentName.setBackgroundColor(Color.TRANSPARENT);
	}

	@Override
	public void onPlayerDoneListener() {
		this.layoutContainer.setOnDragListener(null);
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

	@Override
	public void onPlayerPlayedCard(Card card) {
		removeDrag();
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
			deckView.setAdapter(adapter);
		}
	}

	@Override
	public void onStatsUpdateListener(Player player) {
		if (player == gameMachine.player) {
			playerName.setText(player.getName());
			playerHealth.setText(String.valueOf(player.getHealth()));
			playerArmor.setText(String.valueOf(player.getArmor()));
			playerResource.setText(String.valueOf(player.getResources()));

		} else if (player == gameMachine.opponent) {
			// opponentstats.setText(str);
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
	public void onArmorUpdateListener(Player player, int armor) {
		// TODO Auto-generated method stub

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
			// When start as a network game, the game will wait for this until
			// it start.
			this.socket = socket;
			setupGameMachine();
		}

		// Network client will try to reconnect after timeouts
		if (exception != null && !isServer && !stopNetwork) {
			final OpenSocketTask task = new OpenSocketTask().setup(
					this.address, this.port, this.isServer);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					if (!GameActivity.this.stopNetwork) {
						task.execute(GameActivity.this);
					}
				}
			}, 1000);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isNetwork) {
			stopNetwork = true;
			closeSockets();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO: Network reconnect?
	}

	private void closeSockets() {
		if (socket != null) {
			new CloseSocketTask().execute(this.socket);
		}
		if (server != null) {
			new CloseServerSocketTask().execute(this.server);
		}
	}

	@Override
	public void onGameEnd(int amount, int position) {
		gameMachine.playerPlayCard(position, amount);
		FragmentTransaction tr = getFragmentManager().beginTransaction();
		tr.remove(getFragmentManager().findFragmentByTag("minigame"));
		tr.commit();
	}

}
