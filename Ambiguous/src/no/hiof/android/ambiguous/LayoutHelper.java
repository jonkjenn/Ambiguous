package no.hiof.android.ambiguous;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

public class LayoutHelper {
	public static void showResult(TextView resultTextView, boolean victory) {
		resultTextView.setText((victory ? R.string.victory : R.string.defeat));
		resultTextView.setTextColor((victory ? Color.GREEN : Color.RED));
		resultTextView.setVisibility(View.VISIBLE);
	}

	public static void hideResult(TextView resultTextView) {
		resultTextView.setVisibility(View.GONE);
	}
}
