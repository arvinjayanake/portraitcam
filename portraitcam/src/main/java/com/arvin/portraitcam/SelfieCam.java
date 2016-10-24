package com.arvin.portraitcam;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Arvin Jayanake on 20/10/2016.
 */

public class SelfieCam {

    private static final String PIC = "com.arvin.selfiecam.tempselfie";

    enum SelectedCam {
        FRONT_CAM, BACK_CAM
    };

    /**
     * Capture image result code
     */
    public static int CAPTURE_IMAGE = 193;

    /**
     * Use this attribute to select witch camera you want to load
     */
    public static String CAM_MOD = "CAM_MOD";

    /**
     * Cam mod front camera
     */
    public static String FRONT_CAM = "FRONT_CAM";

    /**
     * Cam mod back camera
     */
    public static String BACK_CAM = "BACK_CAM";

    /**
     * Captured image URI
     */
    public static String IMAGE_URI = "IMAGE_URI";

    static void saveImage(Context context, String imgData){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PIC, imgData);
        editor.commit();
    }

    /**
     * Get the selfie as bitmap
     * @param context
     * @return
     */
    public static Bitmap getSelfie(Context context){
        try {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String data = sharedPrefs.getString(PIC, "");
            if (data != null && !data.isEmpty()){
                return stringToBitmap(data);
            }else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    static Bitmap stringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    static String bitmapToString(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            String temp = Base64.encodeToString(b, Base64.DEFAULT);
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
