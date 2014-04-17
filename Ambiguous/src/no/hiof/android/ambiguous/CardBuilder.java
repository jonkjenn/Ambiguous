package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;

/**
 * For simplifying building different version of the same basic card.
 */
public class CardBuilder {
	
	/**
	 * Builds a armor buffing card.
	 * @param name
	 * @param description
	 * @param image
	 * @param cost
	 * @param amount
	 * @return
	 */
	public static Card SelfArmor(String name,String description, String image, int cost, int amount)
	{
		Card c = new Card(name);
		c.description = description;
		c.cost = cost;
		c.image = image;
		
		Effect e2 = new Effect();
		e2.type =  Effect.EffectType.ARMOR;
		e2.target = Effect.Target.SELF;
		e2.minValue = amount;
		
		c.effects.add(e2);
		
		return c;
	}

	/**
	 * Builds a healing card.
	 * @param name
	 * @param description
	 * @param image
	 * @param cost
	 * @param min
	 * @param max
	 * @param crit
	 * @return
	 */
	public static Card SelfHeal(String name,String description, String image, int cost, int min, int max, int crit)
	{
		Card c = new Card(name);
		c.description = description;
		c.cost = cost;
		c.image = image;
		
		Effect e2 = new Effect();
		e2.type =  Effect.EffectType.HEALTH;
		e2.target = Effect.Target.SELF;
		e2.minValue = min;
		e2.maxValue = max;
		e2.crit = crit;
		
		c.effects.add(e2);
		
		return c;
	}

	/**
	 * Build a resource giving card.
	 * @param name
	 * @param description
	 * @param image
	 * @param cost
	 * @param amount
	 * @return
	 */
	public static Card AddResources(String name,String description, String image, int cost, int amount)
	{
		Card c = new Card(name);
		c.description = description;
		c.cost = cost;
		c.image = image;
		
		Effect e2 = new Effect();
		e2.type =  Effect.EffectType.RESOURCE;
		e2.target = Effect.Target.SELF;
		e2.minValue = amount;
		
		c.effects.add(e2);
		
		return c;
	}

	/**
	 * Build a damagedealing card.
	 * @param name
	 * @param description
	 * @param image
	 * @param cost
	 * @param min
	 * @param max
	 * @param crit
	 * @return
	 */
	public static Card DamageOponent(String name,String description, String image, int cost, int min,int max, int crit)
	{
		Card c = new Card(name);
		c.description = description;
		c.cost = cost;
		c.image = image;
		
		Effect e2 = new Effect();
		e2.type =  Effect.EffectType.DAMAGE;
		e2.target = Effect.Target.OPPONENT;
		e2.minValue = min;
		e2.maxValue = max;
		e2.crit = crit;
		
		c.effects.add(e2);
		
		return c;
	}
}
