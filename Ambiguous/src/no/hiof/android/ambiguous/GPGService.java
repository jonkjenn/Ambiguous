package no.hiof.android.ambiguous;

import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.activities.MainActivity;
import android.app.Notification;
import android.app.NotificationManager;
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

	public static String CLOSE = "CLOSE_SERVICE";

	GoogleApiClient gApiClient;

	public static boolean isRunning = false;

	public GPGServiceListner gPGServiceListner;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		isRunning = true;

		// The close button on notification service
		if (intent != null && intent.getAction() != null && intent.getAction().equals(CLOSE)) {
			stopSelf();
			return 0;
		}

		mainLoop();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (gApiClient != null) {
			Games.TurnBasedMultiplayer
					.unregisterMatchUpdateListener(gApiClient);
			Games.Invitations.unregisterInvitationListener(gApiClient);
		}
		isRunning = false;
	}

	private void mainLoop() {
		startForeground(1, buildNotification());

	}

	Notification buildNotification() {
		Intent ni = new Intent(getApplicationContext(), MainActivity.class);
		ni.addCategory(Intent.CATEGORY_LAUNCHER);
		ni.setAction(Intent.ACTION_MAIN);
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

	/**
	 * We register listeners with the connection Google API Client so we can
	 * receive invitations and match updates even without game being open.
	 * 
	 * This is pretty much what the Google built in service does, but here we
	 * can tailor our notifications such that they bring us directly into the
	 * matches.
	 */
	void setGPGListener() {

		Games.TurnBasedMultiplayer.registerMatchUpdateListener(gApiClient,
				new OnTurnBasedMatchUpdateReceivedListener() {

					@Override
					public void onTurnBasedMatchRemoved(String arg0) {
						// TODO Handle this.
					}

					// Update to one of our matches received.
					@Override
					public void onTurnBasedMatchReceived(TurnBasedMatch match) {

						// If this check is true it should mean there's no game
						// open looking at matches.
						if (binder == null || gPGServiceListner == null) {
							Intent i = new Intent(GPGService.this,
									GameActivity.class);
							i.putExtra("useGPGS", true);
							i.putExtra("matchId", match.getMatchId());
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
							PendingIntent pi = PendingIntent.getActivity(
									getApplicationContext(), 0, i,
									PendingIntent.FLAG_UPDATE_CURRENT);

							Notification n = new NotificationCompat.Builder(
									getApplicationContext())
									.setSmallIcon(
											R.drawable.smiley_drawing_small)
									.setContentTitle(
											getResources().getString(
													R.string.your_turn))
									.setContentText(
											String.format(
													getResources()
															.getString(
																	R.string.match_waiting_for_turn),
													// We get the display name
													// for our opponent
													match.getParticipant(
															GPGHelper
																	.getOpponentId(
																			gApiClient,
																			match))
															.getDisplayName()))
									.setAutoCancel(true).setContentIntent(pi)
									.build();
							((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
									.notify(match.getMatchId(), 1, n);
							// TODO: Implement a check so that we only send
							// match updates to the game when the game has open
							// the exact MatchId we're receiving. Other match
							// updates we can post notifications for.
						} else {// If there's a game open listening for matches
								// we send the notification to that.
							notifyTurnBasedMatchReceived(match);
						}
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
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						PendingIntent accept = PendingIntent.getActivity(
								getBaseContext(), 0, i, 0);

						Intent di = new Intent(GPGService.this,
								GameActivity.class);
						di.putExtra("useGPGS", true);
						di.putExtra("denieInvitation", inv.getInvitationId());
						di.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

		/**
		 * Replaces any previous listener, can only be one at a time.
		 * 
		 * @param l
		 */
		public void setGPGServiceListener(GPGServiceListner l) {
			GPGService.this.gPGServiceListner = l;
		}
	}

	public interface GPGServiceListner {
		public void onTurnBasedMatchReceived(TurnBasedMatch match);
	}

	void notifyTurnBasedMatchReceived(TurnBasedMatch match) {
		if (gPGServiceListner != null) {
			gPGServiceListner.onTurnBasedMatchReceived(match);
		}
	}
}
