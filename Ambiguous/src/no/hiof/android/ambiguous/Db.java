package no.hiof.android.ambiguous;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class Db extends SQLiteOpenHelper {
	
	private static final String CREATE_CARD_TABLE = "CREATE TABLE Card (id INTEGER PRIMARY KEY, name TEXT, description TEXT, cost INTEGER, image TEXT)";
	private static final String CREATE_EFFECT_TABLE = "CREATE TABLE Effect (id INTEGER PRIMARY KEY, type VARCHAR(10), target VARCHAR(10), minvalue INTEGER, maxvalue INTEGER, crit INTEGER, card_id INTEGER REFERENCES Card(id))";
	
	private static final String name = "db";	
	
	private static final String DROP_CARD_TABLE = "DROP TABLE IF EXISTS Card";
	private static final String DROP_EFFECT_TABLE = "DROP TABLE IF EXISTS Effect";
	
	private static Db db;

	public static Db getDb(Context ctx)
	{
		if(db == null)
		{
			db = new Db(ctx,name,null,1);
			db.dropTables();
			db.createTables();
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
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}
	
	public void createTables()
	{
		SQLiteDatabase db = getWritableDatabase();
		createTables(db);		
	}
	
	public void createTables(SQLiteDatabase db)
	{
		db.execSQL(CREATE_CARD_TABLE);
		db.execSQL(CREATE_EFFECT_TABLE);

		Effect effect = new Effect()
		.setType(Effect.EffectType.HEALTH)
		.setTarget(Effect.Target.OPPONENT)
		.setMinValue(-2)
		.setMaxValue(-5)
		.setCrit(2);

		List<Effect> effects = Arrays.asList(effect);

		Card c = new Card("Sword of Ambiguity")
		.setDescription("Amusing description of the ambiguous characteristics of this sword.")
		.setCost(2)
		.setImage("sword_of_ambiguity")
		.setEffects(effects);
		
		CardDataSource cs = new CardDataSource(db);
		cs.addCard(c);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
