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

public class NetworkOpponent implements GameMachineListener {

	public enum Packets {
		PLAYER_STATS(100), PLAYER_PLAYED_CARD(101), PLAYER_DISCARD_CARD(102), PLAYER_AFK(
				103), PLAYER_USED_EFFECT(104), PLAYER_TURN_DONE(105);

		private int value;

		private Packets(int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}
		
		public static Packets getPacket(int value)
		{
			for(int i=0;i<Packets.values().length;i++)
			{
				if(Packets.values()[i].getValue() == value)
				{
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

	private Queue<Byte> dataBuffer = new LinkedList<Byte>();

	// private byte[] dataBuffer = new byte[5000];
	// private int dataBufferPosition = 0;

	public NetworkOpponent(OpponentController oc, Player player,
			Player opponent, Socket socket) {
		handler = new Handler();
		this.oc = oc;
		this.player = player;
		this.opponent = opponent;
		this.socket = socket;
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
					while ((r = in.read(buffer, 0, buffer.length)) > 0) {
						// More data incomming then being removed from
						// databuffer,
						// should never happen.
						// Prolly best to close network, show msg about network
						// error
						// etc.
						// if(dataBufferPosition+r-1 >= dataBuffer.length){
						// in.close();}

						// Concat all the buffers read into a larger buffer
						// incase we
						// get packets split over multiple reads.
						for (int i = 0; i < r; i++) {
							// dataBuffer[dataBufferPosition++] = buffer[i];
							dataBuffer.add(buffer[i]);
						}

						readPacketsFromBuffer();
					}

				} catch (IOException e) {
					try {
						NetworkOpponent.this.socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	private void readPacketsFromBuffer() {
		while (dataBuffer.size() >= 1) {
			switch (Packets.getPacket(dataBuffer.peek())) {
			case PLAYER_DISCARD_CARD:
				dataBuffer.poll();
				handler.post(new Runnable() {
					
					@Override
					public void run() {
                        oc.DiscardCard(dataBuffer.poll());
					}
				});
				break;
			case PLAYER_PLAYED_CARD:
				dataBuffer.poll();
				final int card = dataBuffer.poll();
				handler.post(new Runnable() {
					
					@Override
					public void run() {
                    oc.PlayCard(card, false);
						
					}
				});
				break;
			case PLAYER_USED_EFFECT:
				dataBuffer.poll();
				final EffectType type = EffectType.values()[dataBuffer.poll()];
				final Player target = (dataBuffer.poll() == 1 ? opponent : player);
				final int amount = dataBuffer.poll();
				handler.post(new Runnable() {
					
					@Override
					public void run() {
                        oc.UseEffect(type, target, amount);
					}
				});
				// Not implemented
			case PLAYER_STATS:
				if (dataBuffer.size() < 3) {
					return;
				}
				dataBuffer.poll();
				break;
			case PLAYER_TURN_DONE:
				dataBuffer.poll();
				handler.post(new Runnable() {
					
					@Override
					public void run() {
                        oc.TurnDone();
					}
				});
				break;
			default:
				// Add stuff to handle unknown packet header, something like
				// give msg abort game etc.
				Log.d("test", "I dont know this packet! Something went wrong!!");
				break;
			}
		}
	}

	private void sendPlayedCard(int card) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream(2);
			DataOutputStream writer = new DataOutputStream(stream);
			writer.writeByte(Packets.PLAYER_PLAYED_CARD.getValue());
			writer.writeByte(card);
			new WriteBytesTask().Setup(out).execute(stream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendUsedEffect(EffectType type, Player target, int amount) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream(4);
			DataOutputStream writer = new DataOutputStream(stream);
			writer.writeByte(Packets.PLAYER_USED_EFFECT.getValue());
			writer.writeByte(type.ordinal());
			writer.writeByte((target == this.opponent ? 0 : 1));
			writer.writeByte(amount);
			new WriteBytesTask().Setup(out).execute(stream.toByteArray());
		} catch (IOException e) {

		}
	}

	private void sendOpponentTurn() {
			new WriteBytesTask().Setup(out).execute(new byte[]{(byte) Packets.PLAYER_TURN_DONE.getValue()});
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
		handler.post(new Runnable() {

			@Override
			public void run() {
				sendOpponentTurn();
			}
		});
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
	public void onPlayerPlayedCard(final Card card) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				sendPlayedCard(card.getId());
			}
		});
	}

	@Override
	public void onPlayerDiscardCard(Card card) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayerUsedeffect(final EffectType type, final Player target,
			final int amount) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				sendUsedEffect(type, target, amount);
			}
		});
	}

}
