package no.hiof.android.ambiguous.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import no.hiof.android.ambiguous.model.Effect.EffectType;

public class Player implements Parcelable{
	private int mData;
	//private int id;
	private String name;
	private List<Card> deck;
	private Card[] cards;
	public final int maxHealth = 150;
	public final int maxArmor = 250;
	private int health = maxHealth;
	private int armor = 0;
	private int resources = 10;
	private boolean alive = true;
	
	public Player(String name)
	{
		this.name = name;
		cards = new Card[8];
	}

	public void PullCards()
	{
		if(deck.size()==0){return;}
		//if(deckRandom == null){deckRandom = new Random();}
		for(int i=0;i<cards.length;i++)
		{
			if(cards[i] == null)
			{
				//cards[i] = deck.get(deckRandom.nextInt(deck.size()-1));
				cards[i] = deck.remove(0);
				notifyCardsUpdateListeners();
			}
		}
	}
	
	public void CardUsed(int position)
	{
		cards[position] = null;
		PullCards();
	}
	
	public Player SetDeck(List<Card> deck)
	{
		this.deck = deck;
		this.PullCards();
		return this;
	}

	public List<Card> GetDeck()
	{
		return deck;
	}

	public Card GetCard(int position)
	{
		return cards[position];
	}
	
	public Card[] GetCards()
	{
		return cards;
	}

	public void Damage(int amount)
	{
		notifyStatChange(amount*-1,EffectType.DAMAGE);
		if(this.armor>=amount)
		{
			this.armor -= amount;
			amount = 0;
			notifyArmorUpdateListener();
		}
		else if(this.armor>0)
		{
			amount -= this.armor;
			this.armor = 0;
			notifyArmorUpdateListener();
		}

		this.health -= amount;
		notifyStatsUpdateListeners();
		if(this.health<=0){this.alive = false;}
	}
	
	public void Heal(int amount)
	{
		this.health = (this.health + amount>this.maxHealth?this.maxHealth:this.health+amount);
		notifyStatsUpdateListeners();
		notifyStatChange(amount,EffectType.HEALTH);//Color.rgb(45, 190, 50));
	}

	public void ModArmor(int amount)
	{
		this.armor = (this.armor + amount>this.maxArmor?this.maxArmor:this.armor+amount);
		notifyStatsUpdateListeners();
		notifyArmorUpdateListener();
		
		notifyStatChange(amount,EffectType.ARMOR);
	}

	public boolean getAlive()
	{
		return this.alive;
	}
	
	public boolean UseResources(int amount)
	{
		if(amount > this.resources){return false;}		
		this.resources -= amount;
		notifyStatsUpdateListeners();
		return true;
	}
	
	public void ModResource(int amount)
	{
		this.resources = (this.resources + amount < 0?0:this.resources+amount);
		notifyStatChange(amount,EffectType.RESOURCE);//Color.rgb(180,180,50));
		notifyStatsUpdateListeners();
	}
	
	private String getStats()
	{
		return this.name + " Health: " + this.health + " Armor: " + this.armor + " Res: " + this.resources;
	}
	
	public int getHealth()
	{
		return this.health;
	}
	
	public int getArmor()
	{
		return this.armor;
	}
	
	public int getResources()
	{
		return this.resources;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	private ArrayList<PlayerUpdateListener> listeners = new ArrayList<PlayerUpdateListener>();
	public interface PlayerUpdateListener
	{
		void onCardsUpdateListener(Player player, Card[] cards);		
		void onStatsUpdateListener(Player player,String str);
		void onArmorUpdateListener(Player player, int armor);
		void onStatChange(Player player, int amount, Effect.EffectType type);
	}
	
	private void notifyStatChange(int amount, Effect.EffectType type)
	{
		for(PlayerUpdateListener listener:listeners)
		{
			listener.onStatChange(this, amount,type);
		}
	}
	
	private void notifyArmorUpdateListener()
	{
		for(PlayerUpdateListener listener:listeners)
		{
			listener.onArmorUpdateListener(this,this.armor);
		}
	}
	
	//For adding, notifying listeners to update in players cards. The cards "on the board" not the deck we pull cards from.
	public void notifyCardsUpdateListeners()
	{
		for(PlayerUpdateListener listener:listeners)
		{
			listener.onCardsUpdateListener(this,this.cards);
		}
	}
	public void setPlayerUpdateListeners(PlayerUpdateListener listener)
	{
		this.listeners.add(listener);
		notifyCardsUpdateListeners();
		notifyStatsUpdateListeners();
	}
	
	//For notifying listeners to updates in players stats.
	public void notifyStatsUpdateListeners()
	{
		for(PlayerUpdateListener listener:listeners)
		{
			listener.onStatsUpdateListener(this,getStats());			
		}
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	/** save object in parcel */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mData);
		
	}
	
	public static final Parcelable.Creator<Player> CREATOR
	= new Parcelable.Creator<Player>() {
		public Player createFromParcel(Parcel in) {
			return new Player(in);
		}
		public Player[] newArray(int size) {
			return new Player[size];
		}
	};
	
	/** recreate object from parcel */
	private Player(Parcel in){
		mData = in.readInt();
	}
}
