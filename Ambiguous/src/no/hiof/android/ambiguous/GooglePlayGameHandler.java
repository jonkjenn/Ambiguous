package no.hiof.android.ambiguous;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import no.hiof.android.ambiguous.GameMachine.GameMachineListener;
import no.hiof.android.ambiguous.GameMachine.State;
import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.UpdateMatchResult;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;

/**
 * Handles everything with the Google Play Game Services. The separation between
 * this class and the GameActivity class is done like this because this class
 * was introduced very late and we want to avoid more clutter in GameActivity.
 */
public class GooglePlayGameHandler implements GameMachineListener {

	GameActivity gameActivity;
	GameMachine gameMachine;
	boolean useGPGS;
	GameHelper gameHelper;

	TurnBasedMatch match;

	public boolean isCreator = false;

	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_INVITATION_BOX = 10001;
	final static int RC_RESOLVE = 9001;

	GoogleApiClient gApiClient;
	boolean explicitSignOut = false;
	boolean inSignInFlow = false;
	boolean expectingResult = false;

	ConnectionResult connectionResult;

	OpponentController oc;

	final byte PLAYED_CARD = 100;
	final byte DISCARDED_CARD = 101;
	final byte DEAD = 102;
	final byte NO_ACTION = 103;

	// The effects the current player has used
	ArrayList<Integer> effects = new ArrayList<Integer>();
	// The card the current player has played or discarded
	byte[] playedCard = new byte[] { NO_ACTION, 0 };

