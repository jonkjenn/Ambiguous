package no.hiof.android.ambiguous.model;

import java.util.List;

public class Player {
	private int id;
	private String name;
	private List<Card> deck;
	private Card[] cards;

	public Player(String name)
	{
		this.name = name;
		cards = new Card[8];
	}

	public void PullCards()
	{
		for(int i=0;i<8;i++)
		{
			if(cards[i] == null)
			{
				cards[i] = deck.remove(0);
			}
		}
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
}
