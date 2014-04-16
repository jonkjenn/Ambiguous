package no.hiof.android.ambiguous.fragments;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import no.hiof.android.ambiguous.BuildConfig;
import no.hiof.android.ambiguous.GameMachine;
import no.hiof.android.ambiguous.GameMachine.GameMachineListener;
import no.hiof.android.ambiguous.GameMachine.State;
import no.hiof.android.ambiguous.LayoutHelper;
import no.hiof.android.ambiguous.OpponentController;
import no.hiof.android.ambiguous.R;
import no.hiof.android.ambiguous.activities.GameActivity;
import no.hiof.android.ambiguous.datasource.CardDataSource;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.InitiateMatchResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.LoadMatchesResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer.UpdateMatchResult;
import com.google.example.games.basegameutils.GameHelper;
import com.google.example.games.basegameutils.GameHelper.GameHelperListener;

/**
 * Handles everything with the Google Play Game Services. The separation between
 * this class and the GameActivity class is done like this because this class
 * was introduced very late and we want to avoid more clutter in GameActivity.
 */
public class GooglePlayGameFragment extends Fragment implements
		GameMachineListener, GameHelperListener, OnClickListener {

	GameMachine gameMachine;
	boolean useGPGS;
	GameHelper gameHelper;

	TurnBasedMatch match;

	public boolean isCreator = false;

	final static int RC_SELECT_PLAYERS = 10000;
	final static int RC_INVITATION_BOX = 10001;
	final static int RC_RESOLVE = 9001;

	TextView resultTextView;

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
	byte[] playedCard = new byte[] { NO_ACTION, 0, NO_ACTION, 0 };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState != null) {
			match = (TurnBasedMatch) savedInstanceState.getParcelable("match");
		}

		// Google created helper class for helping with signing into the Google
		// service etc
		gameHelper = new GameHelper(getActivity(), GameHelper.CLIENT_ALL);

		if (BuildConfig.DEBUG) {
			gameHelper.enableDebugLog(true);
		}

		gameHelper.setup(this);

		resultTextView = (TextView) getView().findViewById(R.id.result_text);

		// The button that display a list of active games and game invites. Game
		// inbox.
		Button b = (Button) getActivity().findViewById(
				R.id.showActiveGamesButton);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showActiveGames();
			}
		});
		b.setVisibility(View.VISIBLE);

		// Button that loads interface for inviting another player to play
		// against you.
		b = (Button) getActivity().findViewById(R.id.findOpponentButton);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				findOpponent();
			}
		});
		b.setVisibility(View.VISIBLE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("match", match);
	}

	@Override
	public void onStart() {
		super.onStart();

		gameHelper.onStart(getActivity());
	}

	@Override
	public void onStop() {
		super.onStop();
		gameHelper.onStop();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gpg, container, false);
	}

	void showConnected() {
		ImageView iv = (ImageView) getActivity().findViewById(R.id.logon_icon);
		getActivity().findViewById(R.id.sign_in_button)
				.setVisibility(View.GONE);
		getActivity().findViewById(R.id.sign_out_button).setVisibility(
				View.VISIBLE);
		iv.setImageDrawable(getActivity().getResources().getDrawable(
				android.R.drawable.presence_online));
	}

	void showDisconnected() {
		ImageView iv = (ImageView) getActivity().findViewById(R.id.logon_icon);
		getActivity().findViewById(R.id.sign_in_button).setVisibility(
				View.VISIBLE);
		getActivity().findViewById(R.id.sign_out_button).setVisibility(
				View.GONE);
		iv.setImageDrawable(getActivity().getResources().getDrawable(
				android.R.drawable.presence_offline));
	}

	/**
	 * Starts Google's activity for showing active games and invites to new
	 * games.
	 */
	void showActiveGames() {
		if (gameHelper.getApiClient().isConnected()) {
			Intent i = Games.TurnBasedMultiplayer.getInboxIntent(gameHelper
					.getApiClient());
			startActivityForResult(i, RC_INVITATION_BOX);
		} else if (gameHelper.isConnecting()) {
			Toast.makeText(getActivity(), "Please wait, connecting to google.",
					Toast.LENGTH_LONG);
		}
	}

	/**
	 * Starts Google's activity for inviting other people to play against you.
	 */
	void findOpponent() {
		if (gameHelper.isSignedIn()) {
			Intent intent = Games.TurnBasedMultiplayer
					.getSelectOpponentsIntent(gameHelper.getApiClient(), 1, 1);
			startActivityForResult(intent, RC_SELECT_PLAYERS);
		} else if (gameHelper.isConnecting()) {
			Toast.makeText(getActivity(), "Please wait, connecting to google.",
					Toast.LENGTH_LONG);
		}
	}

	void resolveConnectionResult() {
		if (connectionResult != null && connectionResult.hasResolution()) {
			try {
				expectingResult = true;
				connectionResult.startResolutionForResult(getActivity(),
						RC_RESOLVE);
			} catch (SendIntentException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * When receive results back from an activity we launch.
	 * 
	 * @param request
	 * @param response
	 * @param intent
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// GameHelper handles some of the cases with sign-in's etc
		// automatically.
		gameHelper.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_SELECT_PLAYERS) {
			handleInvitePlayerActivityResult(resultCode, data);
		} else if (requestCode == RC_INVITATION_BOX) {
			handleGameInboxActivityResult(resultCode, data);
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

		startGame(match);
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

		LayoutHelper.hideResult(resultTextView);

		State state;
		// Find out if its our turn or opponents turn.
		if (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {

			// Sort of a hack to correctly apply opponents actions before we
			// change to players turn.
			state = State.OPPONENT_TURN;
			((GameActivity) getActivity()).setupGameMachine(state);
			readGameState(match);// Loads the data from the bytes passed in
									// the intent/match into our actual game
									// objects in game.
			gameMachine.startGame();// First start the game with opponent having
									// turn.

			gameMachine.state = State.PLAYER_TURN;// Game will now later be
													// started again as player's
													// turn
		} else {
			state = State.OPPONENT_TURN;
			((GameActivity) getActivity()).setupGameMachine(state);
			readGameState(match);// Loads the data from the bytes passed in
									// the intent/match into our actual game
									// objects ingame.
		}

		this.gameMachine.removeGameMachineListener(this);
		this.gameMachine.setGameMachineListener(this);

		switch (match.getStatus()) {
		case TurnBasedMatch.MATCH_STATUS_ACTIVE:
			handleActiveMatch(match);
			break;
		case TurnBasedMatch.MATCH_STATUS_COMPLETE:
			gameMachine.startGame();
			break;
		}
	}

	void handleActiveMatch(TurnBasedMatch match) {
		if (match.getData() == null) {
			// The game has not started so we set it up and decides who
			// should have the starting turn.
			startNewGame(match);
		} else {
			gameMachine.startGame();// Start the actual game.
		}
	}

	void startNewGame(TurnBasedMatch match) {
		((GameActivity) getActivity()).setupGameMachine(null);// We start a new
																// game and
																// randomly
		// decides who will start.
		if (gameMachine.state == State.OPPONENT_TURN) {// In this case we skip
														// our turn since the
														// creator always will
														// have first turn.
			completeTurn();
		}
	}

	/**
	 * Returns current player's participant Id
	 * 
	 * @return
	 */
	String getMyId() {
		String myId = Games.Players.getCurrentPlayerId(gameHelper
				.getApiClient());
		return (match.getParticipantId(myId));
	}

	/**
	 * Finds the opponents Id
	 * 
	 * @param match
	 *            The current match
	 * @return Opponents Id
	 */
	String getOpponentId(TurnBasedMatch match) {
		String myParticipantId = getMyId();
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

		boolean isCreator = false;
		if (match.getCreatorId().equals(getOpponentId(match))) {
			isCreator = true;
		}

		ByteArrayInputStream stream = new ByteArrayInputStream(match.getData());

		DataInputStream r = new DataInputStream(stream);

		try {

			// Only want the action that the opponent has done so we move past
			// the first action
			if (isCreator) {
				r.skipBytes(2);
			}

			// 1. Read action type and card 2bytes
			switch (r.readByte()) {
			case PLAYED_CARD:
				oc.PlayCard(r.readByte(), false);
				break;
			case DISCARDED_CARD:
				oc.DiscardCard(r.readByte());
				break;
			case NO_ACTION:
				r.skipBytes(1);
				break;
			}

			// Skip these since we already loaded action from creator.
			if (!isCreator) {
				r.skipBytes(2);
			}

			// 3 bytes for each effect, effect type, target and amount

			int numEffects = r.readInt() / 3;
			// 2. read number of effects 1 int
			for (int i = 0; i < numEffects; i++) {
				// 3. Read effects, id, which player and amount 3 ints
				oc.UseEffect(Effect.EffectType.values()[r.readInt()], (r
						.readInt() == 1 ? gameMachine.opponent
						: gameMachine.player), r.readInt(), true);
			}

			Card[] hand = new Card[8];
			// 8 cards on creators hand
			// 4. read 8 cards, 8 ints
			for (int i = 0; i < 8; i++) {
				hand[i] = CardDataSource.getCard(r.readInt());
			}

			if (isCreator) {
				gameMachine.opponent.setHand(hand);
			} else {
				gameMachine.player.setHand(hand);
			}

			hand = new Card[8];
			// 8 cards on invited players hand
			// 5. read 8 cards, 8 ints
			for (int i = 0; i < 8; i++) {
				hand[i] = CardDataSource.getCard(r.readInt());
			}

			Player creator;
			Player other;

			if (isCreator) {
				gameMachine.player.setHand(hand);
				creator = gameMachine.opponent;
				other = gameMachine.player;
			} else {
				gameMachine.opponent.setHand(hand);
				creator = gameMachine.player;
				other = gameMachine.opponent;
			}

			// 6. read stats. 6 ints.
			creator.setHealth(r.readInt());
			creator.setArmor(r.readInt());
			creator.setResources(r.readInt());

			other.setHealth(r.readInt());
			other.setArmor(r.readInt());
			other.setResources(r.readInt());
		} catch (IOException e) {
			Log.d("test", "Error reading data from google");
		}
	}

	/**
	 * Writes the current players choices to a byte array.
	 * 
	 * @return
	 */
	byte[] writeGameState(TurnBasedMatch match) {

		boolean isCreator = false;
		if (getOpponentId(match).equals(match.getCreatorId())) {
			isCreator = true;
		}

		// action type, played card, effects count, effects, action and played
		// card, creator hand, other
		// hand, creator stats, other stats
		int size = 2 + 1 + effects.size() + playedCard.length + 8 + 8 + 3 + 3;

		// The size is just an estimate, the buffer will expand if needed
		ByteArrayOutputStream stream = new ByteArrayOutputStream(size);

		DataOutputStream w = new DataOutputStream(stream);

		// Write what action the player is doing on which card.
		try {

			// 1. Write action type and card 2bytes
			w.write(playedCard[0]);
			w.write(playedCard[1]);
			w.write(playedCard[2]);

			w.write(playedCard[3]);

			// 2. Write number of effects 1 int
			w.writeInt(effects.size());
			for (int i = 0; i < effects.size(); i++) {
				// 3. Write effects 3 int
				w.writeInt(effects.get(i));
			}

			Card[] creator, other;
			Player pCreator, pOther;

			// We always write the creator stats/cards first so check
			// who is the creator
			if (isCreator) {
				creator = gameMachine.opponent.getHand();
				other = gameMachine.player.getHand();
				pCreator = gameMachine.opponent;
				pOther = gameMachine.player;
			} else {
				creator = gameMachine.player.getHand();
				other = gameMachine.opponent.getHand();
				pCreator = gameMachine.player;
				pOther = gameMachine.opponent;
			}

			// 4. write 8 cards, 8 ints
			for (int i = 0; i < creator.length; i++) {
				w.writeInt(creator[i].id);
			}

			// 5. write 8 cards, 8 ints
			for (int i = 0; i < other.length; i++) {
				w.writeInt(other[i].id);
			}

			// 6. write stats, 6 of em.
			w.writeInt(pCreator.health);
			w.writeInt(pCreator.armor);
			w.writeInt(pCreator.resources);
			w.writeInt(pOther.health);
			w.writeInt(pOther.armor);
			w.writeInt(pOther.resources);
		} catch (IOException e) {
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
		// The creator data is in the 2 first bytes, the other player in the 2
		// last bytes.
		int index1;
		if (getOpponentId(match).equals(match.getCreatorId())) {
			index1 = 0;
		} else {
			index1 = 2;
		}
		playedCard[index1] = (discard ? DISCARDED_CARD : PLAYED_CARD);
		playedCard[index1 + 1] = (byte) c.id;
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
		effects.add(e.ordinal());
		effects.add(target == gameMachine.opponent ? 0 : 1);
		effects.add(amount);
	}

	void showErrorMessage(TurnBasedMatch match, int statuscode, int error) {
		GameActivity.showGenericDialog(getActivity(), "Error " + statuscode,
				getActivity().getResources().getString(error));
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
						if (!GooglePlayGameFragment.this.checkStatusCode(match,
								result.getStatus().getStatusCode())) {
							Log.d("test", result.getStatus().getStatusCode()
									+ " Something went wrong");
						}
					}
				});

	}

	/**
	 * Completes the match either by uploading the final results or by
	 * confirming the results.
	 */
	void finishMatch() {
		int playerResult = ParticipantResult.MATCH_RESULT_NONE;
		int opponentResult = ParticipantResult.MATCH_RESULT_NONE;

		if (!gameMachine.player.isAlive()) {
			LayoutHelper.showResult(resultTextView, false);
			playerResult = ParticipantResult.MATCH_RESULT_LOSS;
			opponentResult = ParticipantResult.MATCH_RESULT_WIN;
		} else if (!gameMachine.opponent.isAlive()) {
			LayoutHelper.showResult(resultTextView, true);
			playerResult = ParticipantResult.MATCH_RESULT_WIN;
			opponentResult = ParticipantResult.MATCH_RESULT_LOSS;
		}

		ArrayList<ParticipantResult> results = new ArrayList<ParticipantResult>();

		results.add(new ParticipantResult(getMyId(), playerResult,
				ParticipantResult.PLACING_UNINITIALIZED));

		results.add(new ParticipantResult(getOpponentId(match), opponentResult,
				ParticipantResult.PLACING_UNINITIALIZED));

		if (match.getStatus() == TurnBasedMatch.MATCH_STATUS_ACTIVE) {
			Games.TurnBasedMultiplayer.finishMatch(gameHelper.getApiClient(),
					match.getMatchId(), writeGameState(match), results);
		} else if (match.getStatus() == TurnBasedMatch.MATCH_STATUS_COMPLETE) {
			if (match.getTurnStatus() == TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN) {
				Games.TurnBasedMultiplayer.finishMatch(
						gameHelper.getApiClient(), match.getMatchId());
			}
		}
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
		finishMatch();

	}

	@Override
	public void onOpponentDeadListener(Player opponent) {
		finishMatch();
	}

	@Override
	public void onPlayerPlayedCard(Card card) {
		playerUsedCard(card, false);
	}

	@Override
	public void onPlayerUsedeffect(EffectType type, Player target, int amount) {
		if (type == EffectType.DAMAGE) {
			amount *= -1;
		}
		playerUsedEffect(type, target, amount);
	}

	@Override
	public void onPlayerDiscardCard(Card card) {
		playerUsedCard(card, true);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sign_in_button:
			gameHelper.reconnectClient();
			break;
		case R.id.sign_out_button:
			gameHelper.disconnect();
			showDisconnected();
			break;
		}
	}

	@Override
	public void onSignInSucceeded() {
		showConnected();
		View signin = (View) getView().findViewById(R.id.sign_in_button);
		signin.setOnClickListener(this);

		View signout = (View) getView().findViewById(R.id.sign_out_button);
		signout.setOnClickListener(this);

		// Update the text field that shows how many actions are waiting for
		// you. Invites/your turn
		Games.TurnBasedMultiplayer
				.loadMatchesByStatus(
						gameHelper.getApiClient(),
						new int[] { TurnBasedMatch.MATCH_TURN_STATUS_INVITED,
								TurnBasedMatch.MATCH_TURN_STATUS_MY_TURN })
				.setResultCallback(
						new ResultCallback<TurnBasedMultiplayer.LoadMatchesResult>() {

							@Override
							public void onResult(LoadMatchesResult result) {
								int invites = result.getMatches()
										.getInvitations().getCount();
								int yourturn = result.getMatches()
										.getMyTurnMatches().getCount();
								TextView tw = (TextView) getActivity()
										.findViewById(R.id.activeGamesText);
								tw.setText(String.format(
										getActivity().getResources().getString(
												R.string.activeGamesText),
										yourturn, invites));
								tw.setVisibility(View.VISIBLE);
								getActivity().findViewById(
										R.id.activeGamesSpinner).setVisibility(
										View.GONE);
							}
						});

		Games.TurnBasedMultiplayer.registerMatchUpdateListener(
				gameHelper.getApiClient(),
				new OnTurnBasedMatchUpdateReceivedListener() {

					@Override
					public void onTurnBasedMatchRemoved(String arg0) {
						// TODO Handle this.
					}

					@Override
					public void onTurnBasedMatchReceived(TurnBasedMatch match) {
						startGame(match);
					}
				});

		if (match != null) {
			startGame(match);
		}
	}

	@Override
	public void onSignInFailed() {
		Log.d("test", "Log in fail");
		showDisconnected();
	}
}