package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.ai.AIController;
import no.hiof.android.ambiguous.datasource.SessionDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Player;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

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

	private void resumeGame(Bundle extras) {
		if (extras == null) {
			return;
		} else if (!(extras.getInt("SessionId") >= 0)) {
			return;
		} else {
			loadSessionStateBundle(extras);
			if (currentOpponentCard != null) {
				GameActivity.opponentController.previousCardPlayed(
						currentOpponentCard, opponentCardIsDiscarded);
			}
			if (extras.getBoolean("CheatUsed")) {
				GameActivity.gameMachine.cheatUsed = extras
						.getBoolean("CheatUsed");
			}
		}
	}

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
			// We store stuff so that can resume later.
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
		GameMachine.State state = GameActivity.gameMachine.state;
		// Deletes the session if the game has finished
		if (state == GameMachine.State.GAME_OVER) {
			sds.deleteAllSessions();
		} else {
			// If gameMachine exists, and the game is not finished, attempt
			// to save the current session in the database
			Boolean saveSucessful = sds
					.saveSession(
							state.ordinal(),
							GameActivity.gameMachine.player,
							GameActivity.gameMachine.opponent,
							(currentOpponentCard != null ? currentOpponentCard.id
									: -1), opponentCardIsDiscarded,
							GameActivity.gameMachine.cheatUsed);
			if (!saveSucessful)
				Log.d("sds.saveSession", "Something caused saveSession to fail");
		}

	}

}
