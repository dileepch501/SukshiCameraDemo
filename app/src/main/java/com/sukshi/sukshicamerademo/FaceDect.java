package com.sukshi.sukshicamerademo;

/**
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.Arrays;
import java.util.List;

public class FaceDect {

    private Context context;

    public static boolean detectorAvailable = true;

    public static OnMultipleFacesDetectedListener onMultipleFacesDetectedListener;
    public static OnCaptureListener onCaptureListener;

    public static FaceDetector previewFaceDetector = null;
    private GraphicOverlay mGraphicOverlay;
    private FaceGraphic mFaceGraphic;
    int range[] ={-3,-2,-1,0,1,2,3};
    /**
     * Interface callback on multiple faces detected with face count.
     * **/
    public interface OnMultipleFacesDetectedListener {
        void onMultipleFacesDetected(int n);
    }
    /**
     * Interface callback return captured image byte array and angle of orientation image.
     * **/
    public interface OnCaptureListener {
        void onCapture(byte[] data, int angle);
    }
    /**
     * Constructor initialise context and graphicoverlay objects and initialising multiple face interface and oncapture interface.
     *
     * **/
    public FaceDect(Context mcontext, GraphicOverlay graphicOverlay) {

        this.context = mcontext;
        mGraphicOverlay = graphicOverlay;

        initialisefaceDetec();
//        this.mOnFrontalFaceDetectedListener = (OnFrontalFaceDetectedListener) context;
        this.onMultipleFacesDetectedListener = (OnMultipleFacesDetectedListener) context;
        this.onCaptureListener= (OnCaptureListener) context;
    }

    public FaceDect(Context mcontext) {

        this.context = mcontext;

        initialisefaceDetec();

    }
    /**
     * This  method used to initialise FaceDetector and set it with mutliprocessor using its Builder class.
     * Multiprocessor is a class to handle high speed frames stream optimised to use multiple processors.
     * **/
    public void initialisefaceDetec() {

        previewFaceDetector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(false)
                .setTrackingEnabled(true)
                .build();

        if (previewFaceDetector.isOperational()) {
            detectorAvailable = true;
            previewFaceDetector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
        } else {
            detectorAvailable = false;
        }
    }

    /**
     * This is class implementing mutilprocessorfactory.on create it then passes on newly create graphicfacetracker class.
     * **/
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {

        private GraphicOverlay mOverlay;
        /**
         * Constructor to initialise graphicovelay.
         * **/
        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, context);

        }
        /**
         * on new face detected this method is called to add new face item.
         * **/
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
            CameraActivity.layout.setVisibility(View.VISIBLE);
            CameraActivity.camera.setVisibility(View.VISIBLE);
        }
        /**
         * Ondetector detected is this method is called and is used to get landmarks.
         * Using landmarks to find the slope of two eye points and calculating the rx and ry.
         * FaceGraphic is then updated with face object.
         * **/

        @SuppressLint("ResourceAsColor")
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, final Face face) {

            List<Landmark> landmarks = face.getLandmarks();
            int landmarksCount = landmarks.size();
            float rotation = face.getEulerZ();
            int rotateint = (int) rotation;
            System.out.println("rotation:" + rotateint);
            int val = Arrays.binarySearch(range, rotateint);
            if (val > 0) {
                Log.e("landmarksCount", String.valueOf(landmarksCount));
                CameraActivity.bgcard.setCardBackgroundColor(Color.GREEN);
              /*  CameraActivity.camera2.setVisibility(View.GONE);
                CameraActivity.errorTxt.setText(context.getResources().getString(R.string.straightgone));*/
              CameraActivity.layout.setVisibility(View.INVISIBLE);
                CameraActivity.camera.setVisibility(View.VISIBLE);
            }else {
                CameraActivity.bgcard.setCardBackgroundColor(Color.RED);
                CameraActivity.camera.setVisibility(View.INVISIBLE);
                CameraActivity.layout.setVisibility(View.VISIBLE);
               /* CameraActivity.camera2.setVisibility(View.VISIBLE);
                CameraActivity.errorTxt.setText(context.getResources().getString(R.string.straight));*/
            }
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }
        /**
         * On No face detected is method is called and previously inflated graphic overlay is removed.
         * **/
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            CameraActivity.bgcard.setCardBackgroundColor(Color.WHITE);
            CameraActivity.layout.setVisibility(View.INVISIBLE);
            CameraActivity.camera.setVisibility(View.INVISIBLE);
            mFaceGraphic.goneFace();
            mOverlay.remove(mFaceGraphic);
            mOverlay.clear();
        }
        /**
         * OnDone overlay is removed.
         * **/

        @Override
        public void onDone() {

            mFaceGraphic.goneFace();
            mOverlay.remove(mFaceGraphic);
            mOverlay.clear();
        }
    }


}