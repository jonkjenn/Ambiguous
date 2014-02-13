package no.hiof.android.ambiguous.floatingtext;

import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import android.widget.TextView;

public class RemoveFloatHandler extends Handler{
	TextView textView;

	public RemoveFloatHandler(TextView textView)
	{
		this.textView = textView;
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		((ViewGroup)this.textView.getParent()).removeView(this.textView);
	}
}
