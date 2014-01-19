package com.tuer.tuerist;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holdMe;
	private Camera theCamera;
	private float[] focus;

	public CameraView(Context context, Camera camera) {
		super(context);
		theCamera = camera;
		holdMe = getHolder();
		holdMe.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	public float[] getFocusDistances() {
		return focus;
	}
	

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		focus = new float[3];

		try {
			theCamera.setPreviewDisplay(holder);

			Camera.Parameters params = theCamera.getParameters();
			params.setFocusMode("continuous-picture");
//			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			theCamera.setParameters(params);

//			Log.v("Tuerist", theCamera.getParameters().getFocusMode());

			theCamera.startPreview();
			float len = theCamera.getParameters().getFocalLength();
//			Log.v("Tuerist", "dat len: " + len);
//			theCamera.autoFocus(new AutoFocusCallback(){
//	        	@Override
//				public
//	        	void onAutoFocus(boolean success, Camera cam){
//	    			float len = theCamera.getParameters().getFocalLength();
////	    			Log.v("Tuerist", "dat other len: " + len);
//	        		cam.getParameters().getFocusDistances(focus);
////	        		Log.v("Tuerist", "LOGGIN' DAT SHIT: " + focus[0] + ", " + focus[1] + ", " + focus[2]);
//	        	}
//	        });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
	}

}