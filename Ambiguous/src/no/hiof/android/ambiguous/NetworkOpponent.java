package no.hiof.android.ambiguous;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import no.hiof.android.ambiguous.GameMachine.GameMachineListener;
import no.hiof.android.ambiguous.model.Card;
import no.hiof.android.ambiguous.model.Effect.EffectType;
import no.hiof.android.ambiguous.model.Player;
import no.hiof.android.ambiguous.network.WriteBytesTask;
import android.os.Handler;
import android.util.Log;

/**
 * Handles the packet read and write against a network opponent.
 * 
 * Call start() to start the network operation.
 */
public class NetworkOpponent implements GameMachineListener, OnNetworkErrorListener {

	/**
	 * Enumeration of different packet types each with an unique id.
	 */
	public enum Packets {
		PLAYER_STATS(100), // Not implemented.
		PLAYED_CARD(101), // Id of the Card the player played.
		DISCARD_CARD(102), // Id of the Card the player discarded.
		AFK(103), // Not implemented.
		USED_EFFECT(104), // Which effect used, who is the target and what
							// amount of effect.
		TURN_DONE(105); // Player has completed his turn.

		private int packetId;

		/**
		 * Constructor so that we can get an actual identifying int as a key for
		 * each enum value.
		 * 
		 * @param packetId
		 *            The id for the packet.
		 */
		private Packets(int packetId) {
			this.packetId = packetId;
		}

		public int getPacketId() {
			return this.packetId;
		}

		/**
		 * Find a specific packet enum value from a packet id.
		 * 
		 * @param packetId
		 *            Id of the packet we want to find.
		 * @return The enumeration value.
		 */
		public static Packets getPacket(int packetId) {
			for (int i = 0; i < Packets.values().length; i++) {
				if (Packets.values()[i].getPacketId() == packetId) {
					return Packets.values()[i];
				}
			}
			return null;
		}

	};

	private OpponentController oc;
	private Player player;
	private Player opponent;

	private Socket socket;
	private Handler handler;
	private DataOutputStream out;

	Byte peek;

	/**
	 * The buffer where we will put incoming packets.
	 */
	private Queue<Byte> dataBuffer = new LinkedList<Byte>();

	/**
	 * 
	 * @param oc
	 * @param player
	 *            The local player
	 * @param opponent
	 *            The remote opponent
	 * @param socket
	 *            The already open socket.
	 */
	public NetworkOpponent(OpponentController oc, Player player,
			Player opponent, Socket socket) {
		handler = new Handler();
		this.oc = oc;
		this.player = player;
		this.opponent = opponent;
		this.socket = socket;
	}

	/**
	 * Creates a new thread and starts listening for network data.
	 */
	public void start() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					out = new DataOutputStream(
							NetworkOpponent.this.socket.getOutputStream());
					DataInputStream in = new DataInputStream(
							NetworkOpponent.this.socket.getInputStream());

					byte[] buffer = new byte[100];

