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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.visenze.vimobile.demo.CameraActivity;
import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.WorkflowModel;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

public class CameraModeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "CameraModeFragment";
    private CameraActivity cameraActivity;
    private ImageView cameraButton;
    private WorkflowModel workflowModel;

    public CameraModeFragment() {
        // Required empty public constructor
    }

    public static CameraModeFragment newInstance() {
        return new CameraModeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera_mode, container, false);

        cameraButton = view.findViewById(R.id.camera_btn);
        cameraButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        cameraActivity = (CameraActivity) getActivity();
        setUpWorkflowModel(cameraActivity);

        workflowModel.markCameraFrozen();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setUpWorkflowModel(FragmentActivity activity) {
        workflowModel = ViewModelProviders.of(activity).get(WorkflowModel.class);

        workflowModel.currentMode.observe(
                this,
                cameraMode -> {
                    if (workflowModel.isCameraMode()) {
                        Log.d(TAG, "start camera preview");
                        cameraActivity.startCameraPreview();
                    }
                }
        );
    }

    private void takePicture() {
        Log.d(TAG, "takePicture");
        if (workflowModel.isCameraLive()) {
            cameraActivity.takePicture();
            workflowModel.markCameraFrozen();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_btn) {
            takePicture();
        }
    }
}
