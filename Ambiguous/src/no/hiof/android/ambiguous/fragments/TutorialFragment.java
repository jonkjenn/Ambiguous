package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that is shown on startup for new players. Will teach the player how
 * to play. Is also accessible by a button. Can be disabled.
 * 
 * Will show the player what is his hit points, armor and resources. How to use
 * cards by pulling up and discarding by pulling down.
 */
public class TutorialFragment extends Fragment {
	private boolean hideNeverShowButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle arguments = getArguments();

		//So we can hide the never show tutorial button.
		if (arguments != null) {
			hideNeverShowButton = arguments.getBoolean("hideNeverShow", false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_tutorial, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (hideNeverShowButton) {
			getActivity().findViewById(R.id.neverShowTutorialButton)
					.setVisibility(View.GONE);
		}
	}
}