					int r;
					// Reads the currently available data into the buffer.
					// Limited to our buffer size.
					while ((r = in.read(buffer, 0, buffer.length)) > 0) {
						for (int i = 0; i < r; i++) {
							// Adds the bytes read into our Queue.
							dataBuffer.add(buffer[i]);
						}

						readPacketsFromBuffer();
					}

				} catch (IOException e) {
					notifyNetworkError(e.getMessage());
				}
			}
		});
		t.start();

	}

	/**
	 * Closes the network socket while ignoring any exceptions.
	 */
	private void closeSilently() {
		try {
			NetworkOpponent.this.socket.close();
		} catch (IOException e1) {
			// We have already notified about network error.
		}
	}

	/**
	 * Extract all the packets currently in the data buffer and applies the data
	 * to the OpponentController.
	 */
	private void readPacketsFromBuffer() {

		while (dataBuffer.size() >= 1) {
			switch (Packets.getPacket(dataBuffer.poll())) {
			case DISCARD_CARD:// Opponent discarded card
				final int card = dataBuffer.poll();
				handler.post(new Runnable() {

					@Override
					public void run() {
						oc.discardCard(card);
					}
				});
				break;
			case PLAYED_CARD:// Opponent played card
				final int playedcard = dataBuffer.poll();
				handler.post(new Runnable() {

					@Override
					public void run() {
						oc.playCard(playedcard, false);

					}
				});
				break;
			case USED_EFFECT:// Opponent used effect
				final EffectType type = EffectType.values()[dataBuffer.poll()];
				final Player target = (dataBuffer.poll() == 1 ? opponent
						: player);
				final int amount = dataBuffer.poll();
				handler.post(new Runnable() {

					@Override
					public void run() {
						oc.useEffect(type, target, amount, false);
					}
				});
				break;
			case PLAYER_STATS:// Not implemented
				if (dataBuffer.size() < 3) {
					return;
				}
				dataBuffer.poll();
				break;
			case TURN_DONE:
				handler.post(new Runnable() {

					@Override
					public void run() {
						oc.turnDone();
					}
				});
				break;
			default:// This shouldnt happen, we abort game.
				Log.d("test", "I dont know this packet! Something went wrong!!");
				notifyNetworkError(null);
				return;
			}
		}
	}

	/**
	 * Writes a packet for player using a card to the network.
	 * 
	 * @param card
	 *            The id of the card the local player uses.
	 */
	private void sendPlayedCard(int card) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream(2);
			DataOutputStream writer = new DataOutputStream(stream);
			writer.writeByte(Packets.PLAYED_CARD.getPacketId());
			writer.writeByte(card);
			new WriteBytesTask().Setup(out,this).execute(stream.toByteArray());
		} catch (IOException e) {
			notifyNetworkError(e.getMessage());
		}
	}

	/**
	 * Writes a packet for player discarding a card to the network.
	 * 
	 * @param card
	 *            The id of the card the local player discards.
	 */
	private void sendDiscardCard(int card) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream(2);
			DataOutputStream writer = new DataOutputStream(stream);
			writer.writeByte(Packets.DISCARD_CARD.getPacketId());
			writer.writeByte(card);
			new WriteBytesTask().Setup(out,this).execute(stream.toByteArray());
		} catch (IOException e) {
			notifyNetworkError(e.getMessage());
		}
	}

	/**
	 * Writes a packet for player using a specific effect amount on a specific
	 * target.
	 * 
	 * @param type
	 * @param target
	 * @param amount
	 */
	private void sendUsedEffect(EffectType type, Player target, int amount) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream(4);
			DataOutputStream writer = new DataOutputStream(stream);
			writer.writeByte(Packets.USED_EFFECT.getPacketId());
			writer.writeByte(type.ordinal());
			writer.writeByte((target == this.opponent ? 0 : 1));
			writer.writeByte(amount);
			new WriteBytesTask().Setup(out,this).execute(stream.toByteArray());
		} catch (IOException e) {
			notifyNetworkError(e.getMessage());
		}
	}

	/**
	 * Writes a packet notifying the network opponent that it's their turn.
	 */
	private void sendOpponentTurn() {
		new WriteBytesTask().Setup(out,this).execute(
				new byte[] { (byte) Packets.TURN_DONE.getPacketId() });
	}

	OnNetworkErrorListener onNetworkErrorListener;

	/**
	 * Sets the current OnNetworkErrorListener, there can only be one listener
	 * at a time. Set to null to unsubscribe.
	 * 
	 * @param l
	 */
	public void setOnNetworkErrorListener(OnNetworkErrorListener l) {
		this.onNetworkErrorListener = l;
	}

	/**
	 * Closes the network socket and notifies opponent
	 * 
	 * @param error
	 *            The error/exception msg.
	 */
	void notifyNetworkError(String error) {
		closeSilently();
		if (this.onNetworkErrorListener != null) {
			this.onNetworkErrorListener.onNetworkError(error);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void onOpponentTurnListener() {
		sendOpponentTurn();
	}

	@Override
	public void onPlayerPlayedCard(Card card) {
		sendPlayedCard(card.id);
	}

	@Override
	public void onPlayerDiscardCard(Card card) {
		sendDiscardCard(card.id);
	}

	@Override
	public void onPlayerUsedeffect(final EffectType type, final Player target,
			final int amount) {
		sendUsedEffect(type, target, amount);
	}

	@Override
	public void onNetworkError(String error) {
		notifyNetworkError(error);		
	}
}
