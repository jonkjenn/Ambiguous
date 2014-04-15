package no.hiof.android.ambiguous.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Effect implements Parcelable{
	public static enum EffectType {HEALTH, ARMOR, DAMAGE, RESOURCE};
	public static enum Target {SELF,OPPONENT};
	
	public int id;
	public EffectType type;
	public int minValue;
	public int maxValue;
	public int crit;
	public Target target;

	public Effect(){
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(type.ordinal());
		dest.writeInt(minValue);
		dest.writeInt(maxValue);
		dest.writeInt(crit);
		dest.writeInt(target.ordinal());
		
	}

	public static final Parcelable.Creator<Effect> CREATOR = new Parcelable.Creator<Effect>() {

		@Override
		public Effect createFromParcel(Parcel source) {
			return new Effect(source);
		}

		@Override
		public Effect[] newArray(int size) {
			return new Effect[size];
		}
		
	};
	
	public Effect(Parcel source){
		id = source.readInt();
		type = EffectType.values()[source.readInt()];
		minValue = source.readInt();
		maxValue = source.readInt();
		target = Target.values()[source.readInt()];
	}
}
