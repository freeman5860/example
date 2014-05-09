package com.wangxl;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class ProximitySensorManager {
	
	private static Boolean isProximitySensorNearby(final SensorEvent event) {
		float threshold = 4.001f; // <= 4 cm is near

		final float distanceInCm = event.values[0];
		final float maxDistance = event.sensor.getMaximumRange();

		if (maxDistance <= threshold) {
			// Case binary 0/1 and short sensors
			threshold = maxDistance;
		}

		return distanceInCm < threshold;
	}

	private static boolean isLastProximitySensorValueNearby;
	private static Set<Activity> sProximityDependentActivities = new HashSet<Activity>();
	private static SensorEventListener sProximitySensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.timestamp == 0) return; //just ignoring for nexus 1
			isLastProximitySensorValueNearby = isProximitySensorNearby(event);
			proximityNearbyChanged();
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};


	private static void simulateProximitySensorNearby(Activity activity, boolean nearby) {
		final Window window = activity.getWindow();
		WindowManager.LayoutParams lAttrs = activity.getWindow().getAttributes();
		View view = ((ViewGroup) window.getDecorView().findViewById(android.R.id.content)).getChildAt(0);
		if (nearby) {
			lAttrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
			view.setVisibility(View.INVISIBLE);
		} else  {
			lAttrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN); 
			view.setVisibility(View.VISIBLE);
		}
		window.setAttributes(lAttrs);
	}

	private static void proximityNearbyChanged() {
		boolean nearby = isLastProximitySensorValueNearby;
		for (Activity activity : sProximityDependentActivities) {
			simulateProximitySensorNearby(activity, nearby);
		}
	}

	public static synchronized void startProximitySensorForActivity(Activity activity) {
		if (sProximityDependentActivities.contains(activity)) {
			return;
		}
		
		if (sProximityDependentActivities.size() == 0) {
			SensorManager sm = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
			Sensor s = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			if (s != null) {
				sm.registerListener(sProximitySensorListener,s,SensorManager.SENSOR_DELAY_UI);
			}
		} else if (isLastProximitySensorValueNearby){
			simulateProximitySensorNearby(activity, true);
		}

		sProximityDependentActivities.add(activity);
	}

	public static synchronized void stopProximitySensorForActivity(Activity activity) {
		sProximityDependentActivities.remove(activity);
		simulateProximitySensorNearby(activity, false);
		if (sProximityDependentActivities.size() == 0) {
			SensorManager sm = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
			sm.unregisterListener(sProximitySensorListener);
			isLastProximitySensorValueNearby = false;
		}
	}
}
