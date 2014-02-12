package no.hiof.android.ambiguous;

import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Player;

public class AI {
	private Random rand = new Random();
	private Player computer;
	private Player player;
	private Card[] cards;
	
	public AI(Player computer,Player player)
	{
		this.player = player;
		this.computer = computer;		
		
		this.cards = new Card[computer.GetCards().length];
		
		cards = computer.GetCards();
	}
	
	public int Start()
	{
		if(computer.getHealth() > 80)
		{
			int card = -1;
			
			if(computer.getResources() < 4)
			{
                    card = hasEffect(Effect.EffectType.RESOURCE, Effect.Target.SELF);
                    if(card >= 0)
                    {
                    	return card;
                    }
			}

			card = hasEffect(Effect.EffectType.DAMAGE,Effect.Target.OPPONENT);
			if(card >=0)
			{
				return card;
			}
			
			card = hasEffect(Effect.EffectType.ARMOR,Effect.Target.SELF);
			if(card >= 0)
			{
				return card;
			}
			
			if(computer.getResources() < 100)
			{
                    card = hasEffect(Effect.EffectType.RESOURCE, Effect.Target.SELF);
                    if(card >= 0)
                    {
                    	return card;
                    }
			}
		}
		else if(computer.getHealth() < 80)
		{
			int card = -1;
			
			if(computer.getResources() < 4)
			{
                    card = hasEffect(Effect.EffectType.RESOURCE, Effect.Target.SELF);
                    if(card >= 0)
                    {
                    	return card;
                    }
			}

			card = hasEffect(Effect.EffectType.HEALTH,Effect.Target.SELF);
			if(card >=0)
			{
				return card;
			}
			
			card = hasEffect(Effect.EffectType.ARMOR,Effect.Target.SELF);
			if(card >= 0)
			{
				return card;
			}

			card = hasEffect(Effect.EffectType.DAMAGE,Effect.Target.OPPONENT);
			if(card >=0)
			{
				return card;
			}
			
			if(computer.getResources() < 100)
			{
                    card = hasEffect(Effect.EffectType.RESOURCE, Effect.Target.SELF);
                    if(card >= 0)
                    {
                    	return card;
                    }
			}
			
		}
		

		
		return -1;
	}
	
	private int hasEffect(Effect.EffectType type, Effect.Target target)
	{
		int outcard = -1;
		for(int i=0;i<cards.length;i++)
		{
			int effect = hasEffect(cards[i].getEffects(),type, target);

			if(effect >= 0 && cards[i].getCost() <= computer.getResources())
			{
				if(outcard < 0){outcard = i;}
				else
				{
					if(cards[i].getCost() > cards[outcard].getCost())
					{
						outcard = i;
					}
				}
			}
		}
		
		return outcard;
	}
	
	private int hasEffect(List<Effect> effects, Effect.EffectType type, Effect.Target target)
	{
		for(int i=0;i<effects.size();i++)
		{
			if(effects.get(i).getType() == type && effects.get(i).getTarget() == target)
			{
				return i;
			}
		}
		return -1;
	}

}
