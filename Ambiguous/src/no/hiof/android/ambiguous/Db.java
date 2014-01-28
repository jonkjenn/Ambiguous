package no.hiof.android.ambiguous;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Db extends SQLiteOpenHelper {
	
	private static final String CREATE_CARD_TABLE = "CREATE TABLE Card (id INTEGER PRIMARY KEY, name TEXT, description TEXT, cost INTEGER, image TEXT)";
	private static final String CREATE_EFFECT_TABLE = "CREATE TABLE Effect (id INTEGER PRIMARY KEY, type VARCHAR(10), target VARCHAR(10), minvalue INTEGER, maxvalue INTEGER, crit INTEGER, card_id INTEGER REFERENCES Card(id))";
	private static final String SELECT_CARDS = "SELECT * FROM Card";
	private static final String SELECT_CARDS_ID = "SELECT * FROM Card WHERE id = ?";
	private static final String SELECT_EFFECTS = "SELECT * FROM Effect WHERE card_id = ?";
	private static final String UPDATE_CARD = "UPDATE Card SET name = ?, description = ?, image = ? WHERE id = ?";
	
	private static final String name = "db";	
	
	private static final String DROP_CARD_TABLE = "DROP TABLE IF EXISTS Card";
	private static final String DROP_EFFECT_TABLE = "DROP TABLE IF EXISTS Effect";
	
	private static Db db;

	public static Db getDb(Context ctx)
	{
		if(db == null)
		{
			db = new Db(ctx,name,null,1);
		}
		
		return db;
	}
	
	private Db(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	public void dropTables()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(DROP_CARD_TABLE);
		db.execSQL(DROP_EFFECT_TABLE);
		db.close();
	}
	
	public void addCard(Card card)
	{
		ContentValues cv = getCardContentValues(card);
		
		SQLiteDatabase db = getWritableDatabase();
		
		long id = db.insert("Card", null, cv);
		if(id>0)
		{
			for(int i=0;i<card.getEffects().size();i++)
			{
				db.insert("Effect", null, getEffectContentValues(card.getEffects().get(i),id));
			}
		}
		db.close();
	}
	
	private ContentValues getEffectContentValues(Effect e, long id)
	{
		ContentValues v = new ContentValues();
        v.put("card_id", id);
        v.put("type", e.getType().toString());
        v.put("target", e.getTarget().toString());
        v.put("minvalue", e.getMinValue());
        v.put("maxvalue",e.getMaxValue());
        v.put("crit", e.getCrit());
        return v;
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
	
	public void SaveCard(Card card)
	{
		SQLiteDatabase db = getWritableDatabase();		
		ContentValues cv = getCardContentValues(card);

		int i = db.update("Card", cv, "id = ?", new String[]{Integer.toString(card.getId())});
		db.close();
	}
	
	//id<0 returns all cards
	private List<Card> getCards(int id)
	{
		List<Card> cards = new ArrayList<Card>();
		
		SQLiteDatabase db = getReadableDatabase();

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
		c.close();
		db.close();
		
		//Add effects to the cards
		for(int i=0;i<cards.size();i++)
		{
			Card card = cards.get(i);
			card.setEffects(getEffects(card));
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
	
	private List<Effect> getEffects(Card card)
	{
		List<Effect> effects = new ArrayList<Effect>();
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery(SELECT_EFFECTS, new String[] {Integer.toString(card.getId())});
		c.moveToFirst();
		while(!c.isAfterLast())
		{
			Effect e = new Effect()
			.setId(c.getInt(c.getColumnIndex("id")))
			.setType(Effect.EffectType.valueOf(c.getString(c.getColumnIndex("type"))))
			.setTarget(Effect.Target.valueOf(c.getString(c.getColumnIndex("target"))))
			.setMinValue(c.getInt(c.getColumnIndex("minvalue")))
			.setMaxValue(c.getInt(c.getColumnIndex("maxvalue")))
			.setCrit(c.getInt(c.getColumnIndex("crit")));
			effects.add(e);
			c.moveToNext();
		}
		db.close();
		
		return effects;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables();
	}
	
	public void createTables()
	{
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(CREATE_CARD_TABLE);
		db.execSQL(CREATE_EFFECT_TABLE);

		Effect effect = new Effect()
		.setType(Effect.EffectType.HEALTH)
		.setTarget(Effect.Target.OPPONENT)
		.setMinValue(2)
		.setMaxValue(5)
		.setCrit(2);

		List<Effect> effects = Arrays.asList(effect);

		Card c = new Card("Sword of Ambiguity")
		.setDescription("Amusing description of the ambiguous characteristics of this sword.")
		.setCost(2)
		.setImage("sword_of_ambiguity")
		.setEffects(effects);
		
		addCard(c);
		db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
