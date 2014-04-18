package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.R;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DragFragment extends Fragment implements OnDragListener {
	private View useCardNotificationView;
	private View discardCardNotificationView;
	boolean enabled = false;
	ImageView dragCard;

	int height, width;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_drag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.setOnDragListener(this);

		this.useCardNotificationView = view.findViewById(R.id.gameview_use);
		this.discardCardNotificationView = view
				.findViewById(R.id.gameview_discard);
		this.dragCard = (ImageView) view.findViewById(R.id.drag_card);
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {

		// Data passed from the touch even that starts the drag.
		CardHandFragment.CardTouchData cardTouchData = null;

		// Convert the object passed from card touch
		if (event.getLocalState() != null
				&& event.getLocalState() instanceof CardHandFragment.CardTouchData) {
			cardTouchData = (CardHandFragment.CardTouchData) event
					.getLocalState();
		}

		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			if (enabled) {
				if (event.getLocalState() != null) {
					startDrag(cardTouchData.bitmap, cardTouchData.position);
					return true;
				}
			} else {
				return true;
			}
			return false;

		case DragEvent.ACTION_DRAG_LOCATION:
			if (event.getLocalState() != null) {
				handleDragToLocation(cardTouchData, (int) event.getX(),
						(int) event.getY());
				return true;
			}
			return false;

		case DragEvent.ACTION_DROP:
			if (event.getLocalState() != null) {
				handleDropEvent(cardTouchData, (int) event.getX(),
						(int) event.getY());
			}
			return true;
		case DragEvent.ACTION_DRAG_EXITED:

			// If we drag outside of the activity we reset the drag, BUT we also
			// use the coordinates as a drop location
			// For example Dragging card out at the top will use the card.
			useCardNotificationView.setVisibility(View.GONE);
			discardCardNotificationView.setVisibility(View.GONE);
			stopDrag(cardTouchData.position);
			handleDropEvent(cardTouchData, (int) event.getX(),
					(int) event.getY());
		}

		return false;
	}

	/**
	 * We drag cards by creating a copy of the image used on the card and move
	 * this around while the stationary actual card is hidden.
	 * 
	 * @param card
	 *            The position of the card in the players hand.
	 */
	private void startDrag(Bitmap bitmap, int card) {
		if (this.height == 0) {
			this.height = this.getView().getMeasuredHeight();
			this.width = this.getView().getMeasuredWidth();
		}

		// Get the bitmap used on the stationary card and use this as bitmap on
		// our "drag card".
		// layout.setImageBitmap(((BitmapDrawable)
		// dragCardSource.getDrawable()).getBitmap());
		dragCard.setImageBitmap(bitmap);

		dragCard.setVisibility(ImageView.GONE);
	}

	/**
	 * Moves the card we're dragging around the screen to a new position.
	 * 
	 * @param card
	 *            The position of the card in the players hand.
	 * @param x
	 *            The X coordinate of where we are touching/clicking the screen.
	 * @param y
	 *            The Y coordinate of where we are touching/clicking the screen.
	 * @param insideX
	 *            The X coordinate of where we're pushing on the card to drag
	 *            it.
	 * @param insideY
	 *            The Y coordinate of where we're pushing on the card to drag
	 *            it.
	 */
	void drag(int card, int x, int y, int insideX, int insideY) {
		if (y < 10) {
			useCard(card);
			return;
		}

		RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(
				dragCard.getLayoutParams());
		// Move the card by changing the left and top margins
		par.setMargins(x - insideX, y - insideY, 0, 0);

		// So we dont move the card outside the game view.
		if (par.topMargin - 10 > this.height) {
			discardCard(card);
			return;
		}

		dragCard.setLayoutParams(par);

		//Here we will show the drag card and hide the static card.
		if (dragCard.getVisibility() != View.VISIBLE) {
			notifyDragStatusChanged(card, DRAG_STARTED);

			// Set the view we actually drag around the screen visible.
			dragCard.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * Handles when a drag action moves and generate new coordinates.
	 * 
	 * @param touchData
	 *            The state we pass with the event.
	 * @param eventX
	 * @param eventY
	 */
	private void handleDragToLocation(CardHandFragment.CardTouchData touchData,
			int eventX, int eventY) {
		// For updating the card we drag around.
		drag(touchData.position, eventX, eventY, touchData.localX,
				touchData.localY);

		// If have moved the card upwards enough we display the top bar that
		// tells the user he can now drop the card to use it.
		if (touchData.viewHeight / 2 + eventY < touchData.screenY - 200) {
			useCardNotificationView.setVisibility(View.VISIBLE);
		} else {
			useCardNotificationView.setVisibility(View.INVISIBLE);
		}

		// If have moved the card downwards enough we display the bottom bar
		// that tells the user he can now drop the card to discard it.
		if (touchData.viewHeight / 2 + eventY > this.height - 100) {
			discardCardNotificationView.setVisibility(View.VISIBLE);
		} else {
			discardCardNotificationView.setVisibility(View.INVISIBLE);
		}

		previousY = eventY;
	}

	/**
	 * Handles the drop of a card dragdrop.
	 * 
	 * @param touchData
	 * @param eventX
	 * @param eventY
	 */
	private void handleDropEvent(CardHandFragment.CardTouchData touchData,
			int eventX, int eventY) {

		// If moved the card "enough" upwards use the card.
		if (touchData.viewHeight / 2 + eventY < touchData.screenY - 200) {
			useCard(touchData.position);

		}// If moved the card enough downwards discard the card.
		else if (touchData.viewHeight / 2 + eventY > this.height - 100) {
			// else if (touchData.viewHeight / 2 + eventY > this.layoutContainer
			// .getHeight()) {
			discardCard(touchData.position);
		} else // Cancel the dragdrop so that user can pick a different card.
		{
			stopDrag(touchData.position);
		}
	}

	void discardCard(int card) {
		getActivity().findViewById(R.id.gameview_discard).setVisibility(
				TextView.INVISIBLE);
		stopDrag(card);
		notifyPayerUsedCard(card, true);
	}

	void useCard(int card) {
		useCardNotificationView.setVisibility(View.INVISIBLE);
		stopDrag(card);

		notifyPayerUsedCard(card, false);
	}

	/**
	 * Reshow the stationary card we have previously dragged, typically used
	 * when change our mind on wich card to use.
	 * 
	 * @param card
	 */
	public void stopDrag(int card) {
		getActivity().findViewById(R.id.drag_card)
				.setVisibility(View.INVISIBLE);

		notifyDragStatusChanged(card, DRAG_STOPPED);
	}

	/**
	 * Hide only the card we drag around, typically called when we have used or
	 * discarded a card.
	 */
	/*
	 * public void removeDrag() { getActivity().findViewById(R.id.drag_card)
	 * .setVisibility(View.INVISIBLE); }
	 */

	int previousY = -1;

	void notifyPayerUsedCard(int card, boolean discard) {
		if (playerUsedCardListener != null) {
			playerUsedCardListener.onPlayerUsedCard(card, discard);
		}
	}

	public static final int DRAG_STARTED = 1;
	public static final int DRAG_STOPPED = 0;

	OnPlayerUsedCardListener playerUsedCardListener;
	OnDragStatusChangedListener onDragStatusChangedListener;

	public interface OnPlayerUsedCardListener {
		void onPlayerUsedCard(int card, boolean discard);
	}

	/**
	 * Status is either DragFragment.DRAG_STARTED, or DragFragment.DRAG_STOPPED.
	 * 
	 */
	public interface OnDragStatusChangedListener {
		void onDragStatusChanged(int card, int status);
	}

	void notifyDragStatusChanged(int card, int status) {
		if (onDragStatusChangedListener != null) {
			onDragStatusChangedListener.onDragStatusChanged(card, status);
		}
	}

	public void setOnDragStatusChanged(OnDragStatusChangedListener listener) {
		this.onDragStatusChangedListener = listener;
	}

	public void setPlayerUsedCardListener(OnPlayerUsedCardListener listener) {
		this.playerUsedCardListener = listener;
	}

	public void disableDrag() {
		this.enabled = false;
	}

	public void enableDrag() {
		this.enabled = true;
	}

}
