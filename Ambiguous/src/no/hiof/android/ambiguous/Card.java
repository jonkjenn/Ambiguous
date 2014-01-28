package no.hiof.android.ambiguous;

import java.util.List;

public class Card {
	private int id;
	private String name;
	private String description;
	private int cost;
	private String image;
	private List<Effect> effects;
	
	public Card()
	{
	}
	
	public Card(String name)
	{
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
		return name;
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
}
