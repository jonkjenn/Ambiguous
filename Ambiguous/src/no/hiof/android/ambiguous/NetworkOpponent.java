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
			return value;
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
		try {
			out = new DataOutputStream(socket.getOutputStream());
			DataInputStream in = new DataInputStream(socket.getInputStream());
			byte[] buffer = new byte[100];

			int r;
			while ((r = in.read(buffer, 0, buffer.length)) > 0) {
				// More data incomming then being removed from databuffer,
				// should never happen.
				// Prolly best to close network, show msg about network error
				// etc.
				// if(dataBufferPosition+r-1 >= dataBuffer.length){ in.close();}

				// Concat all the buffers read into a larger buffer incase we
				// get packets split over multiple reads.
				for (int i = 0; i < r; i++) {
					// dataBuffer[dataBufferPosition++] = buffer[i];
					dataBuffer.add(buffer[i]);
				}

				readPacketsFromBuffer();
			}

		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	private void readPacketsFromBuffer() {
		Player target;
		while (dataBuffer.size() > 1) {
			switch (Packets.values()[dataBuffer.peek()]) {
			case PLAYER_DISCARD_CARD:
				dataBuffer.poll();
				oc.DiscardCard(dataBuffer.poll());
				break;
			case PLAYER_PLAYED_CARD:
				dataBuffer.poll();
				int card = dataBuffer.poll();
				oc.PlayCard(card, false);
				break;
			case PLAYER_USED_EFFECT:
				dataBuffer.poll();
				EffectType type = EffectType.values()[dataBuffer.poll()];
				target = (dataBuffer.poll() == 0 ? opponent : player);
				int amount = dataBuffer.poll();
				oc.UseEffect(type, target, amount);
				// Not implemented
			case PLAYER_STATS:
				if (dataBuffer.size() < 3) {
					return;
				}
				dataBuffer.poll();
				break;
			case PLAYER_TURN_DONE:
				oc.TurnDone();
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
			ByteArrayOutputStream stream = new ByteArrayOutputStream(3);
			DataOutputStream writer = new DataOutputStream(stream);
			writer.writeByte(Packets.PLAYER_PLAYED_CARD.ordinal());
			writer.writeByte(card);
			out.write(stream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendUsedEffect(EffectType type, Player target, int amount) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream(3);
			DataOutputStream writer = new DataOutputStream(stream);
			writer.writeByte(type.ordinal());
			writer.writeByte((target == this.opponent ? 0 : 1));
			writer.writeByte(amount);
			out.write(stream.toByteArray());
		} catch (IOException e) {

		}
	}
	
	private void sendOpponentTurn()
	{
		try {
			out.writeByte(Packets.PLAYER_TURN_DONE.ordinal());
		} catch (IOException e) {
			e.printStackTrace();
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
	public void onPlayerUsedeffect(final EffectType type,final Player target,final int amount) {
			handler.post(new Runnable() {

			@Override
			public void run() {
				sendUsedEffect(type,target,amount);
			}
		});
	}

}
