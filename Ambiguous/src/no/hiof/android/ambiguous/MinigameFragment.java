package no.hiof.android.ambiguous;

import java.util.Calendar;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * Minigame that affects the amount of effect a card does.
 */
public class MinigameFragment extends Fragment implements SensorEventListener {

	// The view the player controlls
	View player;
	View target;
	TextView score;
	private SensorManager sensorManager;
	private Sensor sensor;
	private int position;
	private int tPosition;
	// Current speed applied to the player view
	private int speed = 0;
	// Current speed modifier gotten from user tilting device
	private int speedSensor = 0;
	// The width of the minigame container
	private int width;
	// Players current points
	private float points = 0;

	// The delay between each loop
	private int loopDelay = 50;
	// Duration of the minigame
	private long timeLimit = 5000;

	// Randomizes the speed effect on players view
	Random randomSpeed = new Random();

	// The min and max values that affect what numbers the minigame can return.
	// Note the fragment can return values higher then max.
	int min, max, cardPosition;

	private boolean stop = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.min = getArguments().getInt("min");
		this.max = getArguments().getInt("max");
		this.cardPosition = getArguments().getInt("pos");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_minigame, container,
				false);

		return layout;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.width = getActivity().findViewById(R.id.minigame_container)
				.getLayoutParams().width;

		Button b = (Button) getActivity().findViewById(R.id.minigame_help);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showHelp(false);
			}
		});
		player = getActivity().findViewById(R.id.minigame_player);
		target = getActivity().findViewById(R.id.minigame_target);
		score = (TextView) getActivity().findViewById(R.id.minigame_score);
		score.setText("0");

		// Position players view in the center
		RelativeLayout.LayoutParams params = new LayoutParams(
				player.getLayoutParams());
		position = this.width / 2 - player.getWidth() - 10;
		params.leftMargin = position;
		player.setLayoutParams(params);
		//Position target view in the center
		params = new LayoutParams(
				target.getLayoutParams());
		tPosition = this.width / 2 - target.getWidth() - 10;
		params.leftMargin = tPosition;
		target.setLayoutParams(params);

		// Only show help if player has not disabled it in preferences.
		if (hideHelp()) {
			startLoop();
		} else {
			showHelp(true);
		}
	}

	/**
	 * Setup the sensor and start the game loop.
	 */
	private void startLoop() {
		setupSensor();
		previousLoop = Calendar.getInstance().getTimeInMillis();
		loop();
	}

	/**
	 * If the player has told us we should hide the startup help dialog.
	 * 
	 * @return Should we hide the help dialog?
	 */
	private boolean hideHelp() {
		SharedPreferences s = getActivity()
				.getPreferences(Context.MODE_PRIVATE);
		return s.getBoolean("hideHelp", false);
	}

	//We check for the API before we load this fragment.
	@SuppressLint("InlinedApi")
	private void setupSensor() {
		sensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_GAME);
	}

	// Time of the previous loop.
	private long previousLoop;

	/**
	 * Starts the game loop.
	 */
	private void loop() {
		if (stop) {
			return;
		}

		// We run the game loop with a delay by using a handler.
		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				if (stop) {
					return;
				}

				updatePosition();
				loop();
			}
		}, loopDelay);
	}

	// How many times the update loop has run.
	private int updateCount;
	// The random speed the player has to work against by tilting.
	private int randomSpeedMod = getRandomSpeed();
	// Maximum possible points player could have achieved, used to calculate how
	// big value the minigame will return.
	private int maxPoints = (int) timeLimit / loopDelay;
	private int estimateMaxPoints = (int) timeLimit / loopDelay;
	// Total duration in ms of the game loop.
	private int totalDuration = 0;

	// TODO: This method does several things as shown in its description, should
	// split it up.
	/**
	 * The main game loop. Updates player position. Generates new random speed
	 * element. Stops game if time has run out.
	 */
	private void updatePosition() {
		long duration = Calendar.getInstance().getTimeInMillis() - previousLoop;

		// Updates the total duration of the game and stops it if there is no
		// time left.
		totalDuration += duration;
		if (totalDuration >= timeLimit) {
			notifyGameComplete();
			stop = true;
			return;
		}

		updateCount++;

		// Checks if players view is inside the target view, and awards points.
		if (player.getLeft() >= target.getLeft()
				&& player.getLeft() + player.getWidth() <= target.getLeft()
						+ target.getWidth()) {
			points += duration / loopDelay;
			int estScore = (int) ((max - min) * (points / (estimateMaxPoints * 0.6)));
			score.setText(Integer.toString(estScore));
		}

		RelativeLayout.LayoutParams params = new LayoutParams(
				player.getLayoutParams());

		if (updateCount % 40 == 0) {
			randomSpeedMod = getRandomSpeed();
		}

		speed = speedSensor;

		// Prevent players view to move outside the game container.
		if (position + player.getMeasuredWidth() >= this.width && speed > 0
				|| position <= 0 && speed < 0) {
			speed = 0;
		}

		// If target is about to move outside the view we invert its direction.
		if (tPosition + target.getMeasuredWidth() >= this.width && randomSpeedMod > 0
				|| tPosition <= 0 && randomSpeedMod < 0) {
			randomSpeedMod*=-1;			
		}

		// Move the players view
		position += (speed * duration / loopDelay)*1.5;
		params.leftMargin = position;
		player.setLayoutParams(params);
		
		//Move the target view
		RelativeLayout.LayoutParams tParams = new LayoutParams(
				target.getLayoutParams());
		tPosition += (randomSpeedMod * duration / loopDelay)*1.5;
		tParams.leftMargin = tPosition;
		target.setLayoutParams(tParams);

		// So we know how the duration between game updates.
		previousLoop = Calendar.getInstance().getTimeInMillis();
	}

	private int getRandomSpeed() {
		int rnd;
		// Avoid small speed values which are to easy for the player.
		do
		{
			rnd = randomSpeed.nextInt(5) - 2;
		}while(Math.abs(rnd) <= 0);
		return rnd;
	}

	@Override
	public void onPause() {
		super.onPause();
		stop = true;
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		stop = false;

		// If showing the opening help screen, the game will be started when
		// player exit the help dialog.
		if (showingHelp) {
			return;
		} else {
			startLoop();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {

		getResources().getConfiguration();

		// TODO: Fix landscape mode, it might already work?
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || true) {
			speedSensor = -1
					* (int) (event.values[0] * (1 + event.values[1] / 9.81));
		}
	}

	// Listener that will get events from the minigame.
	private MinigameListener listener;

	public void setMinigameListener(MinigameListener listener) {
		this.listener = listener;
	}

	/**
	 * Notifies the listener on player's score
	 */
	private void notifyGameComplete() {
		sensorManager.unregisterListener(this);
		int amount = (int) ((max - min) * (points / (maxPoints * 0.6)));
		listener.onGameEnd(amount, cardPosition);
		listener = null;
	}

	public interface MinigameListener {
		public void onGameEnd(int amount, int position);
	}

	public void showHelp(View view) {
		showHelp(false);
	}

	private boolean showingHelp = false;

	public void showHelp(boolean askNeverShow) {
		if (askNeverShow) {
			showingHelp = true;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Minigame help");
		builder.setMessage("Keep the white bar inside of the red bar by tilting the device left and right.");

		builder.setNeutralButton("Start", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				if (stop) {
					return;
				}
				startLoop();
				showingHelp = false;
			}
		});

		if (askNeverShow) {
			builder.setNegativeButton("Never show this message again",
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							SharedPreferences sp = getActivity()
									.getPreferences(Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sp.edit();
							editor.putBoolean("hideHelp", true);
							editor.commit();
							startLoop();
							showingHelp = false;
						}
					});
		}

		final AlertDialog d = builder.create();
		d.show();
	}
}
