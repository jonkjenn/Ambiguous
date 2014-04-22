package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlayerStatsFragment extends Fragment {

	TextView name;
	TextView health;
	TextView armor;
	TextView resources;

	ViewGroup floatingHealth;
	ViewGroup floatingArmor;
	ViewGroup floatingResources;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle args = this.getArguments();
		boolean reverse = false;
		if (args != null) {
			reverse = this.getArguments().getBoolean("reverse", false);
		}
		return inflater.inflate(
				(reverse ? R.layout.fragment_playerstats_reverse
						: R.layout.fragment_playerstats), container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		this.name = (TextView) view.findViewById(R.id.name);
		this.health = (TextView) view.findViewById(R.id.health);
		this.armor = (TextView) view.findViewById(R.id.armor);
		this.resources = (TextView) view.findViewById(R.id.resource);

		this.floatingHealth = (ViewGroup) view
				.findViewById(R.id.floating_health);
		this.floatingArmor = (ViewGroup) view.findViewById(R.id.floating_armor);
		this.floatingResources = (ViewGroup) view
				.findViewById(R.id.floating_resource);
		
		if(onLoadedListener != null){onLoadedListener.onLoaded();}
	}

	public void setStats(String name, int health, int armor, int resources) {
		if (this.name == null) {
			return;
		}
		this.name.setText(name);
		this.health.setText(String.valueOf(health));
		this.armor.setText(String.valueOf(armor));
		this.resources.setText(String.valueOf(resources));
	}

	public void updateStat(EffectType type, int amount) {
		final ViewGroup viewGroup;
		final TextView floatingText;

		// Find the correct viewgroup for the floating text and set the correct
		// color on the text
		int color;
		switch (type) {
		case ARMOR:
			viewGroup = floatingArmor;
			color = Color.BLUE;
			break;
		case DAMAGE:
			viewGroup = floatingHealth;
			color = Color.RED;
			break;
		case HEALTH:
			viewGroup = floatingHealth;
			color = Color.rgb(45, 190, 50);
			break;
		case RESOURCE:
			viewGroup = floatingResources;
			color = Color.rgb(180, 180, 50);
			break;
		default:
			viewGroup = null;
			color = Color.WHITE;
			break;
		}

		int index = -1;

		// Find the first empty textview so we can put the new stat text there.
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			if (((TextView) viewGroup.getChildAt(i)).getText().length() == 0) {
				index = i;
				break;
			}
		}

		// We have to set this outside the loop because its final, is a better
		// way to do this?
		floatingText = (index >= 0 ? (TextView) viewGroup.getChildAt(index)
				: null);

		// TODO: Improve this?
		// If there is no empty text fields the stat wont be shown. Could
		// improve this.
		if (floatingText == null || viewGroup == null) {
			return;
		}

		// Adds the + sign if positive damage, negative shows automatically.
		floatingText.setText((amount >= 0 ? "+" : "")
				+ Integer.toString(amount));
		floatingText.setTextColor(color);

		// So text is removed after a delay
		Runnable r = new Runnable() {
			@Override
			public void run() {
				floatingText.setText("");
			}
		};
		new Handler().postDelayed(r, 2000);
	}
/**
 * Sets the background color to red to indicate player's turn.
 */
	public void myTurn() {
		name.setBackgroundColor(Color.RED);
	}

/**
 * Sets the background color to transparent to indicate that it's player's turn.
 */
	public void notMyTurn() {
		name.setBackgroundColor(Color.TRANSPARENT);
	}
	
	public interface OnLoadedListener
	{
		void onLoaded();
	}
	
	OnLoadedListener onLoadedListener;
	
	public void setOnLoadedListener(OnLoadedListener listener)
	{
		this.onLoadedListener = listener;
	}
}