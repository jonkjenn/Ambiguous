package no.hiof.android.ambiguous.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hiof.android.ambiguous.model.Card;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * For getting cards from and adding cards to the database.
 */
public class CardDataSource {

	private static final String SELECT_CARDS = "SELECT * FROM Card";
	private static final String SELECT_CARDS_ID = "SELECT * FROM Card WHERE id = ?";

	private EffectDataSource effectDs;
	private SQLiteDatabase db;

	private static List<Card> cards;
	private static Map<Integer, Card> cardMap;

	public CardDataSource(SQLiteDatabase db) {
		this.db = db;
		this.effectDs = new EffectDataSource(db);
	}
	
	public void loadData()
	{
		new AsyncTask<CardDataSource, Void, List<Card>>() {

			@Override
			protected List<Card> doInBackground(CardDataSource... params) {
				if (params.length > 0) {
					List<Card> cards = params[0].getCards();
					return cards;
				} else {

					return null;
				}
			}

			@Override
			protected void onPostExecute(List<Card> result) {
				super.onPostExecute(result);
				CardDataSource.this.cardsLoaded(result);
			}
		}.execute(this);

	}

	void cardsLoaded(List<Card> cards) {
		CardDataSource.cards = cards;

		if (cards != null) {
			cardMap = new HashMap<Integer, Card>();

			for (int i = 0; i < cards.size(); i++) {
				cardMap.put(cards.get(i).id, cards.get(i));
			}
			
			notifyOnLoadComplete();
		}
		else
		{
			//TODO: Display error msg
		}
	}

	public void addCard(Card card) {
		ContentValues cv = getCardContentValues(card);

		long id = db.insert("Card", null, cv);
		if (id > 0) {
			for (int i = 0; i < card.effects.size(); i++) {
				db.insert(
						"Effect",
						null,
						this.effectDs.getEffectContentValues(
								card.effects.get(i), id));
			}
		}
	}

	/**
	 * Convert Card to ContentValues.
	 * 
	 * @param c
	 *            The Card to convert.
	 * @return
	 */
	private ContentValues getCardContentValues(Card c) {
		ContentValues cv = new ContentValues();
		cv.put("name", c.name);
		cv.put("description", c.description);
		cv.put("cost", c.cost);
		cv.put("image", c.image);

		return cv;
	}

	/**
	 * For getting a list of each unique Card. The getCards function takes -1 as
	 * a parameter to return all cards.
	 * 
	 * @return List of all the different cards.
	 */
	public List<Card> getCards() {
		if(cards != null)
		{
			return cards;
		}
		return getCards(-1);
	}

	/**
	 * 
	 * @param id
	 *            Id of the Card.
	 * @return The Card with the specified id or null if does not exist.
	 */
	public static Card getCard(int id) {
		return cardMap.get(id);
	}

	public void updateCard(Card card) {
		ContentValues cv = getCardContentValues(card);

		db.update("Card", cv, "id = ?",
				new String[] { Integer.toString(card.id) });
	}

	/**
	 * 
	 * @param id
	 *            The id of a specific card, if id < 0 return all cards.
	 * @return A list of a specific card or a list of all cards.
	 */
	private List<Card> getCards(int id) {
		List<Card> cards = new ArrayList<Card>();

		Cursor c;
		if (id < 0) {
			c = db.rawQuery(SELECT_CARDS, null);
		} else {
			c = db.rawQuery(SELECT_CARDS_ID,
					new String[] { Integer.toString(id) });
		}
		c.moveToFirst();

		// Build list of cards
		while (!c.isAfterLast()) {
			cards.add(cardFromCursor(c));

			c.moveToNext();
		}
		c.close();

		// Add effects to the cards
		for (int i = 0; i < cards.size(); i++) {
			Card card = cards.get(i);
			card.effects = (this.effectDs.getEffects(card));
		}

		return cards;
	}

	/**
	 * Builds a Card from a cursor.
	 * 
	 * @param c
	 * @return
	 */
	private Card cardFromCursor(Cursor c) {
		Card card = new Card(c.getString(1));
		card.id = (c.getInt(c.getColumnIndex("id")));
		card.description = (c.getString(c.getColumnIndex("description")));
		card.image = (c.getString(c.getColumnIndex("image")));
		card.cost = (c.getInt(c.getColumnIndex("cost")));
		return card;
	}

	public void purge() {
		cards = null;
		cardMap = null;
	}
	
	public interface OnLoadCompleteListener
	{
		void onLoadComplete();
	}
	
	OnLoadCompleteListener onLoadCompleteListener;
	
	public void setOnLoadCompleteListener(OnLoadCompleteListener l)
	{
		this.onLoadCompleteListener = l;
	}
	
	void notifyOnLoadComplete()
	{
		if(this.onLoadCompleteListener != null)
		{
			this.onLoadCompleteListener.onLoadComplete();
		}
	}

}