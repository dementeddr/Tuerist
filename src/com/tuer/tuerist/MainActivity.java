package com.tuer.tuerist;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.loopj.android.http.RequestParams;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class MainActivity extends Activity implements OnClickListener, SensorEventListener {

	private FileObserver observer;
	private TuerLocationListener locationListener;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private float[] gravity;
	private float[] geomagnetic;
	private double azimut;
	private Double lat;
	private Double lng;
	private Double bearing;
	private String slat;
	private String slng;
	private String sbearing;
	//private float focalLength;

	private Camera camera;
	private CameraView cv;

	public static Camera isCameraAvailable(){
		Camera object = null;
		try {
			object = Camera.open(); 
		}
		catch (Exception e){
		}

		return object; 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		observer = new FileWatcher("/sdcard/DCIM/Camera", this);
		observer.startWatching();

		locationListener = new TuerLocationListener();
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationListener.setLastLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

//		camera = isCameraAvailable();
//		cv = new CameraView(this, camera);
//		FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
//		preview.addView(cv);
	}

	private PictureCallback capturedIt = new PictureCallback () {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
			if (bitmap == null) {
				Log.v("Tuerist", "not taken");
			} else {
				Log.v("Tuerist", "taken");
			}
			camera.release();
		}
	};

	public void onClick(View view) {
		camera.takePicture(new Camera.ShutterCallback() {
			@Override
			public void onShutter() {
				// log some shit
				Location l = locationListener.getLastLocation();
				if (l != null) {
					Log.v("Tuerist", "Position: " + l.getLatitude() + ", " + l.getLongitude());
				}
				Log.v("Tuerist", "Azimuth: " + azimut);
				//float len = camera.getParameters().getFocalLength();
				//				camera.getParameters().getFocusDistances(focus);
				//Log.v("Tuerist", "Focus: " + len);
			}
		}, null, new Camera.PictureCallback() {
			public void onPictureTaken(byte[] imageData, Camera c) {
				//do what you want with the imageData. Like make it into a bmp (ill show how to below)
				InputStream is = new ByteArrayInputStream(imageData);
				Bitmap bmp = BitmapFactory.decodeStream(is); //now do what you want with it.
				camera.release();
			} 

		});
	}
	
	protected void onPause() {
		super.onPause();
		try {
		SharedPreferences prefs = this.getSharedPreferences("com.tuer.tuerist", Context.MODE_PRIVATE);
		Editor e = prefs.edit();
		Location l = locationListener.getLastLocation();
		e.putString("lat", Double.valueOf(l.getLatitude()).toString());
		e.putString("lng", Double.valueOf(l.getLongitude()).toString());
		e.putString("bearing", Double.valueOf(azimut).toString());
		e.commit();
		} catch (Exception e) {
			Log.e("Tuerist", e.getMessage());
		}
	}

	protected void onResume() {
		super.onResume();

		try {
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
			sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
			
			boolean temp = getIntent().getBooleanExtra("sendData", false);
			SharedPreferences prefs = this.getSharedPreferences("com.tuer.tuerist", Context.MODE_PRIVATE);
			slat = prefs.getString("lat", "0");
			slng = prefs.getString("lng", "0");
			sbearing = prefs.getString("bearing", "0");
			if (temp) {
				sendData();
			}
		} catch (Exception e) {
			Log.e("Tuerist", e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Creates a notification to ask if the user wants to send 
	 * the picture data to the server
	 * 
	 * @param view
	 */
	public void sendNotification() {
		//Creates an Intent object which will open the Tuerist app when one of
		//it's notifications is tapped.
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("sendData", true);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		//Creation of the notification
		Notification message =
				new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("Tuerist picture data captured")
		.setContentText("Send data to Tuerist server?")
		.setContentIntent(pIntent).build();

		// Hide the notification after its selected
		message.flags |= Notification.FLAG_AUTO_CANCEL;

		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		// mId allows you to update the notification later on.
		int mId = 42;
		mNotificationManager.notify(mId, message);
	}

	public void sendData() {
		try {
			RequestParams params = new RequestParams();
			Log.v("Tuerist", "lat: " + slat);
			params.put("lat", slat.toString());
			params.put("lng", slng.toString());
			params.put("bearing", sbearing.toString());
			params.put("focus", Double.valueOf(15).toString());
	
			TuerRestClient.post(params);
		} catch (Exception e) {
			Log.e("Tuerist", "asdf");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			gravity = event.values.clone();
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			geomagnetic = event.values.clone();
		if (gravity != null && geomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimut = Math.toDegrees(orientation[0]) + 180; // orientation contains: azimut, pitch and roll
			}
		}
	}
}
