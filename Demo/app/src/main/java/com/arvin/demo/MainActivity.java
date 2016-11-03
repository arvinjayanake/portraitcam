package com.arvin.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.arvin.portraitcam.PortraitCam;
import com.arvin.portraitcam.PortraitCamActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button btnBackCam;
    private Button btnFrontCam;
    private ImageView ivPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBackCam = (Button) findViewById(R.id.btnBackCam);
        btnFrontCam = (Button) findViewById(R.id.btnFrontCam);
        ivPic = (ImageView) findViewById(R.id.ivPic);

        btnBackCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PortraitCamActivity.class);
                i.putExtra(PortraitCam.CAM_MOD, PortraitCam.BACK_CAM);
                MainActivity.this.startActivityForResult(i, PortraitCam.CAPTURE_IMAGE);
            }
        });

        btnFrontCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PortraitCamActivity.class);
                i.putExtra(PortraitCam.CAM_MOD, PortraitCam.FRONT_CAM);
                MainActivity.this.startActivityForResult(i, PortraitCam.CAPTURE_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PortraitCam.CAPTURE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {

                String uriString = data.getStringExtra(PortraitCam.IMAGE_URI);
                Uri uri = Uri.parse(uriString);

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                    //Resize image for preview
                    Bitmap resized = scaleBitmap(bitmap, 800, true);
                    bitmap = null;

                    ivPic.setImageBitmap(resized);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Bitmap scaleBitmap(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
    }

}
