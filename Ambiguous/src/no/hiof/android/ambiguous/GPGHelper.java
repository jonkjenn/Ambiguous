package no.hiof.android.ambiguous;

import java.util.ArrayList;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

public class GPGHelper {

	/**
	 * Returns current player's participant Id
	 * 
	 * @return
	 */
	public static String getMyId(GoogleApiClient apiClient, TurnBasedMatch match) {
		String myId = Games.Players.getCurrentPlayerId(apiClient);
		return (match.getParticipantId(myId));
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