package no.hiof.android.ambiguous;

import java.util.Random;

import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Player;

/**
 *Emulates the computers actions as a player.
 */
public class AIController implements GameMachine.TurnChangeListener{
	private Player player;
	private Player computer;
	private Random computerRandom;
	private OpponentController oc;
	
	/**
	 * @param computer The computer/AI player
	 * @param player The human player
	 * @param oc The interface for opponents of the local human player.
	 */
	public AIController(Player computer, Player player, OpponentController oc)
	{
		this.player = player;
		this.computer = computer;
		this.oc = oc;
		computerRandom = new Random();
	}

	/*
	 *  If computer turn, use or discard a card.
	 * (non-Javadoc)
	 * @see no.hiof.android.ambiguous.GameMachine.TurnChangeListener#turnChange(no.hiof.android.ambiguous.model.Player)
	 */
	@Override
	public void turnChange(Player playerGotTurn) {
		if(playerGotTurn != computer){return;};
		AI ai = new AI(computer, player);
		int pos = ai.Start();
		
		 // Discard a random card if did not find one to use.
		if (pos < 0) {
			// Find a card for discarding thats not a resource card.
			do{
                pos = computerRandom.nextInt(computer.GetCards().length - 1);
			}
			while(computer.GetCard(pos).getEffects().get(0).getType()==Effect.EffectType.RESOURCE);

			oc.DiscardCard(computer.GetCard(pos));
			computer.CardUsed(pos);
		} else {
			oc.PlayCard(computer.GetCard(pos),true);
			computer.CardUsed(pos);
		}
		computer.ModResource(5);
	}

}
