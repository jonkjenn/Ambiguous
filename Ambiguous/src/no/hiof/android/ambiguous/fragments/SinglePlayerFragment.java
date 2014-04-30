package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.ai.AIController;
import no.hiof.android.ambiguous.datasource.SessionDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Player;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public class SinglePlayerFragment extends Fragment {
	private AIController aiController;
	// TODO: Possibly recode so don't need these as
	public int savedSessionId = -1;

	// We keep track of current Card the opponent has played so we can restore
	// the view on orientation changes and pauses.
	private Card currentOpponentCard;
	private boolean opponentCardIsDiscarded = false;

	SessionDataSource sds;

	@Override
	public void onStart() {
		super.onStart();
		sds = new SessionDataSource((Db.getDb(getActivity()
				.getApplicationContext()).getWritableDatabase()));
		setupController();

	}

	@Override
	public void onResume() {
		super.onResume();
		// This should only happen when user push the resume button on the main
		// menu
		if (getArguments() != null) {
			resumeGame(getArguments());
		}
		GameActivity.gameMachine.setTurnChangeListener(aiController);

		GameActivity.gameMachine.startGame(GameActivity.gameMachine.state);
	}

	void setupController() {
		if (aiController == null) {
			aiController = new AIController(GameActivity.gameMachine.opponent,
					GameActivity.gameMachine.player,
					GameActivity.opponentController);
		}
	}

	/**
	 * Sets up for unpacking the data for this session from the bundle we have
	 * received as an argument.
	 * 
	 * @param extras
	 */
	private void resumeGame(Bundle extras) {
		if (extras == null) {
			return;
		} else if (!(extras.containsKey("SessionId"))) {
			return;
		} else {
			loadSessionStateBundle(extras);
			// we emulate the opponent using the previous card so that it
			// appears
			// on the game screen
			if (currentOpponentCard != null) {
				GameActivity.opponentController.previousCardPlayed(
						currentOpponentCard, opponentCardIsDiscarded);
			}
			if (extras.containsKey("CheatUsed")) {
				GameActivity.gameMachine.cheatUsed = extras
						.getBoolean("CheatUsed");
			}
		}
	}

	/**
	 * Loads session state from bundle into the game objects.
	 * 
	 * @param extras
	 */
	void loadSessionStateBundle(Bundle extras) {
		savedSessionId = extras.getInt("SessionId");
		GameActivity.gameMachine.player.updatePlayer((Player) extras
				.get("SessionPlayer"));
		GameActivity.gameMachine.opponent.updatePlayer((Player) extras
				.get("SessionOpponent"));
		GameActivity.gameMachine.state = GameMachine.State.values()[extras
				.getInt("SessionTurn")];
		currentOpponentCard = extras.getParcelable("SessionOpponentCard");
		opponentCardIsDiscarded = extras.getBoolean("SessionOpponentDiscard");
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (GameActivity.gameMachine != null) {
			outState.putParcelable("Player", GameActivity.gameMachine.player);
			outState.putParcelable("Opponent",
					GameActivity.gameMachine.opponent);
			outState.putInt("State", GameActivity.gameMachine.state.ordinal());
			outState.putParcelable("OpponentCard",
					GameActivity.gameMachine.currentOpponentCard);
			outState.putBoolean("OpponentCardDiscarded",
					GameActivity.gameMachine.opponentCardIsDiscarded);
			outState.putInt("Session", savedSessionId);
			outState.putBoolean("CheatUsed", GameActivity.gameMachine.cheatUsed);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		GameActivity.gameMachine.removeTurnChangeListener(aiController);

		if (savedSessionId != -1) {
			sds.setSessionId(savedSessionId);
		}

		final GameMachine.State state = GameActivity.gameMachine.state;

		// Deletes the session if the game has finished, we only keep track of
		// one session.
		if (state == GameMachine.State.GAME_OVER) {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					sds.deleteAllSessions();
					return null;
				}
			}.execute();
		} else {
			// If gameMachine exists, and the game is not finished, attempt
			// to save the current session in the database
			new AsyncTask<Void, Void, Boolean>() {

				@Override
				protected Boolean doInBackground(Void... params) {
					return sds
							.saveSession(
									state.ordinal(),
									GameActivity.gameMachine.player,
									GameActivity.gameMachine.opponent,
									(currentOpponentCard != null ? currentOpponentCard.id
											: -1), opponentCardIsDiscarded,
									GameActivity.gameMachine.cheatUsed);

				}

				@Override
				protected void onPostExecute(Boolean result) {
					super.onPostExecute(result);
					if (!result) {
						Log.d("sds.saveSession",
								"Something caused saveSession to fail");

						Toast.makeText(getActivity(), R.string.save_error,
								Toast.LENGTH_LONG).show();
					}
				}
			}.execute();

		}

	}

}
