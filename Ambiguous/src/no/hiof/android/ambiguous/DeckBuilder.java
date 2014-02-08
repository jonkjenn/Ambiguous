package no.hiof.android.ambiguous;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.model.Card;

public class DeckBuilder {
	public static List<Card> StandardDeck(List<Card> cards)
	{
			List<Card> deck = new ArrayList<Card>();

			Random rand = new Random();
			for(int i=0;i<cards.size();i++)
			{
				deck.add(cards.get(i));
			}

			Card temp;
			for(int i=0;i<deck.size();i++)
			{
				int r = (int)(rand.nextFloat()*(deck.size()-1));
				temp = deck.get(r);
				deck.set(r,deck.get(i));
				deck.set(i, temp);
			}
		
			return deck;
	}
}
