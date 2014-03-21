package no.hiof.android.ambiguous;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.model.Card;

/**
 * Builds the deck the local players will use.
 */
public class DeckBuilder {
	private static List<Card> cards;
	private static List<Card> deck;
	
	/**
	 * Builds and returns a randomized deck.
	 * @param cards
	 * @return
	 */
	public static List<Card> StandardDeck(List<Card> cards)
	{
		DeckBuilder.cards = cards;
        DeckBuilder.deck = new ArrayList<Card>();

        Random rand = new Random();
        addCard("Pistol",100);
        addCard("Pistol2",50);
        addCard("Pistol3",20);
        addCard("Rifle",30);
        addCard("Rifle2",30);
        addCard("Shotgun",70);
        addCard("Shotgun2",60);
        addCard("Shotgun3",40);
        addCard("Shotgun4",30);
        addCard("Sword",70);
        addCard("Heal1",70);
        addCard("Heal2",50);
        addCard("Heal3",30);
        addCard("Heal4",20);
        addCard("Armor",70);
        addCard("Armo2",60);
        addCard("Armor3",30);
        addCard("Armor4",10);

        addCard("Resource1",100);
        addCard("Resource2",50);
        
        Card temp;
        for(int i=0;i<deck.size();i++)
        {
                int r = (int)(rand.nextFloat()*(deck.size()-1));
                temp = deck.get(r);
                deck.set(r,deck.get(i));
                deck.set(i, temp);
        }
        
        deck.set(0,cardFromName("Minigame!"));

        return DeckBuilder.deck;
	}
	
	/**
	 * Add many instances of the same card to the deck.
	 * @param name Name of the card.
	 * @param count How many instances of the card to add.
	 */
	private static void addCard(String name, int count)
	{
		Card card = cardFromName(name);
		if(card == null){return;}
		for(int i=0;i<count;i++)
		{
			deck.add(card);
		}
	}
	
	/**
	 * @param name Name of the card.
	 * @return A card with the wanted name or null if none found.
	 */
	private static Card cardFromName(String name)
	{
		for(int i=0;i<cards.size();i++)
		{
			if(cards.get(i).getName().equals(name)){return cards.get(i);}
		}
		return null;
	}
}
