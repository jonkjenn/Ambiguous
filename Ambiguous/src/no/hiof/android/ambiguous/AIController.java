package no.hiof.android.ambiguous;

import java.util.Random;

import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;

public class AIController implements GameMachine.GameMachineListener{
	private Player player;
	private Player computer;
	private Random computerRandom;
	private OpponentController oc;
	
	public AIController(Player computer, Player player, OpponentController oc)
	{
		this.player = player;
		this.computer = computer;
		this.oc = oc;
		computerRandom = new Random();
	}

	@Override
	public void onCouldNotPlayCardListener(int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerTurnListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerDoneListener() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpponentTurnListener() {
		AI ai = new AI(computer, player);
		int pos = ai.Start();
		if (pos < 0) {
			do{
                pos = computerRandom.nextInt(computer.GetCards().length - 1);
			}
			while(computer.GetCard(pos).getEffects().get(0).getType()==Effect.EffectType.RESOURCE);
			oc.DiscardCard(computer.GetCard(pos));
			computer.CardUsed(pos);
			//opponentDiscardCard(opponent.GetCards()[pos]);
		} else {
			//opponentPlayCard(opponent.GetCards()[pos]);
			//playCard(computer.GetCards()[pos], pos);
			oc.PlayCard(computer.GetCard(pos),true);
			computer.CardUsed(pos);
		}
		computer.ModResource(5);
	}

	@Override
	public void onPlayerDeadListener(Player player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpponentDeadListener(Player opponent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerPlayedCard(Card card) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerDiscardCard(Card card) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerUsedeffect(EffectType type, Player target, int amount) {
		// TODO Auto-generated method stub
		
	}

}
