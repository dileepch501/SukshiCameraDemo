package com.sukshi.sukshicamerademo;

/*
 * Vishwam Corp CONFIDENTIAL

 * Vishwam Corp 2018
 * All Rights Reserved.

 * NOTICE:  All information contained herein is, and remains
 * the property of Vishwam Corp. The intellectual and technical concepts contained
 * herein are proprietary to Vishwam Corp
 * and are protected by trade secret or copyright law of U.S.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vishwam Corp
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.sukshi.sukshicamerademo.FaceDect.previewFaceDetector;


public class CameraActivity extends AppCompatActivity implements FaceDect.OnMultipleFacesDetectedListener, FaceDect.OnCaptureListener {

    private static final String TAG = "Custom Camera";
    private Context context;
    public CameraSource mCameraSource;

    // CAMERA VERSION ONE DECLARATIONS
    FaceDect faceDect;

    // COMMON TO BOTH CAMERAS
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private boolean wasActivityResumed = false;
    public static CardView bgcard;

    String username = "vishwam";

    ImageView previewImages,close;
    public static boolean takePicture;
    public static ImageView camera, camera2;
    public static TextView errorTxt;
    public static LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        context = getApplicationContext();
        takePicture = false;

        camera = findViewById(R.id.camera);
        camera2 = findViewById(R.id.preview);
        close=(ImageView)findViewById(R.id.close);
        layout=(LinearLayout)findViewById(R.id.layout);
        errorTxt = (TextView) findViewById(R.id.errortxt);
        RelativeLayout relativeLayout = findViewById(R.id.camRLayout);
        bgcard = (CardView) findViewById(R.id.bgcard);
        mPreview = findViewById(R.id.previewAuth);

        mGraphicOverlay = findViewById(R.id.faceOverlayAuth);
        createCameraSourceFront();
        startCameraSource();

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture = true;
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onMultipleFacesDetected(int n) {

    }

    @Override
    public void onCapture(byte[] data, int angle) {

        stopCameraSource();
        Bitmap OriginalBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotatedbitmap = Bitmap.createBitmap(OriginalBitmap, 0, 0, OriginalBitmap.getWidth(), OriginalBitmap.getHeight(), matrix, true);
        saveFile(rotatedbitmap);
    }


    public void saveFile(Bitmap bitmap) {

        File file = getOutputMediaFile();
        String path = file.getPath();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                    finish();
                    //startActivity(new Intent(CameraActivity.this, MainActivity.class));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createCameraSourceFront() {
        faceDect = new FaceDect(this, mGraphicOverlay);

        mCameraSource = new CameraSource.Builder(context, previewFaceDetector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

        startCameraSource();
    }

    private void startCameraSource() {

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                // Log.e(TAG, "Unable to start caera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void stopCameraSource() {
        mPreview.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wasActivityResumed) {
            createCameraSourceFront();
        }
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();

        wasActivityResumed = true;
        stopCameraSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCameraSource();
    }

    public File getOutputMediaFile() {

        final String TAG = "CameraPreview";

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Camera");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        long time = System.currentTimeMillis();
        File file = new File(mediaStorageDir.getPath() + File.separator + time + ".jpg");

        return file;
    }
}
