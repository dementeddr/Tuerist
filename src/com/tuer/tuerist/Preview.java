package com.tuer.tuerist;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

class Preview extends ViewGroup implements SurfaceHolder.Callback {

	MainActivity main;
    SurfaceView mSurfaceView;
    SurfaceHolder mHolder;
    Camera camera;
    List<Size> supportedPreviewSizes;
    float f[] = new float[3];

    Preview(Context context, MainActivity m) {
        super(context);
        
        main = m;

        mSurfaceView = new SurfaceView(context);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    
    public void setCamera(Camera c) {
        if (camera == c) { return; }
        
        stopPreviewAndFreeCamera();
        
        camera = c;
        
        if (camera != null) {
            List<Size> localSizes = camera.getParameters().getSupportedPreviewSizes();
            supportedPreviewSizes = localSizes;
            requestLayout();
          
            try {
                camera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
          
            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            camera.startPreview();
        }
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(w, h);
        requestLayout();
        camera.setParameters(parameters);

        // Important: Call startPreview() to start updating the preview surface.
        // Preview must be started before you can take a picture.
        camera.startPreview();
        camera.autoFocus(new AutoFocusCallback(){
        	@Override
			public
        	void onAutoFocus(boolean success, Camera cam){
        		cam.getParameters().getFocusDistances(f);
        	}
        });
    }
    
    public void onClick(View v) {
        camera.takePicture(new Camera.ShutterCallback() {
			@Override
			public void onShutter() {
				main.sendData();
			}
		}, null, null);
    }
    
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        if (camera != null) {
            // Call stopPreview() to stop updating the preview surface.
            camera.stopPreview();
        }
    }

    /**
     * When this function returns, mCamera will be null.
     */
    private void stopPreviewAndFreeCamera() {

        if (camera != null) {
            // Call stopPreview() to stop updating the preview surface.
            camera.stopPreview();
        
            // Important: Call release() to release the camera for use by other
            // applications. Applications should release the camera immediately
            // during onPause() and re-open() it during onResume()).
            camera.release();
        
            camera = null;
        }
    }

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		
	}
	

}
