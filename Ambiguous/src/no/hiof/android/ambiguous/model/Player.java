package no.hiof.android.ambiguous.model;

import java.util.List;
import java.util.Random;

import no.hiof.android.ambiguous.DeckBuilder;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.floatingtext.FloatingHandler;
import no.hiof.android.ambiguous.floatingtext.FloatingTextAnimationListener;
import android.graphics.Color;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class Player {
	private int id;
	private String name;
	private List<Card> deck;
	private Card[] cards;
	private final int maxHealth = 150;
	private final int maxArmor = 250;
	private int health = maxHealth;
	private int armor = 0;
	private int resources = 10;
	private boolean alive = true;
	private Random deckRandom;
	private boolean isAI = false;
	
	private ViewGroup viewGroup;
	
	public Player(String name, ViewGroup viewGroup)
	{
		this.name = name;
		cards = new Card[8];
		this.viewGroup = viewGroup;
	}

	public Player(String name, ViewGroup viewGroup, boolean isAI)
	{
		this(name,viewGroup);
		this.isAI = isAI;
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

	public Card[] GetCards()
	{
		return cards;
	}

	public void Damage(int amount)
	{
		displayFloatingNumber(amount*-1,Color.RED);
		if(this.armor>=amount)
		{
			this.armor -= amount;
			amount = 0;
		}
		else
		{
			amount -= this.armor;
			this.armor = 0;
		}

		this.health -= amount;
		if(this.health<0){this.alive = false;}
	}
	
	public void Heal(int amount)
	{
		this.health = (this.health + amount>this.maxHealth?this.maxHealth:this.health+amount);
		displayFloatingNumber(amount,Color.rgb(45, 190, 50));
	}

	public void ModArmor(int amount)
	{
		this.armor = (this.armor + amount>this.maxArmor?this.maxArmor:this.armor+amount);
		
		displayFloatingNumber(amount,Color.BLUE);
	}

	public boolean getAlive()
	{
		return this.alive;
	}
	
	public boolean UseResources(int amount)
	{
		if(amount > this.resources){return false;}		
		this.resources -= amount;
		return true;
	}
	
	public void ModResource(int amount)
	{
		this.resources = (this.resources + amount < 0?0:this.resources+amount);
		if(amount>1){displayFloatingNumber(amount,Color.rgb(180,180,50));}
	}
	
	public String getStats()
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
	
	private void displayFloatingNumber(int amount, int color)
	{

		TextView floatingText = (TextView)LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.floatingtextview,null);
		
		viewGroup.addView(floatingText);
		
		floatingText.setText(Integer.toString(amount));
		floatingText.setTextColor(color);		
		
		Animation ani = AnimationUtils.makeInAnimation(floatingText.getContext(),(isAI?false:true));
		ani.setAnimationListener(new FloatingTextAnimationListener(floatingText,new FloatingHandler(floatingText,(isAI?true:false)),TextView.VISIBLE));
		floatingText.startAnimation(ani);
	}
}
