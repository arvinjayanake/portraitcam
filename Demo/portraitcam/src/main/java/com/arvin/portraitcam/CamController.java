package com.arvin.portraitcam;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Arvin Jayanake on 20/10/2016.
 */

public class CamController {

    private final String TAG = "CamController";
    private final String SAVE_DIR_NAME = "Camera";

    private Activity activity;

    private Camera camera;

    private boolean hasCamera;
    private int cameraId;
    protected List<Size> mPictureSizeList;
    private CameraPreview cameraPreview;

    private PortraitCamCallback selfieCallback;
    private PortraitCam.SelectedCam selectedCam;

    private static int mRotation;

    public CamController(Activity activity, PortraitCamCallback selfieCallback, PortraitCam.SelectedCam selectedCam) {
        this.activity = activity;
        this.selfieCallback = selfieCallback;
        this.selectedCam = selectedCam;

        if (activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (selectedCam == PortraitCam.SelectedCam.FRONT_CAM){
                cameraId = getFrontCameraId();
            }else {
                cameraId = getBackCameraId();
            }

            if (cameraId != -1) {
                hasCamera = true;
            } else {
                hasCamera = false;
            }
        } else {
            hasCamera = false;
        }
    }

    public boolean hasCamera() {
        return hasCamera;
    }

    public void getCameraInstance() {
        camera = null;

        if (hasCamera) {
            try {
                camera = Camera.open(cameraId);
                prepareCamera();
            } catch (Exception e) {
                Log.i(TAG, e.toString());
                hasCamera = false;
            }
        }
    }

    public void takePicture() {
        if (hasCamera) {
            camera.takePicture(null, null, mPicture);
        }
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private int getBackCameraId() {
        int camId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo ci = new CameraInfo();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_BACK) {
                camId = i;
            }
        }

        return camId;
    }

    private int getFrontCameraId() {
        int camId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo ci = new CameraInfo();

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) {
                camId = i;
            }
        }

        return camId;
    }


    public CameraPreview getCameraPreview() {
        return cameraPreview;
    }

    /**
     * Call this method in activity
     */
    public void onPause(){
        if (cameraPreview != null){
            cameraPreview.getHolder().removeCallback(cameraPreview);
        }
    }

    private void prepareCamera() {
        cameraPreview = new CameraPreview(activity, camera);

        SurfaceView view = new SurfaceView(activity);

        try {
            camera.setPreviewDisplay(view.getHolder());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Camera.Parameters parameters = camera.getParameters();
        //parameters.set("orientation", "portrait");
        //parameters.setRotation(90);
        //parameters.setJpegQuality(100);

//        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
//
//        Camera.Size size = sizes.get(0);
//        for (int i = 0; i < sizes.size(); i++) {
//            if (sizes.get(i).width > size.width)
//                size = sizes.get(i);
//        }

        //parameters.setPictureSize(size.width, size.height);

        //Auto focus
        //parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        //if (parameters.getSupportedFocusModes().contains(
        //        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
        //    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        //}
        //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);


        //camera.setParameters(parameters);
        //camera.setDisplayOrientation(90);

        setCameraDisplayOrientation(activity, cameraId, camera);
        //camera.setDisplayOrientation(90);

        try {
            Parameters parameters = camera.getParameters();
            List<Size> sizes = parameters.getSupportedPictureSizes();

            Size size = sizes.get(0);
            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i).width > size.width)
                    size = sizes.get(i);
            }

            parameters.setJpegQuality(100);
            parameters.setPictureSize(size.width, size.height);
            parameters.set("orientation", "portrait");
            parameters.setRotation(90);

            //Some devices does not support focus mode
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            camera.setParameters(parameters);
        }catch (Exception ex){
            Parameters parameters = camera.getParameters();
            List<Size> sizes = parameters.getSupportedPictureSizes();

            Size size = sizes.get(0);
            for (int i = 0; i < sizes.size(); i++) {
                if (sizes.get(i).width > size.width)
                    size = sizes.get(i);
            }

            parameters.setJpegQuality(100);
            parameters.setPictureSize(size.width, size.height);
            parameters.set("orientation", "portrait");
            parameters.setRotation(90);

            camera.setParameters(parameters);
        }

        camera.startPreview();
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        mRotation = result;

        camera.setDisplayOrientation(result);
    }

    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveTask(){
                @Override
                protected void onPostExecute(Uri uri) {
                    releaseCamera();

                    if (selfieCallback != null){
                        selfieCallback.onSelfieTaken(uri);
                    }
                }
            }.execute(data);

            if (selfieCallback != null){
                selfieCallback.onStartSaving();
            }
        }
    };


    //save task
    private class SaveTask extends AsyncTask<Object, Object, Uri> {

        @Override
        protected Uri doInBackground(Object... params) {
            byte[] data = (byte[]) params[0];

            //Crate File
            File pictureFile = getOutputMediaFile();

            if(pictureFile == null){
                Log.d("TEST", "Error creating media file, check storage permissions");
                return null;
            }

            //Get Image
            Bitmap original = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;

            //Rotate
            Bitmap rotatedBitmap = null;

            if (selectedCam == PortraitCam.SelectedCam.FRONT_CAM){
                if (original.getWidth() < original.getHeight()){
                    rotatedBitmap = rotate(original, 180);
                }else {
                    rotatedBitmap = rotate(original, 270);
                }
            }else {
                if (mRotation == 270){
                    //this will execute in nexus devices
                    if (original.getWidth() < original.getHeight()){
                        rotatedBitmap = rotate(original, 180);
                    }else {
                        rotatedBitmap = rotate(original, 270);
                    }
                }else{
                    //samsung landscape
                    if (original.getWidth() < original.getHeight()){
                        rotatedBitmap = rotate(original, 0);
                    }else {
                        rotatedBitmap = rotate(original, 90);
                    }
                }
            }

            original = null;

            //Convert ti bytes
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            byte[] byteArray = out.toByteArray();

            rotatedBitmap = null;

            //Write file
            try{
                Log.d("TEST","File created");
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(byteArray);
                fos.flush();
                fos.close();
                out.close();
            }catch(FileNotFoundException e){
                Log.d("TEST","File not found: "+e.getMessage());
            } catch (IOException e){
                Log.d("TEST","Error accessing file: "+e.getMessage());
            }

            out = null;
            byteArray = null;
            //save on sp
            //String temp = Base64.encodeToString(byteArray, Base64.DEFAULT);
            //SelfieCam.saveImage(context, temp);

            return Uri.fromFile(pictureFile);
        }

        private File getOutputMediaFile() {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), SAVE_DIR_NAME);

            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            File mediaFile;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

            return mediaFile;
        }

        private Bitmap rotate(Bitmap bitmapOrg, int x) {
            int width = bitmapOrg.getWidth();
            int height = bitmapOrg.getHeight();

            Matrix matrix = new Matrix();
            matrix.postRotate(x);

            return Bitmap.createBitmap(bitmapOrg, 0, 0,width, height, matrix, true);
        }
    }






}

