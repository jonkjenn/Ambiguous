package no.hiof.android.ambiguous.datasource;

import java.util.ArrayList;
import java.util.List;

import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class EffectDataSource {
	private static final String SELECT_EFFECTS = "SELECT * FROM Effect WHERE card_id = ?";

	private SQLiteDatabase db;

	public EffectDataSource(SQLiteDatabase db)
	{
		this.db = db;
	}

	public ContentValues getEffectContentValues(Effect e, long id)
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

	public List<Effect> getEffects(Card card)
	{
		List<Effect> effects = new ArrayList<Effect>();
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
		
		return effects;
	}

}
