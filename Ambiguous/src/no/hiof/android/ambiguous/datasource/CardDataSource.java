package no.hiof.android.ambiguous.datasource;

import java.util.ArrayList;
import java.util.List;

import no.hiof.android.ambiguous.model.Card;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * For getting cards from and adding cards to the database.
 */
public class CardDataSource {

	private static final String SELECT_CARDS = "SELECT * FROM Card";
	private static final String SELECT_CARDS_ID = "SELECT * FROM Card WHERE id = ?";

	private EffectDataSource effectDs;
	private SQLiteDatabase db;

	public CardDataSource(SQLiteDatabase db)
	{
		this.db =db;
		this.effectDs = new EffectDataSource(db);
	}
	
	public void addCard(Card card)
	{
		ContentValues cv = getCardContentValues(card);
		
		long id = db.insert("Card", null, cv);
		if(id>0)
		{
			for(int i=0;i<card.getEffects().size();i++)
			{
				db.insert("Effect", null, this.effectDs.getEffectContentValues(card.getEffects().get(i),id));
			}
		}
	}

	/**
	 * Convert Card to ContentValues.
	 * @param c The Card to convert.
	 * @return
	 */
	private ContentValues getCardContentValues(Card c)
	{
		ContentValues cv = new ContentValues();
		cv.put("name", c.getName());
		cv.put("description", c.getDescription());
		cv.put("cost", c.getCost());
		cv.put("image", c.getImage());
		
		return cv;
	}

	/**
	 * For getting a list of each unique Card.
	 * The getCards function takes -1 as a parameter to return all cards.
	 * @return List of all the different cards.
	 */
	public List<Card> getCards()
	{
		return getCards(-1);
	} 
	
	/**
	 * 
	 * @param id Id of the Card.
	 * @return The Card with the specified id or null if does not exist.
	 */
	public Card getCard(int id)
	{
		try
		{
		return getCards(id).get(0);
		}catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	public void updateCard(Card card)
	{
		ContentValues cv = getCardContentValues(card);

		db.update("Card", cv, "id = ?", new String[]{Integer.toString(card.getId())});
	}
	
	/**
	 * 
	 * @param id The id of a specific card, if id < 0 return all cards.
	 * @return A list of a specific card or a list of all cards.
	 */
	private List<Card> getCards(int id)
	{
		List<Card> cards = new ArrayList<Card>();
		
		Cursor c;
		if(id<0){
			c = db.rawQuery(SELECT_CARDS, null);
		}else
		{
			c = db.rawQuery(SELECT_CARDS_ID, new String[]{Integer.toString(id)});
		}
		c.moveToFirst();
		
		//Build list of cards
		while(!c.isAfterLast())
		{
			cards.add(cardFromCursor(c));

			c.moveToNext();
		}
		
		//Add effects to the cards
		for(int i=0;i<cards.size();i++)
		{
			Card card = cards.get(i);
			card.setEffects(this.effectDs.getEffects(card));
		}
		
		return cards;
	}
	
	/**
	 * Builds a Card from a cursor.
	 * @param c
	 * @return
	 */
	private Card cardFromCursor(Cursor c)
	{
			return new Card(c.getString(1))
			.setId(c.getInt(c.getColumnIndex("id")))
			.setDescription(c.getString(c.getColumnIndex("description")))
			.setImage(c.getString(c.getColumnIndex("image")))
			.setCost(c.getInt(c.getColumnIndex("cost")));
	}

}
