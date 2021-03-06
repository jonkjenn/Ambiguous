package no.hiof.android.ambiguous.datasource;

import java.util.List;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Player;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SessionDataSource {

	private SQLiteDatabase db;
	private int sessionId = -1;
	
	public SessionDataSource(SQLiteDatabase db){
		this.db = db;
	}
	
	/**
	 * Get the id for the current session
	 * @return
	 */
	public int getSessionId() {
		return sessionId;
	}

	// This might not be necessary
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	
	/**
	 * Save the current game as it is to the database, preserving everything that is required to 
	 * continue the game exactly where it was paused. If the same session has been saved earlier,
	 * it will be overwritten and replaced by the new session with same id.
	 * @param turn
	 * @param player
	 * @param opponent
	 * @param opponentCardId
	 * @param cardWasDiscarded
	 * @return
	 */
	public boolean saveSession(int turn, Player player, Player opponent, int opponentCardId, boolean cardWasDiscarded, boolean cheatWasUsed){
		int playerId = savePlayer(player);
		int opponentId = savePlayer(opponent);
		
		if(playerId == -1 || opponentId == -1){
			return false;
		}
//		db.execSQL("INSERT INTO Session ('turn','player','opponent','opponentCard','opponentDiscard') " +
//					"VALUES('"+turn+"','"+playerId+"','"+opponentId+"','"+opponentCardId+"','"+cardWasDiscarded+"')");
		ContentValues cv = new ContentValues();
		cv.put("player", playerId);
		cv.put("opponent", opponentId);
		cv.put("turn", turn);
		cv.put("opponentCard", opponentCardId);
		cv.put("opponentDiscard", cardWasDiscarded);
		cv.put("cheatUsed", cheatWasUsed);
		
		// if sessionId is -1 a new session should be created, otherwise update the given session
		int workedFine;
		if(sessionId == -1)
			workedFine = (int)db.insert("Session", null, cv);
		else{
			cv.put("id", sessionId);
			db.replace("Session", null, cv);
			workedFine = sessionId;
		}
			
		return (workedFine == -1 ? false : true);
	}
	
	/**
	 * Saves the specified instance of Player in the db, returning it's id
	 * @param player
	 * @return
	 */
	private int savePlayer(Player player){
		int newPlayerId = 0;
		Cursor c = db.rawQuery("SELECT id FROM player ORDER BY id DESC LIMIT 1", null);
		if(c.moveToFirst()){
			newPlayerId = c.getInt(0)+1;
		}
		c.close();
		return (int)db.insert("Player", null, getPlayerContentValues(player, newPlayerId));
		
	}
	
	/**
	 * Convert player to ContentValues
	 * @param p
	 * @param id
	 * @return
	 */
	private ContentValues getPlayerContentValues(Player p, int id)
	{
		String playerName = p.name;
		checkIfPlayerExists(playerName);
		
		ContentValues cv = new ContentValues();
//		cv.put("name", p.name);
		cv.put("name", playerName);
		cv.put("health", p.health);
		cv.put("armor", p.armor);
		cv.put("resources", p.resources);
		cv.put("deckid", saveDeck(p.getDeck(), id, Db.CARDLISTTYPE_DECK));
		cv.put("handid", saveHand(p.getHand(), id, Db.CARDLISTTYPE_HAND));
		cv.put("id", id);
		
		return cv;
	}

	private int saveDeck(List<Card> deck, int playerId, String cardListType) {
		Card[] cards = deck.toArray(new Card[deck.size()]);
		return saveHand(cards, playerId, cardListType);
	}

	private int saveHand(Card[] cards, int playerId, String cardListType) {
		if(checkIfCardlistTypeExists(cardListType)){
			
			ContentValues cv = new ContentValues();
			cv.put("type", cardListType);
			cv.put("player", playerId);
			
			int rowId = (int)db.insert("Playercardlist", null, cv);
			db.beginTransaction();
			for (int i = 0; i < cards.length; i++) {
				ContentValues handCV = new ContentValues();
				handCV.put("cardid", cards[i].id);
				handCV.put("sessioncardlistid", rowId);
				handCV.put("position", i);
				
				db.insert("Playercard", null, handCV);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			
			return rowId;
		}
		//db.compileStatement("INSERT INTO Playercardlist ")
		//db.rawQuery("INSERT INTO Playercardlist SELECT ", selectionArgs)
		return -1;
	}
	
	private boolean checkIfPlayerExists(String playerName){
		Cursor c = db.rawQuery("SELECT * FROM "+"PlayerProfile"+" WHERE name LIKE ?", 
				new String[]{playerName});
		if(c.getCount() <= 0){
			c.close();
			return false;
		}
		c.close();
		return true;
	}
	private boolean checkIfCardlistTypeExists(String cardlistType){
		Cursor c = db.rawQuery("SELECT * FROM "+Db.TABLE_NAME_CARDLISTTYPE+" WHERE name LIKE ?", 
				new String[]{cardlistType});
		if(c.getCount() <= 0){
			c.close();
			return false;
		}
		c.close();
		return true;
	}
	
	public void deleteAllSessions()
	{
			db.delete("Session", null, null);
	}

}
