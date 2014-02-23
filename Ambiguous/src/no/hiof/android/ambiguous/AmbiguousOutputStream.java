package no.hiof.android.ambiguous;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class AmbiguousOutputStream extends DataOutputStream{

	public AmbiguousOutputStream(OutputStream out) {
		super(out);
	}
}
