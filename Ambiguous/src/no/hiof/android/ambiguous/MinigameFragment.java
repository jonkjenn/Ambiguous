package no.hiof.android.ambiguous;

import android.R.bool;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MinigameFragment extends Fragment implements SensorEventListener {

	View player;
	private SensorManager sensorManager;
	private Sensor sensor;
	private int position;
	private int speed = 1;
	private int width;
	
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
		RelativeLayout.LayoutParams params = new LayoutParams(
				player.getLayoutParams());
		this.width = v.getLayoutParams().width;
		position = this.width / 2 - player.getWidth();
		params.leftMargin = position;
		player.setLayoutParams(params);
		setupSensor();

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
	
	private void loop()
	{
		if(stop){return;}
		Handler h = new Handler();
		h.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				updatePosition(50);
				loop();
			}
		},50);
	}
	
	private void updatePosition(int duration)
	{
		player = getActivity().findViewById(R.id.minigame_player);
		RelativeLayout.LayoutParams params = new LayoutParams(
				player.getLayoutParams());
		position += speed * duration/25;
		params.leftMargin = position;
		if(params.leftMargin + player.getWidth() >= this.width){speed = -1;}
		player.setLayoutParams(params);
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
			speed = -1 * (int)event.values[0];
		}
	}
}
