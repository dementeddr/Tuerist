package com.tuer.tuerist;

import android.util.Log;

import com.loopj.android.http.*;

public class TuerRestClient {

	private static final String URL = "http://jsvana.io:5000/pictures/new";
	private static boolean successful = false;
//	private static AsyncHttpClient client;

	/**
	 * Takes the data captured from the pictures and puts them in a POST request
	 * to the server
	 * 
	 * @param params
	 */
	public static boolean post(String data[]) {
		AsyncHttpClient client = new AsyncHttpClient();
		
		//Adding to RequestParams was moved here for debug purposes
		RequestParams params = new RequestParams();

		params.put("lat", data[0]);
		params.put("lng", data[1]);
		params.put("bearing", data[2]);
		params.put("focus", data[3]);
		
		Log.v("Tuerist", "Attempting to POST: " + data[0] + " " + data[1] + " " + data[2]);

		client.post(URL, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Log.v("Tuerist", response);
				successful = true;
			}
			
			@Override
			public void onFailure(Throwable error, String content) {
				Log.e("Tuerist", "Error: " + error.getMessage() + " (" + content + ")");
				successful = true;
			}
		});
		
		return successful;
	}
}
