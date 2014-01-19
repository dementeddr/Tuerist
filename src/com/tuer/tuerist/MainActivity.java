package com.tuer.tuerist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.hardware.Camera;
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
import android.widget.Button;

public class MainActivity extends Activity implements SensorEventListener {

	private FileObserver observer;
	private TuerLocationListener locationListener;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private float[] gravity;
	private float[] geomagnetic;
	private double azimut;

	private FileOutputStream fos;
	private FileInputStream fis;
	private static final String saveFile = "Tuerist_Data";

	SharedPreferences prefs;
	private Editor editor;

	private String slat;
	private String slng;
	private String sbearing;
	//private float focalLength;

	//private Camera camera;
	//private CameraView cv;
	
	private int pictureCount;


	public static Camera isCameraAvailable(){
		Camera object = null;
		try {
			object = Camera.open(); 
		} catch (Exception e){
			Log.e("Tuerist", "Problem aquiring camera: " + e.getMessage());
		}

		return object; 
	}


	/**
	 * Called when the Activity (which basically means the app) is created.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Watches the Camera directory for added pictures
		observer = new FileWatcher("/sdcard/DCIM/Camera", this);
		observer.startWatching();
		
		//Makes sure the button corresponds with the right function. I hope.
		/*Button sendButton = (Button)findViewById(R.id.button_capture);
		sendButton.setOnClickListener(new OnClickListener() {
			  @Override
			  public void onClick(View v) {
				  readLocationFile(v);
			  }
			});

		try {//TODO REMOVE
			FileOutputStream fout = openFileOutput(saveFile, Context.MODE_PRIVATE);
			fout.write("success".getBytes());
			fout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e("Tuerist", "FRAAAACK");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//Used to determine azimuth
		locationListener = new TuerLocationListener();
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		locationListener.setLastLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		//Used to save data during pauses
		//prefs = this.getSharedPreferences("com.tuer.tuerist", Context.MODE_PRIVATE);
		//editor = prefs.edit();
		
		pictureCount = 0;//TODO Need to make count persistent
	}

	/**
	 * Executes when a different app is brought up. Saves the current location values.
	 */
	protected void onPause() {
		super.onPause();

		try {
			Location l = locationListener.getLastLocation();

			String lat = Double.valueOf(l.getLatitude()).toString();
			String lng = Double.valueOf(l.getLongitude()).toString();
			String bearing = Double.valueOf(azimut).toString();

			Log.v("Tuerism", "onPause lat: " + lat + "  lng: " + lng + "  bearing: " + bearing);

			/*
			//Cleans out the data storage and adds the current data
			editor.clear();
			editor.putString("lat", lat);
			editor.putString("lng", lng);
			editor.putString("bearing", bearing);
			editor.putBoolean("dataSent", false);
			editor.commit();*/

		} catch (Exception e) {
			Log.e("Tuerist", e.getMessage());
		}
	}


	/**
	 * Reads the location values back in when the app resumes
	 */
	protected void onResume() {
		super.onResume();

		try {
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
			sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

			boolean temp = getIntent().getBooleanExtra("readLocationFile", false);
			/*SharedPreferences prefs = this.getSharedPreferences("com.tuer.tuerist", Context.MODE_PRIVATE);
			slat = prefs.getString("lat", "0");
			slng = prefs.getString("lng", "0");
			sbearing = prefs.getString("bearing", "0");
			*/
			if (temp) {
				readLocationFile(null);
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
		intent.putExtra("readLocationFile", true);
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

	/**
	 * Called when a new picture is taken. Is NOT an API method.
	 */
	protected void onNewPictureDetected() {
		Location location = locationListener.getLastLocation();
		writeNewLocation(location);
		sendNotification();
	}


	/**
	 * Adds the picture data to a RequestParams and sends it to the POST function.
	 */
	public void sendData() {
		try {
			//RequestParams moved to TuerRestClient for debug purposes.
			String data[] = {slat.toString(), slng.toString(), sbearing.toString(), Double.valueOf(15).toString()};

			Log.v("Tuerist", "sendData lat: " + slat.toString() + "  lng: " 
					+ slng.toString() + "  bearing: " + sbearing.toString());

			//If POST request was successful, the data can be cleared.
			boolean successful = TuerRestClient.post(data);

			if (successful) {
				editor.clear();
				editor.commit();
			} else {
				Log.e("Tuerist", "Unable to post data to server");
			}

		} catch (Exception e) {
			Log.e("Tuerist", "Exeption in sendData");
		}
	}


	/**
	 * Required for abstract class.
	 */
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	/**
	 * Used to keep azimut up to date.
	 */
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
				azimut = Math.toDegrees(orientation[0]) + 180 ; // orientation contains: azimut, pitch and roll
			}
		}
	}


	/**
	 * Writes the latest picture data to the end of the save file.
	 * 
	 * @param location
	 * @return
	 */
	private boolean writeNewLocation(Location location) {
		try {
			fos = openFileOutput(saveFile,Context.MODE_PRIVATE & Context.MODE_APPEND);
			
			String output = location.getLatitude() + "\n" + location.getLongitude() 
					+ "\n" + location.getBearing() + "\n"; 
			Log.v("Tuerist", "Writing Data to file: " + output);
			
			fos.write(output.getBytes());
			fos.close();
			
		} catch (IOException e) {
			Log.e("Tuerist", "Data failed to write: " + e.getMessage());
			return false;
		}
		Log.v("Tuerist", "Picture data written successfully");
		return true;
	}
	
	
	/**
	 * Reads all of the data from the save file and sends it to the server.
	 * @return
	 */
	private boolean readLocationFile(View view) {
		try {
	        File f = new File(saveFile);
	        if (!f.exists()) {
	        	Log.v("Tuerist", "No data to POST");
	        	return false;
	        }

			fis = openFileInput(saveFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

			String line;
			String data[] = new String[4];
			boolean successful = false;
			
			//Reads three pieces of data at a time and Posts them.
	        while ((line = reader.readLine()) != null) {
	        	data[0] = line;
	        	data[1] = reader.readLine();
	        	data[2]	= reader.readLine();
	        	data[3] = Double.toString(15);//TODO Begone!
	        	
				Log.v("Tuerist", "sendData lat: " + data[0] + "  lng: " 
						+ data[1] + "  bearing: " + data[2]);

				//If POST request was successful, the data can be cleared.
				successful = TuerRestClient.post(data);
				
				if (!successful) {
					Log.e("Tuerist", "Unable to POST data to server");
					return false;
				}
	        }

	        reader.close();
	        fis.close();
	        
			//f.delete();

		} catch (IOException e) {
			Log.e("Tuerist", "Failed to read data file: " + e.getMessage());
			return false;
		} catch (NullPointerException e) {
			Log.e("Turiest", "So it is here");
			return false;
		}
		Log.v("Tuerist", "Picture data file read successfully");
		return true;
	}


	/**
	 * Make sure pictures data goes away on exit
	 */
	public void onDestroy() {
		editor.clear();
		editor.commit();
	}
}
