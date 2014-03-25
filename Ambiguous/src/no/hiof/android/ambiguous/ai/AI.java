package no.hiof.android.ambiguous.ai;

import java.util.List;

import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Player;

/**
 * Generates suggestions for which cards the computer should use.
 */

public class AI {
	private Player computer;
	private Player player;
	private Card[] cards;
	
	private int resource;
	private int damage;
	private int armor;
	private int heal;
	private int moredamage;

	public AI(Player computer, Player player) {
		this.player = player;
		this.computer = computer;

		this.cards = computer.getHand();
		
		findUseableEffects();
	}
	
	/**
	 * Finds the most effective card of each type we are able to cast with current resource.
	 */
	private void findUseableEffects()
	{
        resource = hasEffect(Effect.EffectType.RESOURCE, Effect.Target.SELF);
        damage =hasEffect(Effect.EffectType.DAMAGE,Effect.Target.OPPONENT); 
        armor =hasEffect(Effect.EffectType.ARMOR,Effect.Target.SELF);
		heal = hasEffect(Effect.EffectType.HEALTH,Effect.Target.SELF);
        moredamage = hasDamageMoreThenPlayer();
	}

	/**
	 * Decides if computer will prefer aggressive choices or defensive choices.
	 * 
	 * @return The suggested card id to use. Or -1 if could not find any usable card.
	 */
	public int Start()
	{
		if(computer.getHealth() + computer.getArmor() > 100)
		{
			return playAggressive();
			
		}
		else if(computer.getHealth() + computer.getArmor() <= 100)
		{
			return playDefensive();
		}
		
		return -1;
	}
	
	/**
	 * Find aggressive card choice.
	 * 
	 * @return Card id or -1 if none.
	 */
	private int playAggressive()
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
			return -1;
	}
	
	/**
	 * Find defensive card choice.
	 * @return Card id or -1 if none.
	 */
	private int playDefensive()
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
			return -1;
	}
	
	/**
	 * Tries to find a card that on average does more damage then opponents current health.
	 * 
	 * @return Card id
	 */
	private int hasDamageMoreThenPlayer()
	{
		return hasminEffect(player.getHealth() + player.getArmor(),Effect.EffectType.DAMAGE,Effect.Target.OPPONENT)[0];
	}
	
	/**
	 * Tries to find a heal card that on average heals for less then computer missing health.
	 * For preventing healing above max health.
	 * 
	 * @return An int array where position 0 is card id and position 1 is average heal amount.
	 */
	private int[] hasHealLessMaxHP() {
		return hasEffect(computer.maxHealth - computer.getHealth(),
				Effect.EffectType.HEALTH, Effect.Target.SELF);
	}

	/**
	 * Tries to find a armor card that on average will give total armor below max armor limit.
	 * 
	 * @return An int array where position 0 is card id and position 1 is average armor amount.
	 */
	private int[] hasArmorLessMaxArmor() {
		return hasEffect(computer.maxArmor - computer.getArmor(),
				Effect.EffectType.ARMOR, Effect.Target.SELF);
	}


	/**
	 * Tries to find a card that on average does the minimum amount of the effect type we want.
	 * 
	 * @param min_amount The minimum effect amount we need from the effect.
	 * @param type The effect type.
	 * @param target The effect target.
	 * @return An int array where position 0 is card id and position 1 is average effect amount.
	 */
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
	

	/**
	 * 
	 * Tries to find a card that on average does the most effect amount and the user has enough resources to use.
	 * 
	 * @param min_amount The effect amount we want from the effect.
	 * @param type The effect type.
	 * @param target The effect target.
	 * @return An int array where position 0 is card id and position 1 is average effect amount.
	 */
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

	/**
	 * Calculates the average effect amount.
	 * @param effect
	 * @return Average effect amount.
	 */
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

	/**
	 * Tries to find a card of the specified type which we have enough resources to use.
	 * 
	 * @param type Effect type.
	 * @param target Effect target.
	 * @return Card id or -1 if none.
	 */
	private int hasEffect(Effect.EffectType type, Effect.Target target) {
		int outcard = -1;
		for (int i = 0; i < cards.length; i++) {
			if(cards[i]==null){continue;}
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

	/**
	 * Tries to find a card with the specified effect types which we have enough resources to use.
	 * 
	 * @param type Effect type.
	 * @param target Effect target.
	 * @return Card id or -1 if none.
	 */
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
