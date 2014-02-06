package no.hiof.android.ambiguous.model;

import java.util.List;

public class Player {
	private int id;
	private String name;
	private List<Card> deck;
	private Card[] cards;
	private final int maxHealth = 100;
	private final int maxArmor = 100;
	private int health = maxHealth;
	private int armor = 0;
	private int resources = 10;
	private boolean alive = true;

	public Player(String name)
	{
		this.name = name;
		cards = new Card[8];
	}

	public void PullCards()
	{
		if(deck.size()<=0){return;}
		for(int i=0;i<8;i++)
		{
			if(cards[i] == null)
			{
				cards[i] = deck.remove(0);
			}
		}
	}
	
	public void CardUsed(int position)
	{
		cards[position] = null;
		PullCards();
	}
	
	public Player SetDeck(List<Card> deck)
	{
		this.deck = deck;
		this.PullCards();
		return this;
	}

	public List<Card> GetDeck()
	{
		return deck;
	}

	public Card[] GetCards()
	{
		return cards;
	}

	public void Damage(int amount)
	{
		this.health -= amount;
		if(this.health<0){this.alive = false;}
	}
	
	public void Heal(int amount)
	{
		this.health = (this.health + amount>this.maxHealth?100:this.health+amount);
	}

	public void ModArmor(int amount)
	{
		this.armor = (this.armor + amount>this.maxArmor?100:this.armor+amount);
	}

	public boolean getAlive()
	{
		return this.alive;
	}
	
	public boolean UseResources(int amount)
	{
		if(amount > this.resources){return false;}		
		this.resources -= amount;
		return true;
	}
	
	public void ModResource(int amount)
	{
		this.resources = (this.resources + amount < 0?0:this.resources+amount);
	}
	
	public String getStats()
	{
		return this.name + " Health: " + this.health + " Armor: " + this.armor + " Res: " + this.resources;
	}

}
