package no.hiof.android.ambiguous;

public class Effect {
	public static enum EffectType {HEALTH, ARMOR, DAMAGE};
	public static enum Target {SELF,OPPONENT};
	
	private int id;
	private EffectType type;
	private int minValue;
	private int maxValue;
	private int crit;
	private Target target;

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

}
