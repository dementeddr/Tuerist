package com.tuer.tuerist;

import android.util.Log;

import com.loopj.android.http.*;

public class TuerRestClient {

	private static final String URL = "http://jsvana.io:5000/pictures/new";
//	private static AsyncHttpClient client;

	/**
	 * Takes the data captured from the pictures and puts them in a POST request
	 * to the server
	 * 
	 * @param params
	 */
	public static void post(RequestParams params) {
		AsyncHttpClient client = new AsyncHttpClient();

		client.post(URL, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Log.v("Tuerist", response);
			}
			
			@Override
			public void onFailure(Throwable error, String content) {
				Log.e("Tuerist", "Error: " + error.getMessage() + " (" + content + ")");
			}
		});
	}
}
