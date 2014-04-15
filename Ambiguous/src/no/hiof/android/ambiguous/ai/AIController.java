package no.hiof.android.ambiguous.ai;

import java.util.Random;

import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.OpponentController;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Player;

/**
 * Emulates the computers actions as a player.
 */
public class AIController implements GameMachine.TurnChangeListener {
	private Player player;
	private Player computer;
	private Random computerRandom;
	private OpponentController oc;

	/**
	 * @param computer
	 *            The computer/AI player
	 * @param player
	 *            The human player
	 * @param oc
	 *            The interface for opponents of the local human player.
	 */
	public AIController(Player computer, Player player, OpponentController oc) {
		this.player = player;
		this.computer = computer;
		this.oc = oc;
		computerRandom = new Random();
	}

	/*
	 * If computer turn, use or discard a card. (non-Javadoc)
	 * 
	 * @see
	 * no.hiof.android.ambiguous.GameMachine.TurnChangeListener#turnChange(no
	 * .hiof.android.ambiguous.model.Player)
	 */
	@Override
	public void turnChange(Player playerGotTurn) {
		if (playerGotTurn != computer) {
			return;
		}
		;
		AI ai = new AI(computer, player);
		int pos = ai.Start();

		// Discard a random card if did not find one to use.
		if (pos < 0) {
			// Find a card for discarding thats not a resource card.
			do {
				pos = computerRandom.nextInt(computer.getHand().length - 1);
			} while (computer.getCard(pos).effects.get(0).type == Effect.EffectType.RESOURCE);

			oc.DiscardCard(computer.getCard(pos));
			computer.cardUsed(pos);
		} else {
			oc.PlayCard(computer.getCard(pos), true);
			computer.cardUsed(pos);
		}
		computer.modResource(5);
	}

}
