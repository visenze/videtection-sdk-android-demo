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

package com.visenze.vimobile.demo.ui.fragment;

import android.animation.AnimatorSet;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Objects;
import com.visenze.vimobile.demo.CameraActivity;
import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.WorkflowModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

public class LiveObjectDetectionFragment extends Fragment {

    private static final String TAG = "liveDetectionFragment";
    private CameraActivity cameraActivity;

    private AnimatorSet promptChipAnimator;

    private WorkflowModel workflowModel;
    private WorkflowModel.WorkflowState currentWorkflowState;


    public static LiveObjectDetectionFragment newInstance() {
        return new LiveObjectDetectionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_live_object_detection, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        cameraActivity = (CameraActivity) getActivity();
        setUpWorkflowModel(cameraActivity);

        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED;
        workflowModel.markCameraFrozen();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "on Resume");
        workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "on pause");
    }

    private void setUpWorkflowModel(FragmentActivity activity) {
        workflowModel = ViewModelProviders.of(activity).get(WorkflowModel.class);
        // Observes the workflow state changes, if happens, update the overlay view indicators and
        // camera preview state.
        workflowModel.workflowState.observe(
                this,
                workflowState -> {
                    if (workflowState == null || Objects.equal(currentWorkflowState, workflowState) || workflowModel.isCameraMode()) {
                        return;
                    }

                    currentWorkflowState = workflowState;
                    Log.d(TAG, "Current workflow state: " + currentWorkflowState.name());
                    stateChanged(workflowState);
                });

        workflowModel.currentMode.observe(
                this,
                cameraMode -> {
                    if (workflowModel.isLiveMode()) {
                        Log.d(TAG, "change camera mode to live");
                        workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING);
                    } else {
                        currentWorkflowState = WorkflowModel.WorkflowState.NOT_STARTED;
                    }
                });
    }

    private void stateChanged(WorkflowModel.WorkflowState workflowState) {

        switch (workflowState) {
            case DETECTING:
            case DETECTED:
            case CONFIRMING:
                cameraActivity.startCameraPreview();
                break;
            case CONFIRMED:
            case SEARCHED:
                cameraActivity.stopCameraPreview();
                break;
            default:
                break;
        }
    }
}
