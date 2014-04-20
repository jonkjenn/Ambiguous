package no.hiof.android.ambiguous.fragments;

import no.hiof.android.ambiguous.Db;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.GameMachine.State;
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
		if (getArguments() != null) {
			resumeGame(getArguments());
		}
		sds = new SessionDataSource((Db.getDb(getActivity()
				.getApplicationContext()).getWritableDatabase()));

		setupController();

		startGameMachine();
	}

	void startGameMachine() {
		GameActivity.gameMachine.startGame(GameActivity.gameMachine.state);
	}

	void setupController() {
		aiController = new AIController(GameActivity.gameMachine.opponent,
				GameActivity.gameMachine.player,
				GameActivity.opponentController);
		GameActivity.gameMachine.setTurnChangeListener(aiController);
	}

	private void resumeGame(Bundle extras) {
		if (extras == null) {
			return;
		} else if (!(extras.getInt("SessionId") >= 0)) {
			return;
		} else {
			savedSessionId = extras.getInt("SessionId");
			loadPlayer(GameActivity.gameMachine.player, (Player)extras.get("SessionPlayer"));
			loadPlayer(GameActivity.gameMachine.opponent, (Player) extras
					.get("SessionOpponent"));
			GameActivity.gameMachine.state = GameMachine.State.values()[extras
					.getInt("SessionTurn")];
			currentOpponentCard = extras.getParcelable("SessionOpponentCard");
			opponentCardIsDiscarded = extras
					.getBoolean("SessionOpponentDiscard");
			if (currentOpponentCard != null) {
				GameActivity.opponentController.previousCardPlayed(
						currentOpponentCard, opponentCardIsDiscarded);
			}
		}
	}
	
	//TODO: Find a better way to do this?
	/**
	 * We dont create new instance of player, instead we update current player. 
	 * @param target
	 * @param data
	 */
	void loadPlayer(Player target, Player data)
	{
		target.updatePlayer(data.name, data.health, data.armor, data.resources, data.hand, data.deck);
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
			// TODO: Implementing storing state to database so can resume even
			// if
			// app is destroyed.
			// save sessionid?
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (savedSessionId != -1) {
			sds.setSessionId(savedSessionId);
		}
		GameMachine.State state = GameActivity.gameMachine.state;
		// Deletes the session if the game has finished
		if (state == GameMachine.State.GAME_OVER) {
			sds.deleteAllSessions();
		} else {
			// If gameMachine exists, and the game is not finished, attempt
			// to save
			// the current session in the database
			Boolean saveSucessful = sds
					.saveSession(
							state.ordinal(),
							GameActivity.gameMachine.player,
							GameActivity.gameMachine.opponent,
							(currentOpponentCard != null ? currentOpponentCard.id
									: -1), opponentCardIsDiscarded);
			if (!saveSucessful)
				Log.d("sds.saveSession", "Something caused saveSession to fail");
		}

	}

}