	public GooglePlayGameHandler(GameActivity gameActivity) {
		this.gameActivity = gameActivity;

		// Googl created helper class for helping with signing into the Google
		// service etc
		gameHelper = new GameHelper(gameActivity, GameHelper.CLIENT_ALL);

		// The button that display a list of active games and game invites. Game
		// inbox.
		Button b = (Button) gameActivity
				.findViewById(R.id.showActiveGamesButton);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showActiveGames();
			}
		});
		b.setVisibility(View.VISIBLE);

		// Button that loads interface for inviting another player to play
		// against you.
		b = (Button) gameActivity.findViewById(R.id.findOpponentButton);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				findOpponent();
			}
		});
		b.setVisibility(View.VISIBLE);

		if (BuildConfig.DEBUG) {
			gameHelper.enableDebugLog(true);
		}

		// Should probably handle the callbacks from this better
		// TODO: Handle this better
		GameHelperListener listener = new GameHelperListener() {

			@Override
			public void onSignInSucceeded() {
				Log.d("test", "Log in success");
				Games.TurnBasedMultiplayer.registerMatchUpdateListener(
						gameHelper.getApiClient(),
						new OnTurnBasedMatchUpdateReceivedListener() {

							@Override
							public void onTurnBasedMatchRemoved(String arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onTurnBasedMatchReceived(
									TurnBasedMatch match) {
								startGame(match);
							}
						});
			}

			@Override
			public void onSignInFailed() {
				Log.d("test", "Log in fail");
			}
		};

		gameHelper.setup(listener);
	}

	/**
	 * Starts Google's activity for showing active games and invites to new
	 * games.
	 */
	void showActiveGames() {
		if(gameHelper.getApiClient().isConnected())
		{
		Intent i = Games.TurnBasedMultiplayer.getInboxIntent(gameHelper
				.getApiClient());
		gameActivity.startActivityForResult(i, RC_INVITATION_BOX);
		}
	}

	/**
	 * Starts Google's activity for inviting other people to play against you.
	 */
	void findOpponent() {
		Intent intent = Games.TurnBasedMultiplayer.getSelectOpponentsIntent(
				gameHelper.getApiClient(), 1, 1);
		GooglePlayGameHandler.this.gameActivity.startActivityForResult(intent,
				RC_SELECT_PLAYERS);
	}

	void resolveConnectionResult() {
		if (connectionResult != null && connectionResult.hasResolution()) {
			try {
				expectingResult = true;
				connectionResult.startResolutionForResult(gameActivity,
						RC_RESOLVE);
			} catch (SendIntentException e) {
				e.printStackTrace();
			}
		}

	}

	public void onStart() {
		gameHelper.onStart(gameActivity);
	}

	public void onStop() {
		gameHelper.onStop();
	}

	/**
	 * When receive results back from an activity we launch.
	 * 
	 * @param request
	 * @param response
	 * @param intent
	 */
	public void onActivityResult(int request, int response, Intent intent) {
		{
			// GameHelper handles some of the cases with sign-in's etc
			// automatically.
			gameHelper.onActivityResult(request, response, intent);
			if (request == RC_SELECT_PLAYERS) {
				handleInvitePlayerActivityResult(response, intent);
			} else if (request == RC_INVITATION_BOX) {
				handleGameInboxActivityResult(response, intent);
			}
		}
	}

	/**
	 * Handle results from visiting the game inbox.
	 * 
	 * @param response
	 * @param intent
	 */
	void handleGameInboxActivityResult(int response, Intent intent) {
		// TODO: Handle this.
		if (response != Activity.RESULT_OK) {
			return;
		}

		/**
		 * Load the match from the intent.
		 */
		match = intent.getExtras().getParcelable(
				Multiplayer.EXTRA_TURN_BASED_MATCH);

		if (match != null) {
			if (match.getData() == null) {
				startNewGame(match);// Not sure if this will ever happen.
			} else {

				State state;
				// Find out if its our turn or opponents turn.
				if (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {
					state = State.PLAYER_TURN;
				} else {
					state = State.OPPONENT_TURN;
				}
				gameActivity.setupGameMachine(state);
				readGameState(match);// Loads the data from the bytes passed in
										// the intent/match into our actual game
										// objects ingame.
				gameMachine.startGame();// Start the actual game.
			}
		}

	}

	/**
	 * Handle results from visiting the activity for inviting opponents.
	 * 
	 * @param response
	 * @param intent
	 */
	void handleInvitePlayerActivityResult(int response, Intent intent) {
		// TODO: Handle this.
		if (response != Activity.RESULT_OK) {
			return;
		}

		final ArrayList<String> opponents = intent
				.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);// Load a list
																	// of
																	// opponents,
																	// should
																	// only be
																	// one for
																	// us.

		// We dont use these settings, yet.
		Bundle autoMatchCriteria = null;
		int minAutoMatchPlayers = intent.getIntExtra(
				Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
		int maxAutoMatchPlayers = intent.getIntExtra(
				Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

		if (minAutoMatchPlayers > 0) {
			autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
					minAutoMatchPlayers, maxAutoMatchPlayers, 0);
		} else {
			autoMatchCriteria = null;
		}

		// We want to create a turn based match
		TurnBasedMatchConfig mc = TurnBasedMatchConfig.builder()
				.addInvitedPlayers(opponents)
				.setAutoMatchCriteria(autoMatchCriteria).build();

		Games.TurnBasedMultiplayer
				.createMatch(gameHelper.getApiClient(), mc)
				.setResultCallback(
						new ResultCallback<TurnBasedMultiplayer.InitiateMatchResult>() {

							@Override
							public void onResult(InitiateMatchResult result) {
								// Dont start if theres an error in the connect
								// status.
								if (!checkStatusCode(match, result.getStatus()
										.getStatusCode())) {
									return;
								}
								match = result.getMatch();
								startGame(match);
							}
						});

	}

	/**
	 * Starts the turnbased game.
	 * 
	 * @param result
	 */
	void startGame(TurnBasedMatch match) {
		
		gameActivity.resetLayout();

		if (match.getStatus() == TurnBasedMatch.MATCH_STATUS_ACTIVE
				&& match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {

			// Game is already started
			if (match.getData() != null) {
				// Have to start our gamemachine if havent.
				if (gameMachine == null) {
					gameActivity.setupGameMachine(null);
				}
				// Handle the game status we receive
				readGameState(match);
				// Notify that opponents turn is completed. Now we can make our
				// turn.
				oc.TurnDone();
			} else {
				// The game has not started so we set it up and decides who
				// should have the starting turn.
				startNewGame(match);
			}
		} else {
			// TODO:Handle this better.
			Log.d("test", "Something wrong: " + match.getStatus());

		}
	}

	void startNewGame(TurnBasedMatch match) {
		gameActivity.setupGameMachine(null);// We start a new game and randomly
											// decides who will start.
		if (gameMachine.state == State.OPPONENT_TURN) {// In this case we skip
														// our turn since the
														// creator always will
														// have first turn.
			completeTurn();
		}
	}

	/**
	 * Finds the opponents Id
	 * 
	 * @param match
	 *            The current match
	 * @return Opponents Id
	 */
	String getOpponentId(TurnBasedMatch match) {
		String myId = Games.Players.getCurrentPlayerId(gameHelper
				.getApiClient());
		String myParticipantId = match.getParticipantId(myId);
		ArrayList<String> participantIds = match.getParticipantIds();

		for (int i = 0; i < participantIds.size(); i++) {
			if (!participantIds.get(i).equals(myParticipantId)) {
				return participantIds.get(i);
			}
		}

		Log.d("test", "This should never happen");

		return null;
	}

	public void setOpponentController(OpponentController oc) {
		this.oc = oc;
	}

	public void setGameMachine(GameMachine gameMachine) {
		this.gameMachine = gameMachine;
	}

	void readGameState(TurnBasedMatch match) {
		// If empty we assume it is our turn to play. Can happen if the host has
		// to pass the turn to the other player at first turn.
		if (match.getData() == null || match.getData().length == 0) {
			return;
		}

		int index = 0;
		
		ByteArrayInputStream stream = new ByteArrayInputStream(match.getData());
		
		DataInputStream r = new DataInputStream(stream);

		// 1 byte type of action
		/*switch (match.getData()[index++]) {
		// 1 byte card id
		case PLAYED_CARD:
			oc.PlayCard(match.getData()[index++], false);
			break;
		case DISCARDED_CARD:
			oc.DiscardCard(match.getData()[index++]);
			break;
		case DEAD:
			index++;
			break;
		case NO_ACTION:
			index++;
			break;
		}*/
		
		try
		{
		
		switch(r.readByte())
		{
		case PLAYED_CARD:
			oc.PlayCard(r.readByte(), false);
		case DISCARDED_CARD:
			oc.DiscardCard(r.readByte());
		case NO_ACTION:
			r.skipBytes(1);
			break;
		}

/*		// 1 byte number of effect bytes
		index++;
		for (int i = 0; i < match.getData()[2]; i += 3) {
			// So we dont try to go outside the array size
			if (index + 2 >= match.getData().length) {
				break;
			}
			// 3 bytes for each effect, effect type, target and amount
			oc.UseEffect(Effect.EffectType.values()[match.getData()[index++]],
					(match.getData()[index++] == 1 ? gameActivity.opponent
							: gameActivity.player), match.getData()[index++]);
		}*/

		for (int i = 0; i < r.readInt(); i++ ) {
			// 3 bytes for each effect, effect type, target and amount
			oc.UseEffect(Effect.EffectType.values()[r.readInt()],
					(r.readInt() == 1 ? gameActivity.opponent
							: gameActivity.player), r.readInt());
		};

		Card[] hand = new Card[8];
		// 8 cards on creators hand
		for (int i = 0; i < 8; i++) {
		//	hand[i] = CardDataSource.getCard(match.getData()[index++]);
			hand[i] = CardDataSource.getCard(r.readInt());
			
		}

		if (match.getCreatorId().equals(getOpponentId(match))) {
			gameActivity.opponent.setHand(hand);
		} else {
			gameActivity.player.setHand(hand);
		}

		hand = new Card[8];
		// 8 cards on invited players hand
		for (int i = 0; i < 8; i++) {
			//hand[i] = CardDataSource.getCard(match.getData()[index++]);
			hand[i] = CardDataSource.getCard(r.readInt());
		}

		Player creator;
		Player other;
		if (match.getCreatorId().equals(getOpponentId(match))) {
			gameActivity.player.setHand(hand);
			creator = gameMachine.opponent;
			other = gameMachine.player;
		} else {
			gameActivity.opponent.setHand(hand);
			creator = gameMachine.player;
			other = gameMachine.opponent;
		}
		
		//Read the players's stats

/*		creator.setHealth(match.getData()[index++]);
		creator.setArmor(match.getData()[index++]);
		creator.setResources(match.getData()[index++]);

		other.setHealth(match.getData()[index++]);
		other.setArmor(match.getData()[index++]);
		other.setResources(match.getData()[index++]);*/

		creator.setHealth(r.readInt());
		creator.setArmor(r.readInt());
		creator.setResources(r.readInt());

		other.setHealth(r.readInt());
		other.setArmor(r.readInt());
		other.setResources(r.readInt());
		}catch(IOException e)
		{
			Log.d("test", "Error reading data from google");
		}
	}
	

	/**
	 * Writes the current players choices to a byte array.
	 * 
	 * @return
	 */
	byte[] writeGameState(TurnBasedMatch match) {
		
		
		// action type, played card, effects count, effects, action and played card, creator hand, other
		// hand, creator stats, other stats
		int size = 2 + 1 + effects.size() + playedCard.length + 8 + 8 + 3 + 3;

		ByteArrayOutputStream stream = new ByteArrayOutputStream(size);
		
		DataOutputStream w = new DataOutputStream(stream);

		int index = 0;

		// Write what action the player is doing on which card.
		
		try
		{
		w.write(playedCard[0]);
		w.write(playedCard[1]);
		
		//b[index++] = playedCard[0];
		//b[index++] = playedCard[1];

		//b[index++] = (byte) effects.size();
		w.writeInt(effects.size());
		// Write all the effects the player is applying to himself and opponent.
		for (int i = 0; i < effects.size(); i++) {
			//b[index++] = effects.get(i);
			w.writeInt(effects.get(i));
		}

		// Creators hand
		Card[] creator;
		Card[] other;
		Player pCreator;
		Player pOther;
		// We always write the creator stats/cards first so check
		// who is the creator
		if (getOpponentId(match).equals(match.getCreatorId())) {
			creator = gameActivity.opponent.getHand();
			other = gameActivity.player.getHand();
			pCreator = gameMachine.opponent;
			pOther = gameMachine.player;
		} else {
			creator = gameActivity.player.getHand();
			other = gameActivity.opponent.getHand();
			pCreator = gameMachine.player;
			pOther = gameMachine.opponent;
		}

		// Write the cards to the byte array
		for (int i = 0; i < creator.length; i++) {
			//b[index++] = (byte) creator[i].getId();
			//b[index + 7] = (byte) other[i].getId();
			w.writeInt(creator[i].getId());
		}

		for (int i = 0; i < other.length; i++) {
			//b[index++] = (byte) creator[i].getId();
			//b[index + 7] = (byte) other[i].getId();
			w.writeInt(other[i].getId());
		}
		
		index+=8;//The 8 "extra" cards we just added

		// TODO: We are now limited to 1 byte for each stats, should fix this.
		// Atm it could crash.
		// Write the stats to the byte array
		/*b[index++] = (byte) pCreator.getHealth();
		b[index++] = (byte) pCreator.getArmor();
		b[index++] = (byte) pCreator.getResources();
		b[index++] = (byte) pOther.getHealth();
		b[index++] = (byte) pOther.getArmor();
		b[index++] = (byte) pOther.getResources();*/

		w.writeInt(pCreator.getHealth());
		w.writeInt(pCreator.getArmor());
		w.writeInt(pCreator.getResources());
		w.writeInt(pOther.getHealth());
		w.writeInt(pOther.getArmor());
		w.writeInt(pOther.getResources());
		}catch(IOException e)
		{
			Log.d("test", "Error creating bytes for google");
		}

		effects.clear();

		return stream.toByteArray();
	}

	/**
	 * Set what card player has used or discarded.
	 * 
	 * @param c
	 *            The card used or discarded.
	 */
	void playerUsedCard(Card c, boolean discard) {
		playedCard[0] = (discard ? DISCARDED_CARD : PLAYED_CARD);
		playedCard[1] = (byte) c.getId();
	}

	/**
	 * Add an effect the player has current used.
	 * 
	 * @param e
	 *            The effect type used.
	 * @param target
	 *            Target of the effect.
	 * @param amount
	 *            Effect amount.
	 */
	void playerUsedEffect(EffectType e, Player target, int amount) {
		effects.add( e.ordinal());
		effects.add(target == gameActivity.opponent ?  0 : 1);
		effects.add( amount);
	}

	void showErrorMessage(TurnBasedMatch match, int statuscode, int error) {
		gameActivity.showGenericDialog("Error " + statuscode, gameActivity
				.getResources().getString(error));
	}

	/**
	 * Taken from google's example code.
	 * 
	 * // Returns false if something went wrong, probably. This should handle //
	 * more cases, and probably report more accurate results.
	 * 
	 * @param match
	 * @param statusCode
	 * @return
	 */
	private boolean checkStatusCode(TurnBasedMatch match, int statusCode) {
		switch (statusCode) {
		case GamesStatusCodes.STATUS_OK:
			return true;
		case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_DEFERRED:
			// This is OK; the action is stored by Google Play Services and will
			// be dealt with later.
			return true;
		case GamesStatusCodes.STATUS_MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
			showErrorMessage(match, statusCode,
					R.string.status_multiplayer_error_not_trusted_tester);
			break;
		case GamesStatusCodes.STATUS_MATCH_ERROR_ALREADY_REMATCHED:
			showErrorMessage(match, statusCode,
					R.string.match_error_already_rematched);
			break;
		case GamesStatusCodes.STATUS_NETWORK_ERROR_OPERATION_FAILED:
			showErrorMessage(match, statusCode,
					R.string.network_error_operation_failed);
			break;
		case GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED:
			showErrorMessage(match, statusCode,
					R.string.client_reconnect_required);
			break;
		case GamesStatusCodes.STATUS_INTERNAL_ERROR:
			showErrorMessage(match, statusCode, R.string.internal_error);
			break;
		case GamesStatusCodes.STATUS_MATCH_ERROR_INACTIVE_MATCH:
			showErrorMessage(match, statusCode,
					R.string.match_error_inactive_match);
			break;
		case GamesStatusCodes.STATUS_MATCH_ERROR_LOCALLY_MODIFIED:
			showErrorMessage(match, statusCode,
					R.string.match_error_locally_modified);
			break;
		case GamesStatusCodes.STATUS_MATCH_ERROR_INVALID_PARTICIPANT_STATE:
			showErrorMessage(match, statusCode,
					R.string.match_error_invalid_participant_state);

		default:
			showErrorMessage(match, statusCode, R.string.unexpected_status);
			Log.d("test", "Did not have warning or string to deal with: "
					+ statusCode);
		}

		return false;
	}

	/**
	 * Completes a turn and sends the data to Google.
	 */
	void completeTurn() {

		String matchId = match.getMatchId();// The Id for our match
		String pendingParticipant = getOpponentId(match);// We set who's turn it
															// is next since
															// we're done,
															// should always be
															// opponents turn
															// for us.
		byte[] gameState = writeGameState(match);// We build the game state
													// bytes from the current
													// game state.

		// This actually tries to send our data to Google.
		Games.TurnBasedMultiplayer.takeTurn(gameHelper.getApiClient(), matchId,
				gameState, pendingParticipant).setResultCallback(
				new ResultCallback<TurnBasedMultiplayer.UpdateMatchResult>() {

					@Override
					public void onResult(UpdateMatchResult result) {
						// TODO:Handle this better
						if (!GooglePlayGameHandler.this.checkStatusCode(match,
								result.getStatus().getStatusCode())) {
							Log.d("test", result.getStatus().getStatusCode()
									+ " Something went wrong");
						}
					}
				});

	}

	@Override
	public void onCouldNotPlayCardListener(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerTurnListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerDoneListener() {
		Log.d("test", "Player done");
		completeTurn();
	}

	@Override
	public void onOpponentTurnListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerDeadListener(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onOpponentDeadListener(Player opponent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerPlayedCard(Card card) {
		playerUsedCard(card, false);
	}

	@Override
	public void onPlayerUsedeffect(EffectType type, Player target, int amount) {
		playerUsedEffect(type, target, amount);
	}

	@Override
	public void onPlayerDiscardCard(Card card) {
		playerUsedCard(card, true);
	}
}
