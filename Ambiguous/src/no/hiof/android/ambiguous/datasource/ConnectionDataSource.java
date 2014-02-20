package no.hiof.android.ambiguous.datasource;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ConnectionDataSource {
	SQLiteDatabase db;

	public ConnectionDataSource(SQLiteDatabase db)
	{
		this.db = db;		
	}
	
	public void AddConnection(String address)
	{
		List<String> existing = GetAddresses();
		
		for(int i=0;i<existing.size();i++)
		{
			if(existing.get(i).equals(address))
			{
				return;
			}
		}
		
		ContentValues cv = new ContentValues();
		cv.put("ip", address);
		this.db.insert("Connection",null,cv);
	}
	
	public List<String> GetAddresses()
	{
		List<String> address = new ArrayList<String>();
		
		Cursor c = this.db.query("Connection",new String[]{"ip"},null,null,null,null,"ip");
		
		
		while(c.moveToNext())
		{
			address.add(c.getString(c.getColumnIndex("ip")));
		}
		
		return address;
	}

}
