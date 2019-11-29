/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2019 ViSenze Pte. Ltd.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.visenze.vimobile.demo.objectdetection;

import android.util.Log;

import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.WorkflowModel;
import com.visenze.vimobile.demo.objectdetection.controller.ObjectConfirmationController;
import com.visenze.vimobile.demo.objectdetection.controller.TrackingController;
import com.visenze.vimobile.demo.objectdetection.model.ViDetectedObject;
import com.visenze.vimobile.demo.objectdetection.view.ObjectCircleAnimator;
import com.visenze.vimobile.demo.objectdetection.view.ObjectCircleRippleGraphic;
import com.visenze.vimobile.demo.objectdetection.view.ObjectScanningBoxGraphic;
import com.visenze.vimobile.demo.utils.DeviceUtils;
import com.visenze.videtection.camera.GraphicOverlay;
import com.visenze.videtection.camera.ViFrameProcessor;
import com.visenze.videtection.camera.ViImage;
import com.visenze.videtection.model.ViBoundingBox;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.MainThread;

public class MainObjectProcessor extends ViFrameProcessor {
    private static final String TAG = "MainObjectProcessor";

    private final WorkflowModel workflowModel;
    private final ObjectConfirmationController confirmationController;
    private final TrackingController trackingController;
    private final ObjectCircleAnimator objectCircleAnimator;

    public MainObjectProcessor(GraphicOverlay graphicOverlay, WorkflowModel workflowModel) {
        this.workflowModel = workflowModel;
        confirmationController = new ObjectConfirmationController(graphicOverlay);
        objectCircleAnimator = new ObjectCircleAnimator(graphicOverlay);

        int radius = graphicOverlay.getResources().getDimensionPixelOffset(R.dimen.object_circle_ripple_radius);
        int sdLimit = graphicOverlay.getResources().getDimensionPixelOffset(R.dimen.live_mode_sd_limit);
        trackingController = new TrackingController(radius, sdLimit);
    }

    @MainThread
    protected void onSuccess(ViImage image, List<ViBoundingBox> results, GraphicOverlay graphicOverlay) {
        Log.d(TAG, "onSuccess executed");

        if (!workflowModel.isCameraLive()) {
            Log.d(TAG, "camera is not alive");
            confirmationController.reset();
            return;
        }

        if (!workflowModel.isLiveMode()) {
            Log.d(TAG, "Not live mode, clear all states and UI");
            graphicOverlay.clear();
            confirmationController.reset();
            return;
        }

        List<ViDetectedObject> objects = new ArrayList<ViDetectedObject>();

        for (int i = 0; i < results.size(); i++) {
            boolean isPortraitMode = DeviceUtils.isPortraitMode(workflowModel.getApplication());
            boolean shouldRotateBoundingBox = false;

            // should rotate the bounding box if image rotation detected is different from the display orientation
            if (isPortraitMode && !(image.getRotation() == 90 || image.getRotation() == 270)
                    || !isPortraitMode && !(image.getRotation() == 0 || image.getRotation() == 180)) {
                shouldRotateBoundingBox = true;
            }

            ViDetectedObject obj = new ViDetectedObject(results.get(i), i, image, shouldRotateBoundingBox);
            objects.add(obj);
        }

        objectCircleAnimator.start();
        graphicOverlay.clear();

        if (!objects.isEmpty()) {
            int objectIndex = 0;
            ViDetectedObject selectedObject = objects.get(objectIndex);
            trackingController.addObject(graphicOverlay, selectedObject);

            if (trackingController.objectBoxOverlapConfirmReticle(graphicOverlay, selectedObject)) {
                startScan(graphicOverlay, selectedObject);
            } else {
                addDotAndScanBox(graphicOverlay, selectedObject);
            }

        } else {
            addDetectingCircle(graphicOverlay);
            workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING);
        }
        graphicOverlay.invalidate();
    }

    private void startScan(GraphicOverlay graphicOverlay, ViDetectedObject selectedObject) {
        objectCircleAnimator.cancel();
        // add scanning box
        graphicOverlay.add(new ObjectScanningBoxGraphic(graphicOverlay, selectedObject, confirmationController));

        if (trackingController.isStable() && selectedObject.getObjectId() != null) {
            confirmationController.confirming(selectedObject.getObjectId());
            workflowModel.confirmingObject(selectedObject, confirmationController.getProgress());
        } else {
            confirmationController.reset();
        }
        if (confirmationController.isConfirmed()) {
            confirmationController.reset();
        }
    }

    private void addDotAndScanBox(GraphicOverlay graphicOverlay, ViDetectedObject selectedObject) {
        // add dot
        graphicOverlay.add(new ObjectCircleRippleGraphic(graphicOverlay, objectCircleAnimator));
        objectCircleAnimator.start();
        // add scanning box
        graphicOverlay.add(new ObjectScanningBoxGraphic(graphicOverlay, selectedObject, confirmationController));
        confirmationController.reset();
        workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTED);
    }

    private void addDetectingCircle(GraphicOverlay graphicOverlay) {
        graphicOverlay.add(new ObjectCircleRippleGraphic(graphicOverlay, objectCircleAnimator));
        objectCircleAnimator.start();
        confirmationController.reset();
    }

    @Override
    protected void onFailure() {
        Log.e(TAG, "Object detection failed!");
    }
}
