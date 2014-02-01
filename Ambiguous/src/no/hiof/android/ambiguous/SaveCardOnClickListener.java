package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.activities.EditCardActivity;
import android.view.View;
import android.view.View.OnClickListener;

public class SaveCardOnClickListener implements OnClickListener {

	EditCardActivity activity;

	public SaveCardOnClickListener(EditCardActivity activity)
	{
		this.activity = activity;
	}

	@Override
	public void onClick(View v) {
		
		this.activity.SaveCard();
		
	}

}