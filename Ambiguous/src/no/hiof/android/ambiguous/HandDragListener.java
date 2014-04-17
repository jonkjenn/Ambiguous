package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.cardlistener.CardOnTouchListener;
import android.annotation.TargetApi;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Preferably should not have GameActivity reference here.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HandDragListener implements OnDragListener {
	private GameMachine gameMachine;
	private GameActivity gameActivity;
	private GridView handView;
	private View useCardNotificationView;
	private View discardCardNotificationView;
	private RelativeLayout layoutContainer;

	int height, width;

	public HandDragListener(GameMachine gameMachine, GameActivity gameActivity) {
		this.useCardNotificationView = gameActivity
				.findViewById(R.id.gameview_use);
		this.discardCardNotificationView = gameActivity
				.findViewById(R.id.gameview_discard);
		this.gameMachine = gameMachine;
		this.gameActivity = gameActivity;
		this.handView = (GridView) gameActivity.findViewById(R.id.game_grid);
		this.layoutContainer = (RelativeLayout) gameActivity
				.findViewById(R.id.game_layout_container);

	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		// Can only drag on players turn.
		if (!gameMachine.isPlayersTurn()) {
			return false;
		}

		// Data passed from the touch even that starts the drag.
		CardOnTouchListener.CardTouchData cardTouchData;

		// Convert the object passed from card touch
		if (event.getLocalState() != null
				&& event.getLocalState() instanceof CardOnTouchListener.CardTouchData) {
			cardTouchData = (CardOnTouchListener.CardTouchData) event
					.getLocalState();

			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				if (event.getLocalState() != null) {
					startDrag(cardTouchData.position);
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
				
				//If we drag outside of the activity we reset the drag, BUT we also use the coordinates as a drop location
				//For example Dragging card out at the top will use the card.
				useCardNotificationView.setVisibility(View.GONE);
				discardCardNotificationView.setVisibility(View.GONE);
				stopDrag(cardTouchData.position);
				handleDropEvent(cardTouchData, (int)event.getX(), (int)event.getY());
			}
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
	private void startDrag(int card) {
		if (this.height == 0) {
			this.height = layoutContainer.getMeasuredHeight();
			this.width = layoutContainer.getMeasuredWidth();
		}
		ImageView layout = (ImageView) gameActivity
				.findViewById(R.id.drag_card);
		ImageView i = (ImageView) handView.getChildAt(card);
		// Get the bitmap used on the stationary card and use this as bitmap on
		// our "drag card".
		layout.setImageBitmap(((BitmapDrawable) i.getDrawable()).getBitmap());

		layout.setVisibility(ImageView.GONE);
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
		// Hide the original card on players deck.
		handView.getChildAt(card).setVisibility(View.INVISIBLE);
		// Set the view we actually drag around the screen visible.
		ImageView parent = (ImageView) gameActivity
				.findViewById(R.id.drag_card);
		parent.setVisibility(ImageView.VISIBLE);

		RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(
				parent.getLayoutParams());
		// Move the card by changing the left and top margins
		par.setMargins(x - insideX, y - insideY, 0, 0);

		// So we dont move the card outside the game view.
		if (par.topMargin - 10 > this.height) {
			discardCard(card);
			return;
		}

		parent.setLayoutParams(par);
	}

	/**
	 * Reshow the stationary card we have previously dragged, typically used
	 * when change our mind on wich card to use.
	 * 
	 * @param card
	 */
	public void stopDrag(int card) {
		handView.getChildAt(card).setVisibility(View.VISIBLE);
		removeDrag();
	}

	/**
	 * Hide only the card we drag around, typically called when we have used or
	 * discarded a card.
	 */
	public void removeDrag() {
		gameActivity.findViewById(R.id.drag_card).setVisibility(View.INVISIBLE);
	}

	int previousY = -1;

	/**
	 * Handles when a drag action moves and generate new coordinates.
	 * 
	 * @param touchData
	 *            The state we pass with the event.
	 * @param eventX
	 * @param eventY
	 */
	private void handleDragToLocation(
			CardOnTouchListener.CardTouchData touchData, int eventX, int eventY) {
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
	private void handleDropEvent(CardOnTouchListener.CardTouchData touchData,
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
		gameActivity.findViewById(R.id.gameview_discard).setVisibility(
				TextView.INVISIBLE);
		removeDrag();
		gameMachine.playerDiscardCard(card);

	}

	void useCard(int card) {
		useCardNotificationView.setVisibility(View.INVISIBLE);
		removeDrag();

		gameActivity.playCard(card);

	}

}
