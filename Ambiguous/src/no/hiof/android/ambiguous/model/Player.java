package no.hiof.android.ambiguous.model;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import no.hiof.android.ambiguous.DeckBuilder;
import no.hiof.android.ambiguous.model.Effect.EffectType;

/**
 * Keeps track of player's statistics and cards.
 */
public class Player implements Parcelable {
	public String name;
	public List<Card> deck;
	public Card[] hand;
	public final int maxHealth = 150;
	public final int maxArmor = 250;
	public int health = maxHealth;
	public int armor = 0;
	public int resources = 10;
	public static final int NUMBER_OF_CARDS = 8;
	private boolean alive = true;

	public Player(String name, int health, int armor, int resources,
			Card[] hand, List<Card> deck) {
		super();
		this.name = name;
		this.health = health;
		this.armor = armor;
		this.resources = resources;
		this.hand = hand;
		this.deck = deck;
	}

	public void updatePlayer(String name, int health, int armor, int resources,
			Card[] hand, List<Card> deck) {
		this.name = name;
		this.health = health;
		this.armor = armor;
		this.resources = resources;
		this.hand = hand;
		this.deck = deck;

		notifyStatsUpdateListeners();
		notifyCardsUpdateListeners();
	}

	/**
	 * Copies all data from another Player.
	 * 
	 * @param source
	 *            The source Player we want to copy data from.
	 */
	public void updatePlayer(Player source) {
		if(source == null){return;}
		updatePlayer(source.name, source.health, source.armor,
				source.resources, source.hand, source.deck);
	}

	public Player(String name) {
		this.name = name;
		hand = new Card[NUMBER_OF_CARDS];
	}

	/**
	 * Fill up empty spots in player's hand with cards from deck.
	 */
	public void pullCards() {
		if (deck.size() == 0) {
			deck = DeckBuilder.StandardDeck();
			if (deck == null) {
				// Do something to handle exception
			}
		}
		for (int i = 0; i < hand.length; i++) {
			if (hand[i] == null) {
				hand[i] = deck.remove(0);
				notifyCardsUpdateListeners();
			}
		}
	}

	/**
	 * Remove used card from hand.
	 * 
	 * @param position
	 */
	public void cardUsed(int position) {
		hand[position] = null;
		pullCards();
	}

	public Player setDeck(List<Card> deck) {
		this.deck = deck;
		this.pullCards();
		return this;
	}

	// Replaces the whole hand
	public void setHand(Card[] hand) {
		this.hand = hand;
		notifyCardsUpdateListeners();
	}

	public List<Card> getDeck() {
		return deck;
	}

	/**
	 * 
	 * @param position
	 *            Position of the card in the players hand.
	 * @return
	 */
	public Card getCard(int position) {
		return hand[position];
	}

	public boolean isAlive() {
		return health > 0;
	}

	public Card[] getHand() {
		return hand;
	}

	/**
	 * Applies damage to this player.
	 * 
	 * @param amount
	 *            Amount of damage.
	 */
	public void damage(int amount) {
		notifyStatChange(amount * -1, EffectType.DAMAGE);
		if (this.armor >= amount) {
			this.armor -= amount;
			amount = 0;
		} else if (this.armor > 0) {
			amount -= this.armor;
			this.armor = 0;
		}

		this.health -= amount;
		notifyStatsUpdateListeners();
		if (this.health <= 0) {
			this.alive = false;
		}
	}

	/**
	 * Applies increase in health to this player.
	 * 
	 * @param amount
	 */
	public void heal(int amount) {
		this.health = (this.health + amount > this.maxHealth ? this.maxHealth
				: this.health + amount);
		notifyStatsUpdateListeners();
		notifyStatChange(amount, EffectType.HEALTH);// Color.rgb(45, 190, 50));
	}

	/**
	 * Adds or removes armor from the player.
	 * 
	 * @param amount
	 *            Amount of armor. Use negative values to remove armor.
	 */
	public void modArmor(int amount) {
		this.armor = (this.armor + amount > this.maxArmor ? this.maxArmor
				: this.armor + amount);
		notifyStatsUpdateListeners();

		notifyStatChange(amount, EffectType.ARMOR);
	}

	/**
	 * Removes resources from the player.
	 * 
	 * @param amount
	 *            How many resources to remove.
	 * @return True if player has enough resources, false if player has less
	 *         resources then amount.
	 */
	public boolean useResources(int amount) {
		if (amount > this.resources) {
			return false;
		}
		this.resources -= amount;
		notifyStatsUpdateListeners();
		return true;
	}

	/**
	 * Modifies the amount of resources the player has.
	 * 
	 * @param amount
	 *            The amount of resources. Can be negative.
	 */
	public void modResource(int amount) {
		// If the resource change will put resources below 0, we do not apply
		// the amount.
		this.resources = (this.resources + amount < 0 ? this.resources + amount
				: this.resources + amount);
		notifyStatChange(amount, EffectType.RESOURCE);
		notifyStatsUpdateListeners();
	}

	public void setHealth(int health) {
		this.health = health;
		notifyStatsUpdateListeners();
	}

	public void setArmor(int armor) {
		this.armor = armor;
		notifyStatsUpdateListeners();
	}

	public void setResources(int resources) {
		this.resources = resources;
		notifyStatsUpdateListeners();
	}

	private PlayerUpdateListener listener;

	public interface PlayerUpdateListener {
		void onCardsUpdateListener(Player player, Card[] cards);

		// Is a lot of overlap between these 2.
		void onStatsUpdateListener(Player player);

		void onStatChange(Player player, int amount, Effect.EffectType type);
	}

	private void notifyStatChange(int amount, Effect.EffectType type) {
		if (listener != null) {
			listener.onStatChange(this, amount, type);
		}
	}

	/** For notifying about changes on player's hand. */
	public void notifyCardsUpdateListeners() {
		if (listener != null) {
			listener.onCardsUpdateListener(this, this.hand);
		}
	}

	/**
	 * Adds a listener that will receive updates about changes in player's
	 * statistics and cards.
	 * 
	 * @param listener
	 */
	public void setPlayerUpdateListener(PlayerUpdateListener listener) {
		this.listener = listener;
		notifyCardsUpdateListeners();
		notifyStatsUpdateListeners();
	}

	/**
	 * Notify changes in player's statistics.
	 */
	public void notifyStatsUpdateListeners() {
		if(listener != null)
		{
			listener.onStatsUpdateListener(this);
		}
	}

	// Required by parcelable
	@Override
	public int describeContents() {
		return 0;
	}

	// Converts player to parcel.
	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name);
		out.writeInt(health);
		out.writeInt(armor);
		out.writeInt(resources);

		// Convert hand to List<Card>, cause writing an array as parcel
		// obviously is a pain, cause you can't cast Object[] to Card[]
		List<Card> arList = new ArrayList<Card>();
		for (int i = 0; i < hand.length; i++) {
			arList.add(hand[i]);
		}
		out.writeList(arList);
		out.writeList(deck);
	}

	public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
		public Player createFromParcel(Parcel in) {
			return new Player(in);
		}

		public Player[] newArray(int size) {
			return new Player[size];
		}
	};

	/**
	 * Recreate player from Parcel.
	 * 
	 * @param in
	 */
	public Player(Parcel in) {
		name = in.readString();
		health = in.readInt();
		armor = in.readInt();
		resources = in.readInt();

		List<Card> placeHolder = in.readArrayList(Card.class.getClassLoader());
		hand = placeHolder.toArray(new Card[placeHolder.size()]);

		deck = in.readArrayList(Card.class.getClassLoader());
	}
}
