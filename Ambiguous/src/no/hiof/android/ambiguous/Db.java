package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.datasource.CardDataSource;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles setting up the database and inserting the cards to use in game.
 */
public class Db extends SQLiteOpenHelper {
	
	public static final String TABLE_NAME_CARDLISTTYPE = "Cardlisttype";
	public static final String CARDLISTTYPE_DECK = "deck";
	public static final String CARDLISTTYPE_HAND = "hand";
	
	private static final String CREATE_CARDLISTTYPE_TABLE = 
			"CREATE TABLE IF NOT EXISTS `"+TABLE_NAME_CARDLISTTYPE+"` (" +
			"`name` VARCHAR(45)," +
			"PRIMARY KEY (`name`))";
	
	private static final String CREATE_PLAYERCARDLIST_TABLE = 
			"CREATE TABLE IF NOT EXISTS `Playercardlist` (" +
//			"`id` INTEGER NOT NULL, " +
			"`type` VARCHAR(45) NOT NULL, " +
			"`player` INTEGER NOT NULL, " +
			"PRIMARY KEY (`type`, `player`), " +
			"FOREIGN KEY (`type`) REFERENCES `"+TABLE_NAME_CARDLISTTYPE+"` (`name`) )";
	
	private static final String CREATE_PLAYERPROFILE_TABLE = 
			"CREATE TABLE IF NOT EXISTS `PlayerProfile` (" +
			"`name` VARCHAR(45) PRIMARY KEY, " +
			"`id` INTEGER NOT NULL UNIQUE) ";
	
	private static final String CREATE_PLAYER_TABLE = 
			"CREATE TABLE IF NOT EXISTS `Player` (" +
			"`name` VARCHAR(45) NOT NULL," +
			"`armor` INTEGER," +
			"`resources` INTEGER," +
			"`deckid` INTEGER," +
			"`handid` INTEGER," +
			"`id` INTEGER," +
			"PRIMARY KEY (`id`)," +
			"FOREIGN KEY (`deckid`) REFERENCES `Playercardlist` (`id`) , " +
			"FOREIGN KEY (`handid`) REFERENCES `Playercardlist` (`id`) , " +
			"FOREIGN KEY (`name`)   REFERENCES `PlayerProfile` (`name`) )";
	
	private static final String CREATE_SESSION_TABLE = 
			"CREATE TABLE IF NOT EXISTS `Session` (" +
			"`id` INTEGER PRIMARY KEY," +
			"`player` INTEGER," +
			"`opponent` INTEGER," +
			"`turn` INTEGER NOT NULL," +
			"`opponentCard` INTEGER," +
			"`opponentDiscard` BOOLEAN," +
			"FOREIGN KEY (`player`) REFERENCES `Player` (`id`)," +
			"FOREIGN KEY (`opponent`) REFERENCES `Player` (`id`)," +
			"FOREIGN KEY (`opponentCard`) REFERENCES `Card` (`id`) )";
	
	private static final String CREATE_CARD_TABLE = 
			"CREATE TABLE IF NOT EXISTS `Card` (" +
			"`id` INTEGER PRIMARY KEY," +
			"`name` TEXT," +
			"`description` TEXT," +
			"`cost` INTEGER," +
			"`image` TEXT )";
	
	private static final String CREATE_EFFECT_TABLE = 
			"CREATE TABLE IF NOT EXISTS `Effect` (" +
			"`id` INTEGER PRIMARY KEY," +
			"`type` VARCHAR(10)," +
			"`target` VARCHAR(10)," +
			"`minvalue` INTEGER," +
			"`maxvalue` INTEGER," +
			"`crit` INTEGER," +
			"`card_id` INTEGER," +
			"FOREIGN KEY (`card_id`) REFERENCES `Card` (`id`) )";
	
	private static final String CREATE_CONNECTION_TABLE = 
			"CREATE TABLE IF NOT EXISTS `Connection` (" +
			"`id` INTEGER," +
			"`ip` VARCHAR(15)," +
			"PRIMARY KEY (`id`) )";
	
	private static final String CREATE_PLAYERCARD_TABLE = 
			"CREATE TABLE IF NOT EXISTS `Playercard` (" +
			"`cardid` INTEGER NOT NULL," +
			"`sessioncardlistid` INTEGER NOT NULL," +
			"`position` INTEGER NOT NULL," +
			"PRIMARY KEY (`cardid`, `sessioncardlistid`, `position`)," +
			"FOREIGN KEY (`sessioncardlistid`) REFERENCES `Playercardlist` (`id`)," +
			"FOREIGN KEY (`cardid`) REFERENCES `Card` (`id`) )";
	
	private static final String name = "db";
	
	private static final String DROP_CARDLISTTYPE_TABLE = "DROP TABLE IF EXISTS "+TABLE_NAME_CARDLISTTYPE;
	private static final String DROP_PLAYERCARDLIST_TABLE = "DROP TABLE IF EXISTS Playercardlist";
	private static final String DROP_PLAYERPROFILE_TABLE = "DROP TABLE IF EXISTS PlayerProfile";
	private static final String DROP_PLAYER_TABLE = "DROP TABLE IF EXISTS Player";
	private static final String DROP_SESSION_TABLE = "DROP TABLE IF EXISTS Session";
	private static final String DROP_CARD_TABLE = "DROP TABLE IF EXISTS Card";
	private static final String DROP_EFFECT_TABLE = "DROP TABLE IF EXISTS Effect";
	private static final String DROP_CONNECTION_TABLE = "DROP TABLE IF EXISTS Connection";
	private static final String DROP_PLAYERCARD_TABLE = "DROP TABLE IF EXISTS Playercard";
	
