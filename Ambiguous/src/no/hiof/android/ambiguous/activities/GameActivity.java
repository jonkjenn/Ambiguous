package no.hiof.android.ambiguous.activities;

import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.AI;
import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.DeckBuilder;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.RandomAmountGenerator;
import no.hiof.android.ambiguous.adapter.GameDeckAdapter;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.layouts.CardLayout;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Player;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameActivity extends Activity implements OnDragListener {
	private SQLiteDatabase db;
	private CardDataSource cs;
	private View layoutView;
	private GridView deckView;

	private Player player;
	private Player opponent;

	private Random computerRandom;

	private enum states {
		PLAYER_TURN, PLAYER_DONE, COMPUTER_TURN, GAME_OVER
	};

	private states state;

	TextView playerstats;
	TextView computerstats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		layoutView = findViewById(R.id.game_layout);

		this.db = Db.getDb(getApplicationContext()).getWritableDatabase();
		this.cs = new CardDataSource(db);

		List<Card> cards = cs.getCards();

		opponent = new Player("Computer",
				(ViewGroup) findViewById(R.id.floating_container2), true);
		opponent.SetDeck(DeckBuilder.StandardDeck(cards));
		computerRandom = new Random();

		player = new Player("Jon",
				(ViewGroup) findViewById(R.id.floating_container));
		player.SetDeck(DeckBuilder.StandardDeck(cards));

		deckView = (GridView) findViewById(R.id.game_grid);
		GameDeckAdapter adapter = new GameDeckAdapter(player.GetCards());
		deckView.setAdapter(adapter);

		setupDragDrop(layoutView);

		playerstats = (TextView) findViewById(R.id.stats_player);
		computerstats = (TextView) findViewById(R.id.stats_computer);

		state = (new Random().nextInt(2) == 0 ? states.PLAYER_TURN
				: states.COMPUTER_TURN);

		updateStatsView();
		changeState();
	}

	private void changeState() {
		Handler h = new Handler();
		h.postAtTime(new Runnable() {

			@Override
			public void run() {
				GameActivity.this.doChangeState();
			}
		}, SystemClock.uptimeMillis() + 1000);
	}

	private void doChangeState() {
		checkDead();

		switch (state) {
		case COMPUTER_TURN:
			computerTurn();
			break;
		case PLAYER_TURN:
			playerTurn();
			break;
		case PLAYER_DONE:
			playerDone();
			break;
		case GAME_OVER:
			break;
		default:
			break;
		}
	}

	private void checkDead() {
		if (!player.getAlive()) {
			playerstats.setText("Player dead");
			state = states.GAME_OVER;
		}
		if (!opponent.getAlive()) {
			computerstats.setText("Computer dead");
			state = states.GAME_OVER;
		}
	}

	private void playerTurn() {
		this.setupDragDrop(this.layoutView);
	}

	private void playerDone() {
		player.ModResource(5);
		this.layoutView.setOnDragListener(null);
		updateStatsView();
		deckView.setAdapter(new GameDeckAdapter(player.GetCards()));
		state = states.COMPUTER_TURN;
		changeState();
	}

	private void updateStatsView() {
		playerstats.setText(player.getStats());
		computerstats.setText(opponent.getStats());
	}

	private void computerTurn() {
		updateStatsView();
		AI ai = new AI(opponent, player);
		int pos = ai.Start();
		if (pos < 0) {
			pos = computerRandom.nextInt(opponent.GetCards().length - 1);
			discardCard(pos);
			opponentDiscardCard(opponent.GetCards()[pos]);
		} else {
			opponentPlayCard(opponent.GetCards()[pos]);
			playCard(opponent.GetCards()[pos], pos);
		}
		opponent.ModResource(5);
		updateStatsView();
		state = states.PLAYER_TURN;
		doChangeState();
	}

	private void playCard(int position) {
		if (state != states.PLAYER_TURN) {
			stopDrag(position);
			return;
		}
		Card card = (Card) deckView.getItemAtPosition(position);
		playCard(card, position);
		doChangeState();
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

	private void discardCard(int position) {

		if (state != states.PLAYER_TURN) {
			return;
		}
		switch (state) {
		case COMPUTER_TURN:
			opponent.CardUsed(position);
			state = states.PLAYER_TURN;
			break;
		case PLAYER_TURN:
			player.CardUsed(position);
			state = states.PLAYER_DONE;
			break;
		default:
			break;
		}
		doChangeState();

	}

	private void playCard(Card card, int position) {
		if (card == null) {
			return;
		}
		switch (state) {
		case COMPUTER_TURN:
			useCard(card, opponent, player, position);
			break;
		case PLAYER_TURN:
			useCard(card, player, opponent, position);
			break;
		default:
			break;
		}
	}

	private void useCard(Card card, Player caster, Player opponent, int position) {
		if (caster.UseResources(card.getCost())) {
			for (int i = 0; i < card.getEffects().size(); i++) {
				Effect e = card.getEffects().get(i);
				switch (e.getTarget()) {
				case OPPONENT:
					useEffect(e, opponent);
					break;
				case SELF:
					useEffect(e, caster);
					break;
				default:
					break;
				}
			}
			caster.CardUsed(position);
			if (state == states.PLAYER_TURN) {
                removeDrag();
				state = states.PLAYER_DONE;
			}
		}else{stopDrag(position);}
	}

	private void useEffect(Effect e, Player target) {
		switch (e.getType()) {
		case ARMOR:
			target.ModArmor(e.getMinValue());
			break;
		case DAMAGE:
			target.Damage(RandomAmountGenerator.GenerateAmount(e.getMinValue(),
					e.getMaxValue(), e.getCrit()));
			break;
		case HEALTH:
			target.Heal(RandomAmountGenerator.GenerateAmount(e.getMinValue(),
					e.getMaxValue(), e.getCrit()));
			break;
		case RESOURCE:
			target.ModResource(e.getMinValue());
			break;
		default:
			break;
		}

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
			android.widget.RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(parent.getLayoutParams());
			par.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			//par.setMargins(x-parent.getWidth()/2, y-parent.getHeight()/2, 0, 0);
			par.setMargins(x-insideX, y-insideY, 0, 0);
			parent.setLayoutParams(par);
		}
	}

	private void startDrag(int card,int x, int y) {
		//deckView.getChildAt(card).setVisibility(View.GONE);
		Card c = this.player.GetCards()[card];

		ViewGroup parent = (ViewGroup) findViewById(R.id.drag_card);
		parent.setVisibility(ViewGroup.GONE);
		parent.removeAllViews();
		parent.addView(CardLayout.getCardLayout(c, parent));
	}

	private void stopDrag(int card) {
		deckView.getChildAt(card).setVisibility(View.VISIBLE);
		removeDrag();
	}
	private void removeDrag()
	{
		ViewGroup parent = (ViewGroup) findViewById(R.id.drag_card);
		parent.removeAllViews();
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		if (state != states.PLAYER_TURN) {
			return false;
		}

		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			if (event.getLocalState() != null) {
				int[] dState = (int[]) event.getLocalState();
				startDrag(dState[1],(int)event.getX(), (int)event.getY());
			}

			// Log.d("test", "Drag started");
			return true;

		case DragEvent.ACTION_DRAG_LOCATION:
			if (event.getLocalState() != null) {
				int[] dState = (int[]) event.getLocalState();
				drag(dState[1],(int) event.getX(), (int) event.getY(),dState[3],dState[4]);

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
					playCard(dragState[1]);
				} else if (dragState.length > 2
						&& dragState[2] / 2 + event.getY() > this.layoutView
								.getHeight()) {
					Log.d("test", "funker?");
					findViewById(R.id.gameview_discard).setVisibility(
							TextView.GONE);
					discardCard(dragState[1]);
					removeDrag();
				} else {
					stopDrag(dragState[1]);
				}
			}
			return true;
		}
		return false;
	}

}
