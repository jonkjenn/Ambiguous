package no.hiof.android.ambiguous.datasource;

import java.util.ArrayList;
import java.util.List;

import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * For getting effects from and adding effects to the database.
 */
public class EffectDataSource {
	private static final String SELECT_EFFECTS = "SELECT * FROM Effect WHERE card_id = ?";

	private SQLiteDatabase db;

	public EffectDataSource(SQLiteDatabase db)
	{
		this.db = db;
	}

	/**
	 * Convert effect to ContentValues.
	 * @param e The effect to convert.
	 * @param id The id of the card the effect is attached to.
	 * @return
	 */
	public ContentValues getEffectContentValues(Effect e, long id)
	{
		ContentValues v = new ContentValues();
        v.put("card_id", id);
        v.put("type", e.type.toString());
        v.put("target", e.target.toString());
        v.put("minvalue", e.minValue);
        v.put("maxvalue",e.maxValue);
        v.put("crit", e.crit);
        return v;
	}

	/**
	 * @param card The card we want to find the effects for.
	 * @return List of all the effects on a specific card.
	 */
	public List<Effect> getEffects(Card card)
	{
		List<Effect> effects = new ArrayList<Effect>();
		Cursor c = db.rawQuery(SELECT_EFFECTS, new String[] {Integer.toString(card.id)});
		c.moveToFirst();
		while(!c.isAfterLast())
		{
			Effect e = new Effect();
			e.id = (c.getInt(c.getColumnIndex("id")));
			e.type = (Effect.EffectType.valueOf(c.getString(c.getColumnIndex("type"))));
			e.target = (Effect.Target.valueOf(c.getString(c.getColumnIndex("target"))));
			e.minValue = (c.getInt(c.getColumnIndex("minvalue")));
			e.maxValue = (c.getInt(c.getColumnIndex("maxvalue")));
			e.crit = (c.getInt(c.getColumnIndex("crit")));
			effects.add(e);
			c.moveToNext();
		}
		
		c.close();
		
		return effects;
	}

}
