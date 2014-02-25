package no.hiof.android.ambiguous;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.OpponentController.OpponentListener;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.SystemClock;

public class GameMachine implements OpponentListener {
	public Player player;
	public Player opponent;
	public OpponentController opponentController;
	private NetworkOpponent networkOpponent;

	private CardDataSource cs;

	public enum State {
		PLAYER_TURN, PLAYER_DONE, OPPONENT_TURN, GAME_OVER
	};

	public State state;

	public GameMachine(SQLiteDatabase db, Socket socket, boolean isServer){
		
		this(db, socket, isServer, null, null, null);
	}
	public GameMachine(SQLiteDatabase db, Socket socket, boolean isServer, Player savedPlayer, Player savedOpponent, State new_york ) {

		this.cs = new CardDataSource(db);

		List<Card> cards = cs.getCards();

		if(savedPlayer != null){
			player = savedPlayer;
		}
		else{
			player = new Player("JonAndOrAdrian");
			player.SetDeck(DeckBuilder.StandardDeck(cards));
		}

        opponentController = new OpponentController(cs);
        opponentController.setOpponentListener(this);

		if (socket == null) {

			if(savedOpponent != null){
				opponent = savedOpponent; 
			}
			else{
		        opponent = new Player("Computer");
		        opponent.SetDeck(DeckBuilder.StandardDeck(cards));
			}

            AIController aIController = new AIController(opponent, player,
                            opponentController);
            this.setGameMachineListener(aIController);

            state = (state == null ? (new Random().nextInt(2) == 0 ? State.PLAYER_TURN
                            : State.OPPONENT_TURN) : new_york);
            changeState();
		}
		else
		{
			if(savedOpponent != null){
				opponent = savedOpponent;
			}
			else{
	            opponent = new Player("Network");
	            opponent.SetDeck(DeckBuilder.StandardDeck(cards));
			}
			
			networkOpponent = new NetworkOpponent(opponentController,player,opponent,socket);
			setGameMachineListener(networkOpponent);

			if(isServer)
            {state = (new Random().nextInt(2) == 0 ? State.PLAYER_TURN
				: State.OPPONENT_TURN);
                changeState();
            }
		}


	}

	public boolean playersTurn() {
		return state == State.PLAYER_TURN;
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
			state = State.OPPONENT_TURN;
			changeState();
			break;
		case GAME_OVER:
			break;
		default:
			break;
		}
	}

	public void PlayerPlayCard(int card) {
		playCard(player, card);
	}

	public void PlayerDiscardCard(int card) {
		discardCard(card);
	}

	private void checkDead() {
		if (!player.getAlive()) {
			notifyPlayerDead();
			state = State.GAME_OVER;
		}
		if (!opponent.getAlive()) {
			notifyOpponentDead();
			state = State.GAME_OVER;
		}
	}

	private void discardCard(int position) {

		if (state != State.PLAYER_TURN) {
			return;
		}
		player.CardUsed(position);
		state = State.PLAYER_DONE;
		doChangeState();
	}

	private void playCard(Player player, int position) {
		if (state != State.PLAYER_TURN) {
			notifyCouldNotPlayCard(position);
			return;
		}
		Card card = player.GetCards()[position];
		playCard(player, card, position);
		doChangeState();
	}

	private void playCard(Player caster, Card card, int position) {
		if (card == null || caster == this.player
				&& state == State.OPPONENT_TURN || player == this.opponent
				&& state == State.PLAYER_TURN
				|| !caster.UseResources(card.getCost())) {
			if (state == State.PLAYER_TURN) {
				notifyCouldNotPlayCard(position);
			}
			return;
		}

		Player target = (caster == player ? opponent : player);

		useCard(card, caster, target);
		caster.CardUsed(position);

		if (state == State.PLAYER_TURN) {
			notifyPlayerPlayedCard(card);
			state = State.PLAYER_DONE;
		}
	}

	public void useCard(Card card, Player caster, Player opponent) {
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
	}

	public void useEffect(Effect e, Player target, int amount) {
		switch (e.getType()) {
		case ARMOR:
			target.ModArmor(amount);
			break;
		case DAMAGE:
			target.Damage(amount);
			break;
		case HEALTH:
			target.Heal(amount);
			break;
		case RESOURCE:
			target.ModResource(amount);
			break;
		default:
			break;
		}

		if (state == State.PLAYER_TURN) {
			notifyPlayerUsedEffect(e.getType(), target, amount);
		}
	}

	public void useEffect(Effect e, Player target) {
		switch (e.getType()) {
		case ARMOR:
			useEffect(e, target, (e.getMinValue()));
			break;
		case DAMAGE:
			useEffect(
					e,
					target,
					RandomAmountGenerator.GenerateAmount(e.getMinValue(),
							e.getMaxValue(), e.getCrit()));
			break;
		case HEALTH:
			useEffect(
					e,
					target,
					RandomAmountGenerator.GenerateAmount(e.getMinValue(),
							e.getMaxValue(), e.getCrit()));
			break;
		case RESOURCE:
			useEffect(e, target, e.getMinValue());
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

	private void notifyPlayerUsedEffect(EffectType type, Player target,
			int amount) {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onPlayerUsedeffect(type, target, amount);
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

		void onPlayerUsedeffect(EffectType type, Player target, int amount);

		void onPlayerDiscardCard(Card card);
	}

	@Override
	public void onOpponentPlayCard(Card card, boolean generateDamage) {
		if (generateDamage) {
			playCard(opponent, card, 0);
			state = State.PLAYER_TURN;
			doChangeState();
		}
		
	}

	@Override
	public void onOpponentDiscardCard(Card card) {
		state = State.PLAYER_TURN;
		doChangeState();
	}

	@Override
	public void onOpponentUsedEffect(EffectType type, Player target, int amount) {
		switch (type) {
		case ARMOR:
			target.ModArmor(amount);
			break;
		case DAMAGE:
			target.Damage(amount);
			break;
		case HEALTH:
			target.Heal(amount);
			break;
		case RESOURCE:
			target.ModResource(amount);
			break;
		default:
			break;
		}
	}

	@Override
	public void onOpponentTurnDone() {
		if (state == null || state == State.OPPONENT_TURN) {
			state = State.PLAYER_TURN;
			changeState();
		}
	}

}
