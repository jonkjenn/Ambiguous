package no.hiof.android.ambiguous.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Card implements Parcelable{
	private int id;
	private String name;
	private String description;
	private int cost;
	private String image;
	private List<Effect> effects;
	
	public Card()
	{
		effects = new ArrayList<Effect>();
	}
	
	public Card(String name)
	{
		this();
		this.name = name;
	}
	
	public int getId() {
		return id;
	}
	public Card setId(int id) {
		this.id = id;
		return this;
	}
	public String getName() {
		return this.name;
	}
	public Card setName(String name) {
		this.name = name;
		return this;
	}
	public String getDescription() {
		return description;
	}
	public Card setDescription(String description) {
		this.description = description;
		return this;
	}
	public int getCost() {
		return cost;
	}
	public Card setCost(int cost) {
		this.cost = cost;
		return this;
	}
	public String getImage() {
		return image;
	}
	public Card setImage(String image) {
		this.image = image;
		return this;
	}
	public List<Effect> getEffects() {
		return effects;
	}
	public Card setEffects(List<Effect> effects) {
		this.effects = effects;
		return this;
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
