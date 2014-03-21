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

//TODO: This class should be split up into smaller parts.
/**
 * Sets up the game and moves the game between the different game states.
 * Listens to player and opponent game choices. Applies the effects from the
 * cards used by players. Notifies listeners about game changes.
 */
public class GameMachine implements OpponentListener {
	public Player player;
	public Player opponent;
	public OpponentController opponentController;
	private NetworkOpponent networkOpponent;
	private boolean isNetwork = false;

	private CardDataSource cs;

	public enum State {
		PLAYER_TURN, PLAYER_DONE, OPPONENT_TURN, GAME_OVER
	};

	// Current state of the game.
	public State state;

	/**
	 * @param db
	 *            An open writable database.
	 * @param socket
	 *            Optional network socket.
	 * @param isServer
	 *            True if this game instance is a server.
	 */
	public GameMachine(SQLiteDatabase db, Socket socket, boolean isServer) {

		this(db, socket, isServer, null, null, null);
	}

	/**
	 * 
	 * @param db
	 *            An open writable database.
	 * @param socket
	 *            Optional network socket.
	 * @param isServer
	 *            True if this game instance is a server.
	 * @param savedPlayer
	 *            Used as player instead of creating a new player.
	 * @param savedOpponent
	 *            Used as opponent instead of creating new opponent.
	 * @param turn
	 *            Current state used instead of creating new random state.
	 */
	public GameMachine(SQLiteDatabase db, Socket socket, boolean isServer,
			Player savedPlayer, Player savedOpponent, State turn) {

		this.cs = new CardDataSource(db);

		List<Card> cards = cs.getCards();

		if (savedPlayer != null) {
			player = savedPlayer;
		} else {
			player = new Player("JonAndOrAdrian");
			player.SetDeck(DeckBuilder.StandardDeck(cards));
		}

		opponentController = new OpponentController(cs);
		opponentController.setOpponentListener(this);

		if (savedOpponent != null) {
			opponent = savedOpponent;
		} else {
			opponent = new Player("Opponent");
			opponent.SetDeck(DeckBuilder.StandardDeck(cards));
		}

		if (socket == null) {

			AIController aIController = new AIController(opponent, player,
					opponentController);
			this.setTurnChangeListener(aIController);

		} else {
			isNetwork = true;

			networkOpponent = new NetworkOpponent(opponentController, player,
					opponent, socket);
			setGameMachineListener(networkOpponent);
		}

		if (!isNetwork || isServer) {
			state = (turn == null ? (new Random().nextInt(2) == 0 ? State.PLAYER_TURN
					: State.OPPONENT_TURN)
					: turn);
			changeState();
		}

	}

	public boolean isPlayersTurn() {
		return state == State.PLAYER_TURN;
	}

	/**
	 * Gives a delay before doing the actual state change.
	 */
	private void changeState() {
		int delay = (isNetwork ? 50 : 1000);
		Handler h = new Handler();
		h.postAtTime(new Runnable() {

			@Override
			public void run() {
				doChangeState();
			}
		}, SystemClock.uptimeMillis() + delay);
	}

