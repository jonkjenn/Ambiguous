package no.hiof.android.ambiguous;

import java.util.List;

import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Player;

public class AI {
	private Player computer;
	private Player player;
	private Card[] cards;

	public AI(Player computer, Player player) {
		this.player = player;
		this.computer = computer;

		this.cards = new Card[computer.GetCards().length];

		cards = computer.GetCards();
	}

	public int Start()
	{
        int resource = hasEffect(Effect.EffectType.RESOURCE, Effect.Target.SELF);
        int damage =hasEffect(Effect.EffectType.DAMAGE,Effect.Target.OPPONENT); 
        int armor =hasEffect(Effect.EffectType.ARMOR,Effect.Target.SELF);
		int heal = hasEffect(Effect.EffectType.HEALTH,Effect.Target.SELF);
        int moredamage = hasDamageMoreThenPlayer();

		if(computer.getHealth() + computer.getArmor() > 100)
		{
			if(moredamage >= 0)
			{
				return moredamage;				
			}
			else if(resource >=0 && computer.getResources() < 25)
			{
				return resource;
			}
			
			if(damage >= 0)
			{
				return damage;
			}
			
			armor = hasArmorLessMaxArmor()[0];
			if(armor >= 0){return armor;}
			
			heal = hasHealLessMaxHP()[0];
			if(heal >= 0){return heal;}
		}
		else if(computer.getHealth() + computer.getArmor() <= 100)
		{
			int[] armor_a= hasArmorLessMaxArmor();
			int[] heal_a= hasHealLessMaxHP();
			if(armor_a[0] >= 0 && heal_a[0] >= 0)
			{
				if(armor_a[1] >= heal_a[1])
				{
					return armor_a[0];
				}
				return heal_a[0];
			}
			else if(armor_a[0] >= 0)
			{
				return armor_a[0];
			}
			else if(heal_a[0] >= 0)
			{
				return heal_a[0];
			}
			
			if(moredamage>=0){return moredamage;}
			
			if(resource >=0){return resource;}
			
			if(damage>=0){return damage;}
			
		}
		
		return -1;
	}
	
	private int hasDamageMoreThenPlayer()
	{
		return hasminEffect(player.getHealth() + player.getArmor(),Effect.EffectType.DAMAGE,Effect.Target.OPPONENT)[0];
	}
	
	private int[] hasHealLessMaxHP() {
		return hasEffect(computer.maxHealth - computer.getHealth(),
				Effect.EffectType.HEALTH, Effect.Target.SELF);
	}

	private int[] hasArmorLessMaxArmor() {
		return hasEffect(computer.maxArmor - computer.getArmor(),
				Effect.EffectType.ARMOR, Effect.Target.SELF);
	}


	private int[] hasminEffect(int min_amount, Effect.EffectType type,
			Effect.Target target) {
		int outcard = -1;
		int outamount = -1;

		for (int i = 0; i < cards.length; i++) {
			int effect = hasEffect(cards[i].getEffects(), type, target);

			if (effect >= 0 && cards[i].getCost() <= computer.getResources()) {
				Effect e = cards[i].getEffects().get(effect);
				if ((calcAvg(e)>min_amount) && (calcAvg(e)>outamount || outcard <= 0)) {
					outcard = i;
					outamount = calcAvg(e);
				}
			}
		}
		return new int[]{outcard,outamount};
	}
	
	private int[] hasEffect(int amount, Effect.EffectType type,
			Effect.Target target) {
		int outcard = -1;
		int outamount = -1;

		for (int i = 0; i < cards.length; i++) {
			int effect = hasEffect(cards[i].getEffects(), type, target);

			if (effect >= 0 && cards[i].getCost() <= computer.getResources()) {
				Effect e = cards[i].getEffects().get(effect);
				if ((calcAvg(e) - amount < 5)
						&& ((Math.abs(amount - calcAvg(e)) < Math.abs(amount
								- outamount)) || outcard <= 0)) {
					outcard = i;
					outamount = calcAvg(e);
				}
			}
		}
		return new int[]{outcard,outamount};
	}

	private int calcAvg(Effect effect) {
		if(effect.getMaxValue()>0)
		{
		return (effect.getMinValue() + effect.getMaxValue()) / 2
				+ (int) (effect.getCrit() * 0.25);
		}
		else
		{
			return effect.getMinValue();
		}
	}

	private int hasEffect(Effect.EffectType type, Effect.Target target) {
		int outcard = -1;
		for (int i = 0; i < cards.length; i++) {
			int effect = hasEffect(cards[i].getEffects(), type, target);

			if (effect >= 0 && cards[i].getCost() <= computer.getResources()) {
				if (outcard < 0) {
					outcard = i;
				} else {
					if (cards[i].getCost() > cards[outcard].getCost()) {
						outcard = i;
					}
				}
			}
		}
		return outcard;
	}

	private int hasEffect(List<Effect> effects, Effect.EffectType type,
			Effect.Target target) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).getType() == type
					&& effects.get(i).getTarget() == target) {
				return i;
			}
		}
		return -1;
	}

}
