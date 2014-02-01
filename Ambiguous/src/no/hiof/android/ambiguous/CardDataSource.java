package no.hiof.android.ambiguous;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CardDataSource {

	private static final String SELECT_CARDS = "SELECT * FROM Card";
	private static final String SELECT_CARDS_ID = "SELECT * FROM Card WHERE id = ?";
	private static final String UPDATE_CARD = "UPDATE Card SET name = ?, description = ?, image = ? WHERE id = ?";

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

	private ContentValues getCardContentValues(Card c)
	{
		ContentValues cv = new ContentValues();
		cv.put("name", c.getName());
		cv.put("description", c.getDescription());
		cv.put("cost", c.getCost());
		cv.put("image", c.getImage());
		
		return cv;
	}

	public List<Card> getCards()
	{
		return getCards(-1);
	} 
	
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

		int i = db.update("Card", cv, "id = ?", new String[]{Integer.toString(card.getId())});
	}
	
	//id<0 returns all cards
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
	
	private Card cardFromCursor(Cursor c)
	{
			return new Card(c.getString(1))
			.setId(c.getInt(c.getColumnIndex("id")))
			.setDescription(c.getString(c.getColumnIndex("description")))
			.setImage(c.getString(c.getColumnIndex("image")))
			.setCost(c.getInt(c.getColumnIndex("cost")));
	}

}
