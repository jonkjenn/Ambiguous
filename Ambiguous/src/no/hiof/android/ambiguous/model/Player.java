package no.hiof.android.ambiguous.model;

import java.util.List;
import java.util.Random;

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
	private Random deckRandom;

	public Player(String name)
	{
		this.name = name;
		cards = new Card[8];
	}

	public void PullCards()
	{
		if(deckRandom == null){deckRandom = new Random();}
		for(int i=0;i<cards.length;i++)
		{
			if(cards[i] == null)
			{
				cards[i] = deck.get(deckRandom.nextInt(deck.size()-1));
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
		if(this.armor>=amount)
		{
			this.armor -= amount;
			amount = 0;
		}
		else
		{
			amount -= this.armor;
			this.armor = 0;
		}

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
	
	public int getHealth()
	{
		return this.health;
	}
	
	public int getArmor()
	{
		return this.armor;
	}
	
	public int getResources()
	{
		return this.resources;
	}
	
}
