package no.hiof.android.ambiguous.cardlistener;

import no.hiof.android.ambiguous.activities.EditCardActivity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class CardOnClickListener implements OnClickListener{

	int cardId;
	Context context;
	public CardOnClickListener(Context context, int cardId) {
		this.cardId = cardId;
		this.context = context;
	}
	
	@Override
	public void onClick(View v) {
		Intent startEdit = new Intent(context, EditCardActivity.class);		
		startEdit.putExtra("id", this.cardId);
		context.startActivity(startEdit);
	}
}