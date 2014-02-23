package no.hiof.android.ambiguous;

import java.util.ArrayList;

import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;

public class OpponentController {

	CardDataSource cs;

	public OpponentController(CardDataSource cs) {
		this.cs = cs;
	}

	public void PlayCard(int card, boolean generateDamage) {
		PlayCard(cs.getCard(card), generateDamage);
	}

	public void UseEffect(EffectType type, Player target, int amount) {
		notifyUsedEffect(type, target, amount);
	}

	public void PlayCard(Card card, boolean generateDamage) {
		notifyPlayCard(card, generateDamage);
	}

	public void TurnDone() {
		notifyTurnDone();
	}

	public void DiscardCard(int card) {
		DiscardCard(cs.getCard(card));
	}

	public void DiscardCard(Card card) {
		notifyDiscardCard(card);
	}

	private void notifyPlayCard(Card card, boolean generateDamage) {
		for (OpponentListener listener : listeners) {
			listener.onOpponentPlayCard(card, generateDamage);
		}
	}

	private void notifyUsedEffect(EffectType type, Player target, int amount) {
		for (OpponentListener listener : listeners) {
			listener.onOpponentUsedEffect(type, target, amount);
		}
	}

	private void notifyDiscardCard(Card card) {
		for (OpponentListener listener : listeners) {
			listener.onOpponentDiscardCard(card);
		}
	}

	private void notifyTurnDone() {
		for (OpponentListener listener : listeners) {
			listener.onOpponentTurnDone();
		}
	}

	private ArrayList<OpponentListener> listeners = new ArrayList<OpponentListener>();

	public void setOpponentListener(OpponentListener listener) {
		listeners.add(listener);
	}

	public interface OpponentListener {
		void onOpponentPlayCard(Card card, boolean generateDamage);

		void onOpponentUsedEffect(EffectType type, Player target, int amount);

		void onOpponentDiscardCard(Card card);

		void onOpponentTurnDone();
	}

}
