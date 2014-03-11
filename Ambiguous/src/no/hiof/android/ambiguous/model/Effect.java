package no.hiof.android.ambiguous.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Effect implements Parcelable{
	public static enum EffectType {HEALTH, ARMOR, DAMAGE, RESOURCE};
	public static enum Target {SELF,OPPONENT};
	
	private int id;
	private EffectType type;
	private int minValue;
	private int maxValue;
	private int crit;
	private Target target;

	public Effect(){
		
	}
	public int getId() {
		return id;
	}

	public Effect setId(int id) {
		this.id = id;
		return this;
	}

	public EffectType getType() {
		return type;
	}

	public Effect setType(EffectType type) {
		this.type = type;
		return this;
	}

	public int getMinValue() {
		return minValue;
	}

	public Effect setMinValue(int minValue) {
		this.minValue = minValue;
		return this;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public Effect setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	public int getCrit() {
		return crit;
	}

	public Effect setCrit(int crit) {
		this.crit = crit;
		return this;
	}

	public Target getTarget() {
		return target;
	}

	public Effect setTarget(Target target) {
		this.target = target;
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
