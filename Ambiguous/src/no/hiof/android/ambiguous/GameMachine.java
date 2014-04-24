package no.hiof.android.ambiguous;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.OpponentController.OpponentListener;
import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player.PlayerUpdateListener;
import no.hiof.android.ambiguous.model.Player;
import android.os.Handler;
import android.os.SystemClock;

//TODO:Class has been simplified but might still be applicable. This class should be split up into smaller parts. 
/**
 * Moves the game between the different game states. Listens to player and
 * opponent game choices. Applies the effects from the cards used by players.
 * Notifies listeners about game changes.
 */
public class GameMachine implements OpponentListener, PlayerUpdateListener {

	public Player player;
	public Player opponent;

	public Card currentOpponentCard;
	public boolean opponentCardIsDiscarded;
	public boolean cheatUsed;

	public int delay;

	public enum State {
		PLAYER_TURN, PLAYER_DONE, OPPONENT_TURN, GAME_OVER
	};

	// Current state of the game.
	public State state = null;

	/**
	 * Can only create class by a builder.
	 * 
	 * @param builder
	 */
	public GameMachine(List<Card> cards) {
		player = new Player("Local player");
		opponent = new Player("Opponent");
		addPlayerListeners();
		opponent.setDeck(DeckBuilder.StandardDeck(cards));
		player.setDeck(DeckBuilder.StandardDeck(cards));
		this.delay = 1000;
	}

	public boolean isPlayersTurn() {
		return state == State.PLAYER_TURN;
	}

	/**
	 * Uses the state the GameMachine currently has.
	 */
	public void startGame() {
		changeState();
	}

	/**
	 * Starts with the state passed.
	 * 
	 * @param state
	 *            If null generates a state by random.
	 */
	public void startGame(State state) {
		if (state == null) {
			startRandom();
			return;
		}
		this.state = state;
		changeState();
	}

	/**
	 * Starts with a random state.
	 */
	public void startRandom() {
		// Randomly pick if player or opponent should start game.
		state = (new Random().nextInt(2) == 0 ? State.PLAYER_TURN
				: State.OPPONENT_TURN);

		changeState();
	}

