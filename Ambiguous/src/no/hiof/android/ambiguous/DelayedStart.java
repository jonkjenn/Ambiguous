package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.activities.GameActivity;
import android.os.Handler;

public class DelayedStart {

	int checkCount = 0;
	final int checkMax = 5;

	public DelayedStart(OnReadyTostartListener listener) {
		this.onReadyToStartListener = listener;
		checkLoop();
	}

	void check() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				checkLoop();
			}
		}, 200);
	}

	void checkLoop() {
		checkCount++;
		if (checkCount == checkMax) {
			onReadyToStartListener.gaveUp();
		} else if (GameActivity.gameMachine == null
				|| GameActivity.gameMachine.player == null
				|| GameActivity.gameMachine.opponent == null) {
			check();
		} else {
			onReadyToStartListener.startLoad();
		}
	}

	public interface OnReadyTostartListener {
		void startLoad();

		void gaveUp();
	}

	OnReadyTostartListener onReadyToStartListener;

}