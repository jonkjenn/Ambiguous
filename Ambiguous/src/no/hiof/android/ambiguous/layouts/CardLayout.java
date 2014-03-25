package no.hiof.android.ambiguous.layouts;

import java.util.HashMap;
import java.util.List;

import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Builds layouts for the Cards, also handles caching of bitmaps used to prevent
 * building the same Card bitmap mulitple times. We use bitmaps instead of
 * complicated views with images to hopefully improve performance.
 */
public class CardLayout {

	// Cache of generated bitmaps.
	private static HashMap<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();

	/**
	 * Checks if we have the card in cache, if not generates a new image.
	 * 
	 * @param card
	 *            The card we want a bitmap for.
	 * @param parent
	 * @return A bitmap image of the card.
	 */
	public static Bitmap getCardBitmap(Card card, ViewGroup parent) {
		// If exist in cache.
		if (bitmaps.containsKey(card.getName())) {
			return bitmaps.get(card.getName());
		} else// Create a new bitmap.
		{
			getCardLayout(card, parent);
			return getCardBitmap(card, parent);
		}
	}

	/**
	 * Creates a View for a Card.
	 * 
	 * @param card
	 * @param parent
	 * @return
	 */
	public static View getCardLayout(Card card, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) parent.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.card_game, parent, false);
		// Return a empty view if no card.
		if (card == null) {
			return view;
		}

		// If the card has been created before we return it from the cache.
		if (bitmaps.containsKey(card.getName())) {
			ImageView v = (ImageView) inflater.inflate(R.layout.card_game2,
					parent, false);
			v.setTag(card.getImage());
			v.setImageBitmap(bitmaps.get(card.getName()));
			return v;
		}

		View cardView = createCardView(view, card, parent);
		// Create a bitmap to use instead of the View, improve performance.
		Bitmap b = viewToBitmap(cardView);
		// Add the bitmap to the cache to prevent creating more of the same.
		bitmaps.put(card.getName(), b);

		// Build the simple ImageView to use for the bitmap of the Card View.
		ImageView v = (ImageView) inflater.inflate(R.layout.card_game2, parent,
				false);
		v.setTag(card.getImage());
		v.setImageBitmap(b);

		return v;
	}

	/**
	 * First creates a view from a Card.
	 * 
	 * @param view
	 *            The base view already inflated from layout.
	 * @param card
	 * @param parent
	 * @return
	 */
	private static View createCardView(View view, Card card, ViewGroup parent) {
		TextView cost = (TextView) view.findViewById(R.id.card_cost);
		TextView name = (TextView) view.findViewById(R.id.card_name);
		TextView description = (TextView) view
				.findViewById(R.id.card_description);
		ImageView image = (ImageView) view.findViewById(R.id.card_image);
		cost.setText(Integer.toString(card.getCost()));
		name.setText(card.getName());
		if (description != null) {
			description.setText(card.getDescription());
		}

		// We store the images as drawable resources, so got to use this "hack"
		// to get the drawable id from the image name stored in the database.
		// The image name stored in the database must equal the filename
		// excluding the file extension.
		int imageId = parent
				.getContext()
				.getResources()
				.getIdentifier(card.getImage(), "drawable",
						parent.getContext().getPackageName());

		if (imageId > 0) {
			image.setImageResource(imageId);
		}

		//Add the effects to the card.
		List<Effect> e = card.getEffects();
		LinearLayout l = (LinearLayout) view.findViewById(R.id.card_effects);
		l.removeAllViews();// For fixing double effects when resume activity

		for (int i = 0; i < e.size(); i++) {
			l.addView(getEffectView(e.get(i), parent.getContext()));
		}

		return view;
	}

	/**
	 * Converts a view to a bitmap.
	 * @param view
	 * @return
	 */
	private static Bitmap viewToBitmap(View view) {
		//Without this the bitmap is blank.
		view.measure(MeasureSpec.makeMeasureSpec(view.getLayoutParams().width,
				MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
				view.getLayoutParams().height, MeasureSpec.EXACTLY));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.setDrawingCacheEnabled(true);

		return Bitmap.createBitmap(view.getDrawingCache());
	}

	/**
	 * Creates a View from an Effect.
	 * @param e
	 * @param context
	 * @return
	 */
	private static TextView getEffectView(Effect e, Context context) {
		String text = "";

		String min = Integer.toString(e.getMinValue());
		String max = Integer.toString(e.getMaxValue());
		String crit = Integer.toString(e.getCrit());

		//Textview that shows the effects min/max/crit
		TextView t = new TextView(context);
		t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
		t.setTextColor(Color.WHITE);

		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layout.leftMargin = 7;
		t.setLayoutParams(layout);
		t.setPadding(3, 0, 3, 0);

		//Change the color and layout depending on which effect type.
		switch (e.getType()) {
		case ARMOR:
			text = min;
			t.setBackgroundColor(Color.BLUE);
			break;
		case DAMAGE:
			text = min + "-" + max + "(" + crit + ")";
			t.setBackgroundColor(Color.RED);
			break;
		case HEALTH:
			text = min + "-" + max + "(" + crit + ")";
			t.setBackgroundColor(Color.rgb(45, 190, 50));
			break;
		case RESOURCE:
			text = min;
			t.setBackgroundColor(Color.rgb(180, 180, 50));
			break;
		default:
			break;
		}

		t.setText(text);

		return t;
	}
}