	/**
	 * Gives a delay before doing the actual state change.
	 */
	private void changeState() {
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
		notifyStateChange();

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
			player.modResource(5);
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

	public void playerPlayCard(int card, int amount) {
		if (state != State.PLAYER_TURN
				|| player.resources < player.getHand()[card].cost) {
			notifyCouldNotPlayCard(card);
			return;
		}
		Card c = player.getHand()[card];
		Effect e = c.effects.get(0);
		e.minValue = amount;
		e.maxValue = (amount);
		e.crit = 0;
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
		if (!player.isAlive()) {
			notifyPlayerDead();
			state = State.GAME_OVER;
		} else if (!opponent.isAlive()) {
			notifyOpponentDead();
			state = State.GAME_OVER;
		}
	}
	
	public void setStateAndNotify(State state)
	{
		this.state = state;
		notifyStateChange();		
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
		notifyPlayerDiscardCard(player.getCard(position));
		player.cardUsed(position);
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
		Card card = player.getHand()[position];
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
				|| !caster.useResources(card.cost)) {
			if (caster == this.player) {
				notifyCouldNotPlayCard(position);
			}
			return;
		}

		// Decides if the player or opponent is the target of the card.
		Player target = (caster == player ? opponent : player);

		useCard(card, caster, target);
		caster.cardUsed(position);

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
		for (int i = 0; i < card.effects.size(); i++) {
			Effect e = card.effects.get(i);
			switch (e.target) {
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
		switch (e.type) {
		case ARMOR:
			target.modArmor(amount);
			break;
		case DAMAGE:
			target.damage(amount);
			break;
		case HEALTH:
			target.heal(amount);
			break;
		case RESOURCE:
			target.modResource(amount);
			break;
		default:
			break;
		}

		if (state == State.PLAYER_TURN) {
			notifyPlayerUsedEffect(e.type, target, amount);
		}
	}

	/**
	 * Generate a effect amount and apply it to a target player.
	 * 
	 * @param e
	 * @param target
	 */
	public void useEffect(Effect e, Player target) {
		switch (e.type) {
		case ARMOR:
			useEffect(e, target, (e.minValue));
			break;
		case DAMAGE:
			useEffect(e, target, RandomAmountGenerator.GenerateAmount(
					e.minValue, e.maxValue, e.crit));
			break;
		case HEALTH:
			useEffect(e, target, RandomAmountGenerator.GenerateAmount(
					e.minValue, e.maxValue, e.crit));
			break;
		case RESOURCE:
			useEffect(e, target, e.minValue);
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

	public void removeGameMachineListener(GameMachineListener listener) {
		gameMachineListeners.remove(listener);
	}

	public void clearGameMachineListener() {
		gameMachineListeners.clear();
	}

	public void setTurnChangeListener(TurnChangeListener listener) {
		turnChangeListeners.add(listener);
	}

	public void removeTurnChangeListener(TurnChangeListener listener) {
		turnChangeListeners.remove(listener);
	}

	public void clearTurnChangedListener() {
		turnChangeListeners.clear();
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

	void notifyPlayerDiscardCard(Card card) {
		for (GameMachineListener listener : gameMachineListeners) {
			listener.onPlayerDiscardCard(card);
		}
	}

	public interface TurnChangeListener {
		void turnChange(Player player);
	}

	// TODO: Split into different interfaces
	/**
	 * Changes emerging from GameMachine
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

	public interface OnPlayerUpdates {
		void onCardsUpdateListener(Player player, Card[] cards);

		void onStatsUpdateListener(Player player);

		void onStatChange(Player player, int amount, EffectType type);
	}

	OnPlayerUpdates onPlayerUpdatesListener;

	public void setOnPlayerUpdatesListener(OnPlayerUpdates listener) {
		this.onPlayerUpdatesListener = listener;
		if (this.onPlayerUpdatesListener != null) {
			postAllPlayerUpdates();
		}
	}

	void postAllPlayerUpdates() {
		onPlayerUpdatesListener.onCardsUpdateListener(player, player.getHand());
		onPlayerUpdatesListener.onStatsUpdateListener(player);
		onPlayerUpdatesListener.onStatsUpdateListener(opponent);
	}

	public interface OnStateChangeListener {
		void onStateChanged(State state);
	}

	final ArrayList<OnStateChangeListener> onStateChangedListeners = new ArrayList<GameMachine.OnStateChangeListener>();

	public void setOnStateChangeListener(OnStateChangeListener listener) {
		onStateChangedListeners.add(listener);
	}

	public void removeOnStateChangedListener(OnStateChangeListener listener) {
		onStateChangedListeners.remove(listener);
	}

	void notifyStateChange() {
		for (OnStateChangeListener l : onStateChangedListeners) {
			l.onStateChanged(state);
		}
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
			opponent.useResources(card.cost);
		}

		currentOpponentCard = card;
		opponentCardIsDiscarded = false;
	}

	/**
	 * Applies effect directly on target.
	 */
	@Override
	public void onOpponentUsedEffect(EffectType type, Player target,
			int amount, boolean onlyDisplay) {
		if (onlyDisplay) {
			return;
		}
		switch (type) {
		case ARMOR:
			target.modArmor(amount);
			break;
		case DAMAGE:
			target.damage(amount);
			break;
		case HEALTH:
			target.heal(amount);
			break;
		case RESOURCE:
			target.modResource(amount);
			break;
		default:
			break;
		}
		checkDead();
	}

	@Override
	public void onOpponentTurnDone() {
		if (state == null || state == State.OPPONENT_TURN) {
			state = State.PLAYER_TURN;
			changeState();
		}
	}

	@Override
	public void onOpponentDiscardCard(int card) {
		state = State.PLAYER_TURN;
		doChangeState();

		currentOpponentCard = CardDataSource.getCard(card);
		opponentCardIsDiscarded = false;
	}

	@Override
	public void previousCardPlayed(Card card, boolean discarded) {
		currentOpponentCard = card;
		opponentCardIsDiscarded = discarded;
	}

	/*
	 * Creates a new "blank" match with new players and no state.
	 */
	public void reset() {
		player = new Player("Local player");
		opponent = new Player("Opponent");
		player.setDeck(DeckBuilder.StandardDeck());
		opponent.setDeck(DeckBuilder.StandardDeck());
		state = null;
		addPlayerListeners();
		GameActivity.opponentController.previousCardPlayed(null, false);
	}

	void addPlayerListeners() {
		player.setPlayerUpdateListener(this);
		opponent.setPlayerUpdateListener(this);
	}

	@Override
	public void onCardsUpdateListener(Player player, Card[] cards) {
		if (onPlayerUpdatesListener != null) {
			onPlayerUpdatesListener.onCardsUpdateListener(player, cards);
		}
	}

	@Override
	public void onStatsUpdateListener(Player player) {
		if (onPlayerUpdatesListener != null) {
			onPlayerUpdatesListener.onStatsUpdateListener(player);
		}
	}

	@Override
	public void onStatChange(Player player, int amount, EffectType type) {
		if (onPlayerUpdatesListener != null) {
			onPlayerUpdatesListener.onStatChange(player, amount, type);
		}
	}
}
