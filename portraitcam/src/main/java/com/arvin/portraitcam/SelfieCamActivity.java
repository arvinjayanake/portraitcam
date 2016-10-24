package com.arvin.portraitcam;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class SelfieCamActivity extends AppCompatActivity implements SelfieCallback {

    private final String TAG = "SelfieCamActivity";
    private final int PERMISSION_REQUEST = 954;

    private CamController cc;

    private ImageView ivCapture;

    private ProgressDialog dialog;

    private SelfieCam.SelectedCam selectedCam = SelfieCam.SelectedCam.FRONT_CAM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_cam);
        initComponents();
    }

    private void initComponents() {
        if (getIntent().hasExtra(SelfieCam.CAM_MOD)){
            String camMod = getIntent().getStringExtra(SelfieCam.CAM_MOD);
            if (camMod.equals(SelfieCam.BACK_CAM)){
                selectedCam = SelfieCam.SelectedCam.BACK_CAM;
            }
        }

        if (isPermissionGranted()) {
            startCameraIntent();
        } else {
            requestPermission();
        }

        ivCapture = (ImageView) findViewById(R.id.ivCapture);
        ivCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cc.hasCamera()) {
                    cc.takePicture();
                } else {
                    Log.e(TAG, "Device does not have front camera");
                }
            }
        });
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(getResources().getString(R.string.permission_denied));
        builder.setMessage(getResources().getString(R.string.permission_denied_txt));

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                SelfieCamActivity.this.finish();
            }
        });

        builder.create().show();
    }

    private boolean shouldShowExplanation() {
        boolean cam = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
        boolean readStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean writeStorage = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (cam || readStorage || writeStorage) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST);
    }

    private void startCameraIntent() {
        cc = new CamController(this, this, selectedCam);

        if (cc.hasCamera()) {
            cc.getCameraInstance();

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(cc.getCameraPreview());
        } else {
            ivCapture.setVisibility(View.GONE);
            Log.e(TAG, "Device does not have front camera");
        }
    }

    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length >= 3
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                startCameraIntent();
            } else {
                showPermissionDeniedDialog();
                //showToast("Permission Denied");
                ivCapture.setVisibility(View.GONE);
            }
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (cc != null){
            cc.releaseCamera();
        }
    }

    @Override
    public void onStartSaving() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (dialog != null){
            dialog = null;
        }

        dialog = ProgressDialog.show(SelfieCamActivity.this, "Saving", "Please wait...", true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSelfieTaken(Uri uri) {
        //Scanner
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
            dialog = null;
        }

        Intent intent = new Intent();
        intent.putExtra(SelfieCam.IMAGE_URI, uri.toString());
        setResult(RESULT_OK, intent);

        SelfieCamActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
