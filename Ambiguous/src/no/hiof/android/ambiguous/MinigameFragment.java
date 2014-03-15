package no.hiof.android.ambiguous;

import java.util.Calendar;
import java.util.Random;

import android.app.Fragment;
import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

/**
 * Minigame that affects the amount of effect a card does.
 */
public class MinigameFragment extends Fragment implements SensorEventListener {

	View player;
	View target;
	TextView score;
	private SensorManager sensorManager;
	private Sensor sensor;
	private int position;
	private int speed = 0;
	private int speedSensor = 0;
	private int width;
	private int points = 0;
	Random randomSpeed = new Random();
	
	private boolean stop = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		View v = getActivity().findViewById(R.id.minigame_container);

		player = getActivity().findViewById(R.id.minigame_player);
		target = getActivity().findViewById(R.id.minigame_target);
		score = (TextView)getActivity().findViewById(R.id.minigame_score);
		RelativeLayout.LayoutParams params = new LayoutParams(
				player.getLayoutParams());
		this.width = v.getLayoutParams().width;
		position = this.width / 2 - player.getWidth();
		params.leftMargin = position;
		player.setLayoutParams(params);
		setupSensor();

		previousLoop = Calendar.getInstance().getTimeInMillis();
		loop();
	}
	
	private void setupSensor()
	{
		sensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_GAME);
	}
	
	//Time of the previous loop.
	private long previousLoop;

	/**
	 * Starts the delayed game loop.
	 */
	private void loop()
	{
		if(stop){return;}
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			
			@Override
			public void run() {
                if(stop){return;}
                
				updatePosition();
				loop();
			}
		},50);
	}
	
	private int updateCount;
	private int randomSpeedMod;
	private int maxPoints;
	private void updatePosition()
	{
		
		long duration = Calendar.getInstance().getTimeInMillis() - previousLoop;
		
		updateCount++;
		
		maxPoints+=1;
		if(player.getLeft() >= target.getLeft() && player.getLeft() + player.getWidth() <= target.getLeft() + target.getWidth())
		{
			points+=1;
			score.setText(Integer.toString(points));
		}
		
		RelativeLayout.LayoutParams params = new LayoutParams(
				player.getLayoutParams());
		if(updateCount%20==0){randomSpeedMod=randomSpeed.nextInt(10)-5;}
		
		speed = randomSpeedMod + speedSensor;
				
		if(position + player.getMeasuredWidth() >= this.width  && speed > 0|| position <= 0 && speed <0){
			speed = 0;
			}
		
		position += speed * duration/25;
		params.leftMargin = position;

		player.setLayoutParams(params);
		previousLoop = Calendar.getInstance().getTimeInMillis();
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
		setupSensor();
		loop();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	public void onSensorChanged(SensorEvent event) {
		
		getResources().getConfiguration();
		//Log.d("rot",Float.toString(event.values[0]) + " " + Float.toString(event.values[1]) + " " + Float.toString(event.values[2]));
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			speedSensor = -1 * (int)event.values[0];
		}
	}
}
