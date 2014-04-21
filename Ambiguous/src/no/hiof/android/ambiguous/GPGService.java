package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.activities.GameActivity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

public class GPGService extends Service {

	final GPGBinder binder = new GPGBinder();
	GPGCallbackInterface callback;

	static String CLOSE = "CLOSE_SERVICE";

	GoogleApiClient gApiClient;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getAction() != null && intent.getAction().equals(CLOSE)) {
			stopSelf();
			return 0;
		}

		/*new Thread(new Runnable() {

			@Override
			public void run() {
				mainLoop();
			}
		}, "no.hiof.ambiguous.GPGService").start();*/
		mainLoop();
		return super.onStartCommand(intent, flags, startId);
	}

	private void mainLoop() {
		startForeground(1, buildNotification());

	}

	Notification buildNotification() {
		Intent ni = new Intent(this, GameActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, ni, 0);

		Intent close = new Intent(this, GPGService.class);
		close.setAction(CLOSE);
		PendingIntent pClose = PendingIntent.getService(this, 0, close, 0);

		Notification n = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.smiley_drawing_small)
				.setContentTitle("Ambiguous")
				.setContentText("Ambiguous")
				.addAction(R.drawable.abc_ic_clear,
						getResources().getText(R.string.close_service), pClose)
				.setContentIntent(pi).build();
		return n;
	}

	void setGPGListener() {
		Games.TurnBasedMultiplayer.registerMatchUpdateListener(gApiClient,
				new OnTurnBasedMatchUpdateReceivedListener() {

					@Override
					public void onTurnBasedMatchRemoved(String arg0) {
						// TODO Handle this.
					}

					@Override
					public void onTurnBasedMatchReceived(TurnBasedMatch match) {
						Log.d("test", "Found match update");
						notifyTurnBasedMatchReceived(match);
					}
				});
		Games.Invitations.registerInvitationListener(gApiClient,
				new OnInvitationReceivedListener() {

					@Override
					public void onInvitationRemoved(String arg0) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onInvitationReceived(Invitation inv) {
						Log.d("test", "Got invitation");

						Intent i = new Intent(GPGService.this,
								GameActivity.class);
						i.putExtra("useGPGS", true);
						i.putExtra("acceptInvitation", inv.getInvitationId());
						PendingIntent accept = PendingIntent.getActivity(
								getBaseContext(), 0, i, 0);

						Intent di = new Intent(GPGService.this,
								GameActivity.class);
						di.putExtra("useGPGS", true);
						di.putExtra("denieInvitation", inv.getInvitationId());
						PendingIntent denie = PendingIntent.getActivity(
								getBaseContext(), 0, i, 0);

						new NotificationCompat.Builder(GPGService.this)
								.setContentTitle("Ambiguous")
								.setSmallIcon(R.drawable.smiley_drawing_small)
								.setContentText(
										String.format(
												getResources()
														.getString(
																R.string.got_invite_from),
												inv.getInviter()
														.getDisplayName()))
								.addAction(
										R.drawable.pistol1,
										getResources().getText(R.string.accept),
										accept)
								.addAction(R.drawable.plus_drawing,
										getResources().getText(R.string.denie),
										denie).build();
					}
				});
	}

	public class GPGBinder extends Binder {

		public void setActivityCallback(GPGCallbackInterface callback) {
			GPGService.this.callback = callback;
		}

		public void setGoogleApiClient(GoogleApiClient client) {
			GPGService.this.gApiClient = client;
			setGPGListener();
		}

		public void setGPGServiceListener(GPGServiceListner l) {
			GPGService.this.gPGServiceListner = l;
		}
	}

	public interface GPGServiceListner {
		public void onTurnBasedMatchReceived(TurnBasedMatch match);
	}

	GPGServiceListner gPGServiceListner;

	void notifyTurnBasedMatchReceived(TurnBasedMatch match) {
		gPGServiceListner.onTurnBasedMatchReceived(match);
	}
}
