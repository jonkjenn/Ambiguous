package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
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

		
		CardDataSource cs = new CardDataSource(db);

		Card c = CardBuilder.DamageOponent("Sword1", "Amusing description of the ambiguous characteristics of this sword.", "sword_of_ambiguity", 3, 10, 30, 10);
		Card c2 = CardBuilder.SelfArmor("Armor1", "Gives small amount of armor", "sword_of_ambiguity", 2, 5);
		Card c3 = CardBuilder.SelfHeal("Heal1", "Heals", "sword_of_ambiguity", 2, 5,10,2);
		Card c4 = CardBuilder.AddResources("Resource1", "Gives some resources", "sword_of_ambiguity", 2, 4);
		
		cs.addCard(c);
		cs.addCard(c2);
		cs.addCard(c3);
		cs.addCard(c4);
		cs.addCard(CardBuilder.DamageOponent("Sword2", "", "sword_of_ambiguity", 25, 30, 50, 10));
		cs.addCard(CardBuilder.DamageOponent("Sword3", "", "sword_of_ambiguity", 5, 5, 10, 5));
		cs.addCard(CardBuilder.SelfHeal("Heal2", "", "sword_of_ambiguity", 10, 30, 50, 15));
		cs.addCard(CardBuilder.SelfHeal("Heal3", "", "sword_of_ambiguity", 7, 5, 50, 15));
		cs.addCard(CardBuilder.SelfArmor("Armor2", "Gives small amount of armor", "sword_of_ambiguity", 5, 10));
		cs.addCard(CardBuilder.DamageOponent("Sword4", "Amusing description of the ambiguous characteristics of this sword.", "sword_of_ambiguity", 5, 10, 20, 10));
		cs.addCard(CardBuilder.AddResources("Resource2", "Gives more resources", "smiley_drawing_small", 1, 20));
		cs.addCard(CardBuilder.SelfArmor("Armor3", "Gives a fair amount of armor", "smiley_drawing", 4, 20));
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
