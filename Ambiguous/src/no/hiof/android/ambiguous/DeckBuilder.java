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
	
	
	public static List<Card> StandardDeck(){
		if(cards == null){
			return null;
		}
		return StandardDeck(cards);
	}
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
        addCard("Pistol",10);
        addCard("Pistol2",5);
        addCard("Pistol3",2);
        addCard("Rifle",3);
        addCard("Rifle2",3);
        addCard("Shotgun",7);
        addCard("Shotgun2",6);
        addCard("Shotgun3",4);
        addCard("Shotgun4",3);
        addCard("Sword",7);
        addCard("Heal1",7);
        addCard("Heal2",5);
        addCard("Heal3",3);
        addCard("Heal4",2);
        addCard("Armor",7);
        addCard("Armo2",6);
        addCard("Armor3",3);
        addCard("Armor4",1);

        addCard("Resource1",10);
        addCard("Resource2",5);
        
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
			if(cards.get(i).name.equals(name)){return cards.get(i);}
		}
		return null;
	}
}
