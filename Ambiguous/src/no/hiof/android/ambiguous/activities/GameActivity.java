package no.hiof.android.ambiguous.activities;

import java.net.ServerSocket;
import java.net.Socket;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.OpponentController.OpponentListener;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.adapter.GameDeckAdapter;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;
import no.hiof.android.ambiguous.model.Player.PlayerUpdateListener;
import no.hiof.android.ambiguous.network.CloseServerSocketTask;
import no.hiof.android.ambiguous.network.CloseSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask;
import no.hiof.android.ambiguous.network.OpenSocketTask.OpenSocketListener;
import android.app.ActionBar;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameActivity extends Activity implements OnDragListener,
		GameMachine.GameMachineListener, OpponentListener, PlayerUpdateListener, OpenSocketListener {
	private SQLiteDatabase db;
	private View layoutView;
	private GridView deckView;
	private GameMachine gameMachine;

	TextView playerstatus;
	TextView opponentstatus;
	private boolean isNetwork;
	private String address;
	private int port;
	private boolean isServer;
	private Socket socket;
	private ServerSocket server;

	private static final String KEY_TEXT_PLAYER_VALUE = "playerTextValue";
	private static final String KEY_TEXT_OPPONENT_VALUE = "opponentTextValue";
	/*private static final String KEY_TEXT_PLAYER_NAME = "playerNameValue";
	private static final String KEY_TEXT_PLAYER_HEALTH = "playerHealthValue";
	private static final String KEY_TEXT_PLAYER_ARMOR = "playerArmorValue";
	private static final String KEY_TEXT_PLAYER_RESOURCE = "playerResourceValue";
	private static final String KEY_TEXT_OPPONENT_NAME = "opponentNameValue";
	private static final String KEY_TEXT_OPPONENT_HEALTH = "opponentHealthValue";
	private static final String KEY_TEXT_OPPONENT_ARMOR = "opponentArmorValue";
	private static final String KEY_TEXT_OPPONENT_RESOURCE = "opponentResourceValue";*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		layoutView = findViewById(R.id.game_layout);
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();

		this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
		
		if(isNetwork = getIntent().getBooleanExtra("isNetwork",false))
		{
            this.address = getIntent().getStringExtra("address");
            this.port = getIntent().getIntExtra("port",19999);
            this.isServer = getIntent().getBooleanExtra("isServer",false);
            new OpenSocketTask().setup(this.address,this.port,this.isServer).execute(this);
		}
		else{
            setupGameMachine();
		}

	}
	
	private void setupGameMachine()
	{
		gameMachine = new GameMachine(this.db, socket,isServer);
		gameMachine.setGameMachineListener(this);
		gameMachine.opponentController.setOpponentListener(this);

		deckView = (GridView) findViewById(R.id.game_grid);

		// Listen to the player class for updates on the players current cards
		// "on the table", NOT the players deck we pull cards from.

		setupDragDrop(layoutView);

		playerstatus = (TextView) findViewById(R.id.stats_player);
		opponentstatus = (TextView) findViewById(R.id.stats_computer);

		playerstatus.setText(" ");
		if(savedInstanceState != null){
			String savedText = savedInstanceState.getString(KEY_TEXT_PLAYER_VALUE);
			playerstatus.setText(savedText);
			/*((TextView)findViewById(R.id.stat_player_name)).setText(savedInstanceState.getString(KEY_TEXT_PLAYER_NAME));
			((TextView)findViewById(R.id.stat_player_health)).setText(savedInstanceState.getString(KEY_TEXT_PLAYER_HEALTH));
			((TextView)findViewById(R.id.stat_player_armor)).setText(savedInstanceState.getString(KEY_TEXT_PLAYER_ARMOR));
			((TextView)findViewById(R.id.stat_player_resource)).setText(savedInstanceState.getString(KEY_TEXT_PLAYER_RESOURCE));
			((TextView)findViewById(R.id.stat_opponent_name)).setText(savedInstanceState.getString(KEY_TEXT_OPPONENT_NAME));
			((TextView)findViewById(R.id.stat_opponent_health)).setText(savedInstanceState.getString(KEY_TEXT_OPPONENT_HEALTH));
			((TextView)findViewById(R.id.stat_opponent_armor)).setText(savedInstanceState.getString(KEY_TEXT_OPPONENT_ARMOR));
			((TextView)findViewById(R.id.stat_opponent_resource)).setText(savedInstanceState.getString(KEY_TEXT_OPPONENT_RESOURCE));
			*/
			gameMachine.player = savedInstanceState.getParcelable("Player");
			gameMachine.opponent = savedInstanceState.getParcelable("Opponent");
		}
		
		gameMachine.player.setPlayerUpdateListeners(this);
		gameMachine.opponent.setPlayerUpdateListeners(this);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		/*outState.putString(KEY_TEXT_PLAYER_VALUE, (String)playerstatus.getText());
		outState.putString(KEY_TEXT_PLAYER_NAME, (String)((TextView)findViewById(R.id.stat_player_name)).getText());
		outState.putString(KEY_TEXT_PLAYER_HEALTH, (String)((TextView)findViewById(R.id.stat_player_health)).getText());
		outState.putString(KEY_TEXT_PLAYER_ARMOR, (String)((TextView)findViewById(R.id.stat_player_armor)).getText());
		outState.putString(KEY_TEXT_PLAYER_RESOURCE, (String)((TextView)findViewById(R.id.stat_player_resource)).getText());
		outState.putString(KEY_TEXT_OPPONENT_NAME, (String)((TextView)findViewById(R.id.stat_opponent_name)).getText());
		outState.putString(KEY_TEXT_OPPONENT_ARMOR, (String)((TextView)findViewById(R.id.stat_opponent_armor)).getText());
		outState.putString(KEY_TEXT_OPPONENT_RESOURCE, (String)((TextView)findViewById(R.id.stat_opponent_resource)).getText());
		*/
		outState.putParcelable("Player", gameMachine.player);
		outState.putParcelable("Opponent", gameMachine.opponent);
		
	}

	private void opponentPlayCard(Card card) {
		ViewGroup parent = (ViewGroup) findViewById(R.id.opponent_card);
		parent.removeAllViews();
		parent.addView(CardLayout.getCardLayout(card, parent));
	}

	private void opponentDiscardCard(Card card) {
		ViewGroup parent = (ViewGroup) findViewById(R.id.opponent_card);
		parent.removeAllViews();
		parent.addView(CardLayout.getCardLayout(card, parent));
		TextView v = new TextView(this);
		RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 30);
		par.addRule(RelativeLayout.CENTER_HORIZONTAL);
		par.addRule(RelativeLayout.CENTER_VERTICAL);
		v.setBackgroundColor(Color.RED);
		v.setLayoutParams(par);
		v.setGravity(Gravity.CENTER);
		v.setTextColor(Color.BLACK);
		v.setText("DISCARD");
		parent.addView(v);
	}

	private void setupDragDrop(View view) {
		view.setOnDragListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	private void drag(int card, int x, int y, int insideX, int insideY) {
		deckView.getChildAt(card).setVisibility(View.GONE);
		RelativeLayout parent = (RelativeLayout) findViewById(R.id.drag_card);
		parent.setVisibility(RelativeLayout.VISIBLE);
		if (parent.getChildCount() > 0) {
			android.widget.RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(
					parent.getLayoutParams());
			//par.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			// par.setMargins(x-parent.getWidth()/2, y-parent.getHeight()/2, 0,
			// 0);
			par.setMargins(x - insideX, y - insideY, 0, 0);
			parent.setLayoutParams(par);
		}
	}

	private void startDrag(int card, int x, int y) {
		//deckView.getChildAt(card).setVisibility(View.GONE);
		ViewGroup parent = (ViewGroup) findViewById(R.id.drag_card);
		Card c = gameMachine.player.GetCard(card);
		View layout = CardLayout.getCardLayout(c, parent);

		parent.setVisibility(ViewGroup.GONE);
		parent.removeAllViews();
		parent.addView(layout);
	}

	private void stopDrag(int card) {
		deckView.getChildAt(card).setVisibility(View.VISIBLE);
		removeDrag();
	}

	private void removeDrag() {
		ViewGroup parent = (ViewGroup) findViewById(R.id.drag_card);
		parent.removeAllViews();
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		if (!gameMachine.playersTurn()) {
			return false;
		}

		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			if (event.getLocalState() != null) {
				int[] dState = (int[]) event.getLocalState();
				startDrag(dState[1], (int) event.getX(), (int) event.getY());
			}

			return true;

		case DragEvent.ACTION_DRAG_LOCATION:
			if (event.getLocalState() != null) {
				int[] dState = (int[]) event.getLocalState();
				drag(dState[1], (int) event.getX(), (int) event.getY(),
						dState[3], dState[4]);

				if (dState.length > 2
						&& dState[2] / 2 + event.getY() < dState[0] - 100) {
					Log.d("test", "Oppover");
					findViewById(R.id.gameview_use).setVisibility(
							TextView.VISIBLE);
				} else {
					findViewById(R.id.gameview_use)
							.setVisibility(TextView.GONE);
				}
				if (dState.length > 2
						&& dState[2] / 2 + event.getY() > this.layoutView
								.getHeight()) {
					Log.d("test", "funker?");
					findViewById(R.id.gameview_discard).setVisibility(
							TextView.VISIBLE);
				} else {
					findViewById(R.id.gameview_discard).setVisibility(
							TextView.GONE);
				}
			}
			break;

		case DragEvent.ACTION_DROP:
			float y = event.getY();
			if (event.getLocalState() != null) {
				int[] dragState = (int[]) event.getLocalState();
				float starty = (float) dragState[0];
				Log.d("test", y + " " + starty);

				if (dragState.length > 2
						&& dragState[2] / 2 + event.getY() < dragState[0] - 100) {
					Log.d("test", "Oppover");
					findViewById(R.id.gameview_use)
							.setVisibility(TextView.GONE);
					gameMachine.PlayerPlayCard(dragState[1]);
				} else if (dragState.length > 2
						&& dragState[2] / 2 + event.getY() > this.layoutView
								.getHeight()) {
					Log.d("test", "funker?");
					findViewById(R.id.gameview_discard).setVisibility(
							TextView.GONE);
					gameMachine.PlayerDiscardCard(dragState[1]);
					removeDrag();
				} else {
					stopDrag(dragState[1]);
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void onCouldNotPlayCardListener(int position) {
		stopDrag(position);
	}

	@Override
	public void onPlayerTurnListener() {
		setupDragDrop(layoutView);
	}

	@Override
	public void onPlayerDoneListener() {
		this.layoutView.setOnDragListener(null);
	}

	@Override
	public void onOpponentTurnListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerDeadListener(Player player) {
		playerstatus.setText("Player dead");
	}

	@Override
	public void onOpponentDeadListener(Player opponent) {
		opponentstatus.setText("Opponent dead");
	}

	@Override
	public void onPlayerPlayedCard(Card card) {
		removeDrag();
	}

	@Override
	public void onPlayerDiscardCard(Card card) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOpponentPlayCard(Card card,boolean generateDamage) {
		opponentPlayCard(card);
	}

	@Override
	public void onOpponentDiscardCard(Card card) {
		opponentDiscardCard(card);
	}

	@Override
	public void onCardsUpdateListener(Player player, Card[] cards) {
		if (player == gameMachine.player) {
			GameDeckAdapter adapter = new GameDeckAdapter(cards);
			deckView.setAdapter(adapter);
		}
	}

	@Override
	public void onStatsUpdateListener(Player player, String str) {
		if (player == gameMachine.player) {
			//playerstats.setText(str);
			TextView playerName = ((TextView)findViewById(R.id.stat_player_name));
					playerName.setText(player.getName());
			TextView playerHealth = ((TextView)findViewById(R.id.stat_player_health));
					playerHealth.setText(String.valueOf(player.getHealth()));
			TextView playerArmor = ((TextView)findViewById(R.id.stat_player_armor));
					playerArmor.setText(String.valueOf(player.getArmor()));
			TextView playerResources = ((TextView)findViewById(R.id.stat_player_resource));
					playerResources.setText(String.valueOf(player.getResources()));
			
		} else if (player == gameMachine.opponent) {
			//opponentstats.setText(str);
			TextView opponentName = ((TextView)findViewById(R.id.stat_opponent_name));
					opponentName.setText(player.getName());
			TextView opponentHealth = ((TextView)findViewById(R.id.stat_opponent_health));
					opponentHealth.setText(String.valueOf(player.getHealth()));
			TextView opponentArmor = ((TextView)findViewById(R.id.stat_opponent_armor));
					opponentArmor.setText(String.valueOf(player.getArmor()));
			TextView opponentResources = ((TextView)findViewById(R.id.stat_opponent_resource));
					opponentResources.setText(String.valueOf(player.getResources()));
		}
	}

	@Override
	public void onStatChange(Player player, int amount, EffectType type) {
		int color;
		switch(type)
		{
		case ARMOR:
			color = Color.BLUE;
			break;
		case DAMAGE:
			color = Color.RED;
			break;
		case HEALTH:
			color = Color.rgb(45, 190, 50);
			break;
		case RESOURCE:
			color = Color.rgb(180,180,50);
			break;
		default:
			color = Color.WHITE;
			break;
		}
		final boolean isPlayer = player==gameMachine.player;
		final ViewGroup viewGroup = (ViewGroup) findViewById((isPlayer ? R.id.floating_container
				: R.id.floating_container2));

		final TextView floatingText = (TextView) LayoutInflater.from(
				viewGroup.getContext())
				.inflate(R.layout.floatingtextview, null);

		viewGroup.addView(floatingText);

		floatingText.setText(Integer.toString(amount));
		floatingText.setTextColor(color);

		final AlphaAnimation fadeIn = new AlphaAnimation(0.0f,1.0f);
		fadeIn.setDuration(500);
		final AlphaAnimation fadeOut = new AlphaAnimation(1.0f,0.0f);
		fadeOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				new Handler().post(new Runnable() {
			        public void run() {
			        	viewGroup.removeView(floatingText);
			        }
			    });
				
			}
		});
		fadeOut.setDuration(500);
		fadeIn.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				Runnable r = new Runnable() {
					
					@Override
					public void run() {
						floatingText.startAnimation(fadeOut);
					}
				};
				new Handler().postDelayed(r,1000);
			}
		});
		
		floatingText.startAnimation(fadeIn);
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
		if(server != null){this.server = server;}
		if(socket != null){
			this.socket = socket;
			setupGameMachine();
		}
		if(exception != null && !isServer)
		{
            final OpenSocketTask task = new OpenSocketTask().setup(this.address,this.port,this.isServer);
            new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					task.execute(GameActivity.this);										
				}
			},1000);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		closeSockets();
	}
	
	private void closeSockets()
	{
		if(socket != null){new CloseSocketTask().execute(this.socket);}
		if(server != null){new CloseServerSocketTask().execute(this.server);}
	}
}
