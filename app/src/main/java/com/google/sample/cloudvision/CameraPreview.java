package com.google.sample.cloudvision;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera = null;
    private boolean previewInitialized = false;

    Camera.PictureCallback photoCallback;

    public CameraPreview(Context context, Camera cam, Camera.PictureCallback pictureCallback) {
        super(context);
        //mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCamera = cam;
        photoCallback = pictureCallback;
    }

    public void setCamera(Camera camera) {
        if (mCamera != null) {
            mCamera = camera;
        }
    }


    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            previewInitialized = true;
            Log.d("Camera Preview","started preview");
        } catch (IOException e) {
            Log.d("Camera preview", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
            previewInitialized = false;
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            previewInitialized = true;

        } catch (Exception e){
            Log.d("Camera preview", "Error starting camera preview: " + e.getMessage());
        }
    }

    public boolean canTakePicture() {
        return previewInitialized;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Toast.makeText(getContext(), "Touch event", Toast.LENGTH_SHORT).show();
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                //if (previewInitialized) {
                    Log.d("PICTURE","taking picture");
                    mCamera.takePicture(null, null, photoCallback);
                    previewInitialized = false;
                //}

            default:
                break;
        }
        return true;
    }
}