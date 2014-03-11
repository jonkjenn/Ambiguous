package no.hiof.android.ambiguous;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.renderscript.Matrix3f;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MinigameFragment extends Fragment implements SensorEventListener {

	View player;

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

	private SensorManager sensorManager;
	private Sensor sensor;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View v = getActivity().findViewById(R.id.minigame_container);

		player = getActivity().findViewById(R.id.minigame_player);
		RelativeLayout.LayoutParams params = new LayoutParams(
				player.getLayoutParams());
		params.leftMargin = v.getLayoutParams().width / 2 - player.getWidth();
		player.setLayoutParams(params);

		sensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_GAME);

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	// Create a constant to convert nanoseconds to seconds.
	private static final float NS2S = 1.0f / 1000000000.0f;
	private static final float EPSILON = 0.1f;
	private final float[] deltaRotationVector = new float[4];
	private float timestamp;
	private float[] topDir = new float[] { 1.0f,0,0,0,1.0f,0,0,0,1.0f};
	private float[] result = new float[9];

	public void onSensorChanged(SensorEvent event) {
		
		Log.d("rot",Float.toString(event.values[0]) + " " + Float.toString(event.values[1]) + " " + Float.toString(event.values[2]));
		if(true){return;}
		// This timestep's delta rotation to be multiplied by the current
		// rotation
		// after computing it from the gyro sample data.
		if (timestamp != 0) {
			final float dT = (event.timestamp - timestamp) * NS2S;
			// Axis of the rotation sample, not normalized yet.
			float axisX = event.values[0];
			float axisY = event.values[1];
			float axisZ = event.values[2];

			// Calculate the angular speed of the sample
			float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY
					* axisY + axisZ * axisZ);

			// Normalize the rotation vector if it's big enough to get the axis
			// (that is, EPSILON should represent your maximum allowable margin
			// of error)
			if (omegaMagnitude > EPSILON) {
				axisX /= omegaMagnitude;
				axisY /= omegaMagnitude;
				axisZ /= omegaMagnitude;
			}

			// Integrate around this axis with the angular speed by the timestep
			// in order to get a delta rotation from this sample over the
			// timestep
			// We will convert this axis-angle representation of the delta
			// rotation
			// into a quaternion before turning it into the rotation matrix.
			float thetaOverTwo = omegaMagnitude * dT / 2.0f;
			float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
			float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
			deltaRotationVector[0] = sinThetaOverTwo * axisX;
			deltaRotationVector[1] = sinThetaOverTwo * axisY;
			deltaRotationVector[2] = sinThetaOverTwo * axisZ;
			deltaRotationVector[3] = cosThetaOverTwo;
		}
		timestamp = event.timestamp;
		float[] deltaRotationMatrix = new float[9];
		SensorManager.getRotationMatrixFromVector(deltaRotationMatrix,
				deltaRotationVector);
		// User code should concatenate the delta rotation we computed with the
		// current rotation
		// in order to get the updated rotation.
		// rotationCurrent = rotationCurrent * deltaRotationMatrix;
		
		topDir = matrixMultiplication(topDir,deltaRotationMatrix);
		Log.d("gyro", Float.toString(deltaRotationMatrix[0]) + " " + Float.toString(deltaRotationMatrix[4]) + " " + Float.toString(deltaRotationMatrix[8]));
		//Log.d("gyro", Float.toString(topDir[0]) + " " + Float.toString(topDir[4]) + " " + Float.toString(topDir[8]));

	}

	private float[] matrixMultiplication(float[] a, float[] b)
	{
		float[] result = new float[9];

		result[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
		result[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
		result[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];

		result[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
		result[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
		result[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];

		result[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
		result[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
		result[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];

		return result;
	}
}
