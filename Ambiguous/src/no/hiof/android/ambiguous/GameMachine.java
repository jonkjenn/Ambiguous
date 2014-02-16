package no.hiof.android.ambiguous;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.OpponentController.OpponentListener;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Player;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.SystemClock;

public class GameMachine implements OpponentListener {
	public Player player;
	public Player opponent;
	public OpponentController opponentController;

	private SQLiteDatabase db;
	private CardDataSource cs;

	private enum states {
		PLAYER_TURN, PLAYER_DONE, OPPONENT_TURN, GAME_OVER
	};

	private states state;

	public GameMachine(SQLiteDatabase db) {
		this.db = db;
		this.cs = new CardDataSource(db);

		List<Card> cards = cs.getCards();

		opponent = new Player("Computer", true);
		opponent.SetDeck(DeckBuilder.StandardDeck(cards));

		opponentController = new OpponentController();
		opponentController.setOpponentListener(this);

		player = new Player("Jon");
		player.SetDeck(DeckBuilder.StandardDeck(cards));

		AIController aIController = new AIController(opponent, player,
				opponentController);
		this.setGameMachineListener(aIController);

		state = (new Random().nextInt(2) == 0 ? states.PLAYER_TURN
				: states.OPPONENT_TURN);

		changeState();
	}

	public boolean playersTurn() {
		return state == states.PLAYER_TURN;
	}

	private void changeState() {
		Handler h = new Handler();
		h.postAtTime(new Runnable() {

			@Override
			public void run() {
				doChangeState();
			}
		}, SystemClock.uptimeMillis() + 1000);
	}

	private void doChangeState() {
		checkDead();

		switch (state) {
		case OPPONENT_TURN:
			notifyOpponentTurn();
			break;
		case PLAYER_TURN:
			notifyPlayerTurn();
			break;
		case PLAYER_DONE:
			player.ModResource(5);
			notifyPlayerDone();
			state = states.OPPONENT_TURN;
			changeState();
			break;
		case GAME_OVER:
			break;
		default:
			break;
		}
	}

	public void PlayerPlayCard(int card) {
		playCard(card);
	}

	public void PlayerDiscardCard(int card) {
		discardCard(card);
	}


	private void checkDead() {
		if (!player.getAlive()) {
			notifyPlayerDead();
			state = states.GAME_OVER;
		}
		if (!opponent.getAlive()) {
			notifyOpponentDead();
			state = states.GAME_OVER;
		}
	}

	private void discardCard(int position) {

		if (state != states.PLAYER_TURN) {
			return;
		}
		switch (state) {
		case PLAYER_TURN:
			player.CardUsed(position);
			state = states.PLAYER_DONE;
			break;
		default:
			break;
		}
		doChangeState();

	}

	private void playCard(int position) {
		if (state != states.PLAYER_TURN) {
			notifyCouldNotPlayCard(position);
			return;
		}
		Card card = player.GetCards()[position];
		playCard(card, position);
		doChangeState();
	}

	private void playCard(Card card, int position) {
		if (card == null) {
			return;
		}
		switch (state) {
		case OPPONENT_TURN:
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
			if (state == states.PLAYER_TURN) {
				caster.CardUsed(position);
				notifyPlayerPlayedCard(player.GetCard(position));
				state = states.PLAYER_DONE;
			}
		} else {
			if (state == states.PLAYER_TURN) {
				notifyCouldNotPlayCard(position);
			}
		}
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

	ArrayList<GameMachineListener> gameMachineListeners = new ArrayList<GameMachineListener>();

	public void setGameMachineListener(GameMachineListener listener) {
		gameMachineListeners.add(listener);
	}

	private void notifyCouldNotPlayCard(int position) {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onCouldNotPlayCardListener(position);
		}
	}

	private void notifyPlayerTurn() {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onPlayerTurnListener();
		}
	}

	private void notifyOpponentTurn() {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onOpponentTurnListener();
		}
	}

	private void notifyPlayerDone() {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onPlayerDoneListener();
		}
	}

	private void notifyPlayerDead() {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onPlayerDeadListener(player);
		}
	}

	private void notifyOpponentDead() {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onOpponentDeadListener(opponent);
		}
	}

	private void notifyPlayerPlayedCard(Card card) {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onPlayerPlayedCard(card);
		}
	}

	/*
	 * private void notifyOpponentPlayedCard(Card card) {
	 * for(GameMachineListener listener:gameMachineListeners) {
	 * listener.onOpponentPlayedCard(card); } }
	 * 
	 * private void notifyOpponentDiscardCard(Card card) {
	 * for(GameMachineListener listener:gameMachineListeners) {
	 * listener.onOpponentDiscardCard(card); } }
	 */

	public interface GameMachineListener {
		void onCouldNotPlayCardListener(int position);

		void onPlayerTurnListener();

		void onPlayerDoneListener();

		void onOpponentTurnListener();

		void onPlayerDeadListener(Player player);

		void onOpponentDeadListener(Player opponent);

		/*
		 * void onOpponentPlayedCard(Card card); void onOpponentDiscardCard(Card
		 * card);
		 */

		void onPlayerPlayedCard(Card card);

		void onPlayerDiscardCard(Card card);
	}

	@Override
	public void onOpponentPlayCard(Card card) {
		playCard(card, 0);
		state = states.PLAYER_TURN;
		doChangeState();
	}

	@Override
	public void onOpponentDiscardCard(Card card) {
		state = states.PLAYER_TURN;
		doChangeState();
	}

}
