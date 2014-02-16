package no.hiof.android.ambiguous;

import java.util.ArrayList;

import no.hiof.android.ambiguous.model.Card;

public class OpponentController {
	
	public void PlayCard(Card card)
	{
		notifyPlayCard(card);		
	}
	
	public void DiscardCard(Card card)
	{
		notifyDiscardCard(card);
	}
	
	private void notifyPlayCard(Card card)
	{
		for(OpponentListener listener:listeners)
		{
			listener.onOpponentPlayCard(card);
		}
	}
	private void notifyDiscardCard(Card card)
	{
		for(OpponentListener listener:listeners)
		{
			listener.onOpponentDiscardCard(card);
		}
	}
	private ArrayList<OpponentListener> listeners = new ArrayList<OpponentListener>();
	public void setOpponentListener(OpponentListener listener)
	{
		listeners.add(listener);
	}
	public interface OpponentListener
	{
		void onOpponentPlayCard(Card card);
		void onOpponentDiscardCard(Card card);
	}

}
