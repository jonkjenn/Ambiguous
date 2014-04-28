package no.hiof.android.ambiguous;

import java.util.ArrayList;

import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.model.Player;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

public class GPGHelper {

	/**
	 * So we can decide if the effect target is Player or Opponent.
	 * 
	 * @param i
	 *            Can be 0 or 1, 0 being the creator.
	 * @return The target Player
	 */
	public static Player intToPlayer(int i, boolean isCreator) {

		return (i == 0 && isCreator) || (i == 1 && !isCreator) ? GameActivity.gameMachine.player
				: GameActivity.gameMachine.opponent;
	}

	public static int[] getIds(GoogleApiClient apiClient, TurnBasedMatch match) {
		if (GPGHelper.getMyId(apiClient, match).equals("p_1")) {
			return new int[] { 1, 2 };
		} else {
			return new int[] { 2, 1 };
		}
	}

	public static int getTargetId(GoogleApiClient apiClient, TurnBasedMatch match, Player target)
	{
		int[] ids = getIds(apiClient, match);
		return target == GameActivity.gameMachine.player?ids[0]:ids[1];
	}
	
	public static Player getTarget(GoogleApiClient apiClient, TurnBasedMatch match, int id)
	{
		int[] ids = getIds(apiClient, match);
		return id == ids[0]?GameActivity.gameMachine.player:GameActivity.gameMachine.opponent;
	}

	/*
	 * if(i == 0 && isCreator) { return GameActivity.gameMachine.player; } else
	 * if(i == 0 && !isCreator) { return GameActivity.gameMachine.opponent; }
	 * else if(i == 1 && isCreator) { return GameActivity.gameMachine.opponent;
	 * } else//No other alternatives should come if(i == 1 && !isCreator) {
	 * return GameActivity.gameMachine.player; }
	 */

	/**
	 * Convert from player to creator or not creator (0 or 1).
	 * 
	 * @param isPlayer
	 *            If the target is the GameMachine.Player
	 * @param isCreator
	 *            If the caster is the match creator.
	 * @return Either 0 or 1, 0 being the creator.
	 */
	public static int playerToInt(boolean isPlayer, boolean isCreator) {
		return isCreator && isPlayer || !isCreator && !isPlayer ? 0 : 1;
	}

	/*
	 * if(isCreator && isPlayer) { return 0; } else if(isCreator && !isPlayer) {
	 * return 1; } else if(!isCreator && isPlayer) { return 1; } else { return
	 * 0; }
	 */

	public static boolean isCreator(GoogleApiClient apiClient,
			TurnBasedMatch match) {

		return getMyId(apiClient, match).equals(match.getCreatorId());
	}

	/**
	 * Returns current player's participant Id
	 * 
	 * @return
	 */
	public static String getMyId(GoogleApiClient apiClient, TurnBasedMatch match) {
		return (match.getParticipantId(Games.Players
				.getCurrentPlayerId(apiClient)));
	}

	/**
	 * Finds the opponents Id
	 * 
	 * @param match
	 *            The current match
	 * @return Opponents Id
	 */
	public static String getOpponentId(GoogleApiClient apiClient,
			TurnBasedMatch match) {
		String myParticipantId = getMyId(apiClient, match);
		ArrayList<String> participantIds = match.getParticipantIds();

		for (int i = 0; i < participantIds.size(); i++) {
			if (!participantIds.get(i).equals(myParticipantId)) {
				return participantIds.get(i);
			}
		}
		Log.d("test", "This should never happen");

		return null;
	}
}