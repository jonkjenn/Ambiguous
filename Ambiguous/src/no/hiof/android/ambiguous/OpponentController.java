package no.hiof.android.ambiguous;

import java.util.ArrayList;

import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;

/**
 * For sending opponent actions to the gamemachine and GUI. Helps computer and network opponent to be treated equal in games.
 */
public class OpponentController {

	public void playCard(int card, boolean generateDamage) {
		playCard(CardDataSource.getCard(card), generateDamage);
	}

	public void useEffect(EffectType type, Player target, int amount, boolean onlyDisplay) {
		notifyUsedEffect(type, target, amount, onlyDisplay);
	}
	
	public void playCard(Card card, boolean generateDamage) {
		notifyPlayCard(card, generateDamage);
	}

	public void turnDone() {
		notifyTurnDone();
	}

	public void discardCard(int card) {
		discardCard(card);
	}
	
	public void previousCardPlayed(Card card, boolean discarded)
	{
		notifyPreviousCardPlayed(card, discarded);
	}

	private void notifyPlayCard(Card card, boolean generateDamage) {
		for (OpponentListener listener : listeners) {
			listener.onOpponentPlayCard(card, generateDamage);
		}
	}

	private void notifyUsedEffect(EffectType type, Player target, int amount, boolean onlyDisplay) {
		for (OpponentListener listener : listeners) {
			listener.onOpponentUsedEffect(type, target, amount, onlyDisplay);
		}
	}

	private void notifyTurnDone() {
		for (OpponentListener listener : listeners) {
			listener.onOpponentTurnDone();
		}
	}
	
	void notifyPreviousCardPlayed(Card card, boolean discarded){
		for(OpponentListener l : listeners)
		{
			l.previousCardPlayed(card, discarded);
		}
	}

	private ArrayList<OpponentListener> listeners = new ArrayList<OpponentListener>();

	public void setOpponentListener(OpponentListener listener) {
		listeners.add(listener);
	}
	
	public void removeOpponentListener(OpponentListener listener)
	{
		listeners.remove(listener);
	}
	
	/**
	 * Remove all opponentlisteners
	 */
	public void clearOpponentListener()
	{
		listeners.clear();
	}

	public interface OpponentListener {
		void onOpponentPlayCard(Card card, boolean generateDamage);

		void onOpponentUsedEffect(EffectType type, Player target, int amount, boolean onlyDisplay);

		void onOpponentDiscardCard(int card);

		void onOpponentTurnDone();
		
		void previousCardPlayed(Card card, boolean discarded);
	}

}
