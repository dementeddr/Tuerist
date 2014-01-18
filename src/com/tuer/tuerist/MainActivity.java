package com.tuer.tuerist;

import android.os.Bundle;
import android.os.FileObserver;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	
	//private static int picCount;
	private FileObserver observer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		observer = new FileWatcher
				(android.os.Environment.getExternalStorageDirectory().toString()
						+ "/DCIM/Camera", this);
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
	
	
	public void sendNotification(View view) {
		sendNotification();
	}
	
	public void onShutter() {
		sendNotification();
	}
}
