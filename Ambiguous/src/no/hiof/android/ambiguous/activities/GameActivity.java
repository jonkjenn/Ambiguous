package no.hiof.android.ambiguous.activities;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.OpponentController.OpponentListener;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.adapter.GameDeckAdapter;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Player;
import no.hiof.android.ambiguous.model.Player.PlayerUpdateListener;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameActivity extends Activity implements OnDragListener,
		GameMachine.GameMachineListener, OpponentListener, PlayerUpdateListener {
	private SQLiteDatabase db;
	private View layoutView;
	private GridView deckView;
	private GameMachine gameMachine;

	TextView playerstatus;
	TextView opponentstatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		layoutView = findViewById(R.id.game_layout);
		
		
		ActionBar actionBar = getActionBar();
		actionBar.hide();

		this.db = Db.getDb(getApplicationContext()).getWritableDatabase();

		gameMachine = new GameMachine(this.db);
		gameMachine.setGameMachineListener(this);
		gameMachine.opponentController.setOpponentListener(this);

		deckView = (GridView) findViewById(R.id.game_grid);

		// Listen to the player class for updates on the players current cards
		// "on the table", NOT the players deck we pull cards from.

		setupDragDrop(layoutView);

		playerstatus = (TextView) findViewById(R.id.stats_player);
		opponentstatus = (TextView) findViewById(R.id.stats_computer);

		playerstatus.setText(" ");
		gameMachine.player.setPlayerUpdateListeners(this);
		gameMachine.opponent.setPlayerUpdateListeners(this);
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
	public void onOpponentPlayCard(Card card) {
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
	public void onFloatingText(Player player, int amount, int color) {
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
				viewGroup.removeView(floatingText);
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
}
