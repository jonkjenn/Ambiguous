package no.hiof.android.ambiguous.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable{
	public int id;
	public String name;
	public String description;
	public int cost;
	public String image;
	public List<Effect> effects;
	
	public Card()
	{
		effects = new ArrayList<Effect>();
	}
	
	public Card(String name)
	{
		this();
		this.name = name;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeInt(cost);
		dest.writeString(image);
		dest.writeList(effects);
	}
	
	public static final Parcelable.Creator<Card> CREATOR = new Parcelable.Creator<Card>() {

		@Override
		public Card createFromParcel(Parcel source) {
			return new Card(source);
		}

		@Override
		public Card[] newArray(int size) {
			return new Card[size];
		}
		
	};
	
	public Card(Parcel source){
		id = source.readInt();
		name = source.readString();
		description = source.readString();
		cost = source.readInt();
		image = source.readString();
		source.readList(effects, null);
	}
}
