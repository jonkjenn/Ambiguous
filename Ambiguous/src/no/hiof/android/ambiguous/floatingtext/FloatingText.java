package no.hiof.android.ambiguous.floatingtext;

import no.hiof.android.ambiguous.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

public class FloatingText extends TextView{
	Context context;

	public FloatingText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void SetView(ViewGroup parent)
	{
		inflate(context,R.id.floatingtext,parent);
	}

}