	private static Db db;

	/**
	 * Return the current database or initiate a new one if a database does not exist.
	 * @param ctx
	 * @return
	 */
	public static Db getDb(Context ctx)
	{
		if(db == null)
		{
			db = new Db(ctx,name,null,5);
				
		}
		
		return db;
	}

	private Db(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	private void dropTables(SQLiteDatabase db)
	{
		db.execSQL(DROP_PLAYERCARD_TABLE);
		db.execSQL(DROP_CONNECTION_TABLE);
		db.execSQL(DROP_EFFECT_TABLE);
		db.execSQL(DROP_CARD_TABLE);
		db.execSQL(DROP_SESSION_TABLE);
		db.execSQL(DROP_PLAYER_TABLE);
		db.execSQL(DROP_PLAYERPROFILE_TABLE);
		db.execSQL(DROP_PLAYERCARDLIST_TABLE);
		db.execSQL(DROP_CARDLISTTYPE_TABLE);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
		insertData(db);
	}
	
	
	/**
	 * Create all the tables.
	 * @param db
	 */
	public void createTables(SQLiteDatabase db)
	{
		db.execSQL(CREATE_CARDLISTTYPE_TABLE);
		db.execSQL(CREATE_PLAYERCARDLIST_TABLE);
		db.execSQL(CREATE_PLAYERPROFILE_TABLE);
		db.execSQL(CREATE_PLAYER_TABLE);
		db.execSQL(CREATE_SESSION_TABLE);
		db.execSQL(CREATE_CARD_TABLE);
		db.execSQL(CREATE_EFFECT_TABLE);
		db.execSQL(CREATE_CONNECTION_TABLE);
		db.execSQL(CREATE_PLAYERCARD_TABLE);
	}

	/**
	 * Insert all the different cards available in the game.
	 * @param db
	 */
	private void insertData(SQLiteDatabase db)
	{
		//ContentValues contentValues = new ContentValues();
		db.execSQL("INSERT INTO "+TABLE_NAME_CARDLISTTYPE+" (name) VALUES('"+CARDLISTTYPE_DECK+"')");
		db.execSQL("INSERT INTO "+TABLE_NAME_CARDLISTTYPE+" (name) VALUES('"+CARDLISTTYPE_HAND+"')");
		CardDataSource cs = new CardDataSource(db);

		cs.addCard(CardBuilder.DamageOponent("Pistol","","pistol1",4,5,10,5));
		cs.addCard(CardBuilder.DamageOponent("Pistol2","","pistol3",7,10,20,10));
		cs.addCard(CardBuilder.DamageOponent("Pistol3","","pistol6",10,10,27,30));
		cs.addCard(CardBuilder.DamageOponent("Rifle","","rifle3",15,25,40,30));
		cs.addCard(CardBuilder.DamageOponent("Rifle2","","rifle4",20,30,60,40));
		cs.addCard(CardBuilder.DamageOponent("Shotgun","","shotgun1",6,1,25,5));
		cs.addCard(CardBuilder.DamageOponent("Shotgun2","","shotgun2",10,1,45,5));
		cs.addCard(CardBuilder.DamageOponent("Shotgun3","","shotgun4",15,10,60,10));
		cs.addCard(CardBuilder.DamageOponent("Shotgun4","","shotgun6",20,25,65,30));
		cs.addCard(CardBuilder.DamageOponent("Sword","","sword_of_ambiguity",10,10,30,10));
		cs.addCard(CardBuilder.DamageOponent("Minigame!","","smiley_drawing_small",10,10,100,0));
		cs.addCard(CardBuilder.SelfHeal("Heal1","","plus_drawing",4,10,15,5));
		cs.addCard(CardBuilder.SelfHeal("Heal2","","plus_drawing",10,15,50,15));
		cs.addCard(CardBuilder.SelfHeal("Heal3","","plus_drawing",15,35,60,30));
		cs.addCard(CardBuilder.SelfHeal("Heal4","","plus_drawing",20,50,100,0));
		cs.addCard(CardBuilder.SelfArmor("Armor","","kevlar_drawing",4,15));
		cs.addCard(CardBuilder.SelfArmor("Armo2","","kevlar_drawing",10,40));
		cs.addCard(CardBuilder.SelfArmor("Armor3","","kevlar_drawing",15,65));
		cs.addCard(CardBuilder.SelfArmor("Armor4","","kevlar_drawing",20,90));

		cs.addCard(CardBuilder.AddResources("Resource2", "Gives more resources", "smiley_drawing_small", 5, 30));
		cs.addCard(CardBuilder.AddResources("Resource1", "Gives more resources", "smiley_drawing_small", 2, 10));
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		dropTables(db);
		createTables(db);
		insertData(db);
	}

}
