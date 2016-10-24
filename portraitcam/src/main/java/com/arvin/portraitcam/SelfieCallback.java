package com.arvin.portraitcam;

import android.net.Uri;

/**
 * Created by Arvin Jayanake on 20/10/2016.
 */

interface SelfieCallback {

    public void onStartSaving();

    public void onSelfieTaken(Uri uri);

}
