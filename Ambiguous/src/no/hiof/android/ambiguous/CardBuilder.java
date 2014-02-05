package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;

public class CardBuilder {
	
	public static Card SelfArmor(String name,String description, String image, int cost, int amount)
	{
		Card c = new Card(name)
		.setDescription(description)
		.setCost(cost)
		.setImage(image);
		
		Effect e2 = new Effect()
		.setType(Effect.EffectType.ARMOR)
		.setTarget(Effect.Target.SELF)
		.setMinValue(amount);
		
		c.getEffects().add(e2);
		
		return c;
	}

	public static Card SelfHeal(String name,String description, String image, int cost, int min, int max, int crit)
	{
		Card c = new Card(name)
		.setDescription(description)
		.setCost(cost)
		.setImage(image);
		
		Effect e2 = new Effect()
		.setType(Effect.EffectType.HEALTH)
		.setTarget(Effect.Target.SELF)
		.setMinValue(min)
		.setMaxValue(max)
		.setCrit(crit);
		
		c.getEffects().add(e2);
		
		return c;
	}

	public static Card AddResources(String name,String description, String image, int cost, int amount)
	{
		Card c = new Card(name)
		.setDescription(description)
		.setCost(cost)
		.setImage(image);
		
		Effect e2 = new Effect()
		.setType(Effect.EffectType.RESOURCE)
		.setTarget(Effect.Target.SELF)
		.setMinValue(amount);
		
		c.getEffects().add(e2);
		
		return c;
	}

	public static Card DamageOponent(String name,String description, String image, int cost, int min,int max, int crit)
	{
		Card c = new Card(name)
		.setDescription(description)
		.setCost(cost)
		.setImage(image);
		
		Effect e2 = new Effect()
		.setType(Effect.EffectType.DAMAGE)
		.setTarget(Effect.Target.OPPONENT)
		.setMinValue(min)
		.setMaxValue(max)
		.setCrit(crit);
		
		c.getEffects().add(e2);
		
		return c;
	}

}