	/**
	 * Changes the current game state.
	 */
	private void doChangeState() {
		checkDead();

		switch (state) {
		case OPPONENT_TURN:
			notifyOpponentTurn();
			notifyTurnChanged(this.opponent);
			break;
		case PLAYER_TURN:
			notifyPlayerTurn();
			notifyTurnChanged(this.player);
			break;
		case PLAYER_DONE:
			// TODO:This should be moved elsewhere.
			player.ModResource(5);
			notifyPlayerUsedEffect(EffectType.RESOURCE, player, 5);
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

	// TODO: Think these functions should be done a different way.
	/**
	 * Actions in the UI ends up calling this function for player using a card.
	 * 
	 * @param card
	 *            The position in the players card "hand" array.
	 */
	public void playerPlayCard(int card) {
		playCard(card);
	}
	
	public void playerPlayCard(int card,int amount)
	{
		if (state != State.PLAYER_TURN || player.getResources() < player.GetCards()[card].getCost()) {
			notifyCouldNotPlayCard(card);
			return;
		}
		Card c = player.GetCards()[card];
		c.getEffects().get(0).setMinValue(amount).setMaxValue(amount).setCrit(0);
		playCard(player, c, card);
		doChangeState();
	}

	/**
	 * Actions in the UI ends up calling this function for player discarding a
	 * card.
	 * 
	 * @param card
	 *            The position in the players card "hand" array.
	 */
	public void playerDiscardCard(int card) {
		discardCard(card);
	}

	/**
	 * Checks if player or opponent is dead and changes state if so.
	 */
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

	/**
	 * Discards a card from the player and ends player's turn.
	 * 
	 * @param position
	 *            The position in the card "hand" array of the player.
	 */
	private void discardCard(int position) {

		if (state != State.PLAYER_TURN) {
			return;
		}
		player.CardUsed(position);
		state = State.PLAYER_DONE;
		doChangeState();
	}

	/**
	 * Plays a card for the player and ends player's turn.
	 * 
	 * @param position
	 *            The position in the player's card "hand" array.
	 */
	private void playCard(int position) {
		if (state != State.PLAYER_TURN) {
			notifyCouldNotPlayCard(position);
			return;
		}
		Card card = player.GetCards()[position];
		playCard(player, card, position);
		doChangeState();
	}

	/**
	 * Checks if the cast has enough resources. sorts out who is the target of
	 * the card.
	 * 
	 * @param caster
	 *            The player that uses the card.
	 * @param card
	 * @param position
	 *            The position of the card in the player's card "hand" array.
	 */
	private void playCard(Player caster, Card card, int position) {
		// Some checks to avoid using cards at incorrect states or if do not
		// have enough resources.
		if (card == null || caster == this.player && state != State.PLAYER_TURN
				|| caster == this.opponent && state != State.OPPONENT_TURN
				|| !caster.UseResources(card.getCost())) {
			if (caster == this.player) {
				notifyCouldNotPlayCard(position);
			}
			return;
		}

		// Decides if the player or opponent is the target of the card.
		Player target = (caster == player ? opponent : player);

		useCard(card, caster, target);
		caster.CardUsed(position);

		if (state == State.PLAYER_TURN) {
			notifyPlayerPlayedCard(card);
			state = State.PLAYER_DONE;
		}
	}
	

	/**
	 * Iterate through a cards effect and apply them to the target.
	 * 
	 * @param card
	 * @param caster
	 * @param target
	 */
	public void useCard(Card card, Player caster, Player target) {
		for (int i = 0; i < card.getEffects().size(); i++) {
			Effect e = card.getEffects().get(i);
			switch (e.getTarget()) {
			case OPPONENT:
				useEffect(e, target);
				break;
			case SELF:
				useEffect(e, caster);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * Apply a specific effect amount to a target player.
	 * 
	 * @param e
	 * @param target
	 * @param amount
	 */
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

	/**
	 * Generate a effect amount and apply it to a target player.
	 * 
	 * @param e
	 * @param target
	 */
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

	// Listeners to changes happening in gamemachine
	ArrayList<GameMachineListener> gameMachineListeners = new ArrayList<GameMachineListener>();
	// Listeners to turn changes.
	ArrayList<TurnChangeListener> turnChangeListeners = new ArrayList<TurnChangeListener>();

	public void setGameMachineListener(GameMachineListener listener) {
		gameMachineListeners.add(listener);
	}

	public void setTurnChangeListener(TurnChangeListener listener) {
		turnChangeListeners.add(listener);
	}

	private void notifyTurnChanged(Player player) {
		for (TurnChangeListener listener : turnChangeListeners) {
			listener.turnChange(player);
		}
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

	public interface TurnChangeListener {
		void turnChange(Player player);
	}

	/**
	 * Changes emerging from gamemachine
	 */
	public interface GameMachineListener {
		void onCouldNotPlayCardListener(int position);

		void onPlayerTurnListener();

		void onPlayerDoneListener();

		void onOpponentTurnListener();

		void onPlayerDeadListener(Player player);

		void onOpponentDeadListener(Player opponent);

		void onPlayerPlayedCard(Card card);

		void onPlayerUsedeffect(EffectType type, Player target, int amount);

		void onPlayerDiscardCard(Card card);
	}

	// TODO:Do something else with the bool
	@Override
	/**
	 * Handle when opponent plays cards.  
	 * @parameter generateAmount Should only be false during network play.
	 */
	public void onOpponentPlayCard(Card card, boolean generateAmount) {
		if (generateAmount) {
			playCard(opponent, card, 0);
			state = State.PLAYER_TURN;
			doChangeState();
		} else {
			opponent.UseResources(card.getCost());
		}

	}

	@Override
	public void onOpponentDiscardCard(Card card) {
		state = State.PLAYER_TURN;
		doChangeState();
	}

	@Override
	/**
	 * Applies effect directly on target.
	 */
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
