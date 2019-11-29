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

package com.visenze.vimobile.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.objectdetection.MainObjectProcessor;
import com.visenze.vimobile.demo.productsearch.SearchEngine;
import com.visenze.vimobile.demo.ui.fragment.CameraModeFragment;
import com.visenze.vimobile.demo.ui.fragment.LiveObjectDetectionFragment;
import com.visenze.vimobile.demo.ui.view.ProductListLayout;
import com.visenze.vimobile.demo.utils.ImageUtils;
import com.visenze.videtection.camera.CameraSourcePreview;
import com.visenze.videtection.camera.GraphicOverlay;
import com.visenze.visearch.android.model.Image;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener, Camera.PictureCallback {

    private static final String TAG = "CameraActivity";
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private ImageView thumbnailImage;

    // view model
    private WorkflowModel workflowModel;
    private SearchEngine searchEngine;
    FrameLayout fragmentContainer;

    private TextView liveMode;
    private TextView cameraMode;

    private View flashButton;
    private View albumButton;

    // private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    // live detection and camera fragments
    private LiveObjectDetectionFragment liveObjectDetectionFragment;
    private CameraModeFragment cameraModeFragment;

    // search result product view
    private ViewGroup productListViewContainer;
    private ProductListLayout productListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        fragmentContainer = findViewById(R.id.fragment_container);
        liveMode = findViewById(R.id.live_mode);
        cameraMode = findViewById(R.id.camera_mode);
        flashButton = findViewById(R.id.flash_button);
        albumButton = findViewById(R.id.album_button);

        liveMode.setOnClickListener(this);
        cameraMode.setOnClickListener(this);
        flashButton.setOnClickListener(this);
        albumButton.setOnClickListener(this);

        searchEngine = new SearchEngine(getApplicationContext());
        setUpBottomSheet();
        setUpWorkflowModel();

        preview = findViewById(R.id.detection_camera_preview);
        graphicOverlay = preview.getGraphicOverlay();
        preview.setFrameProcessor(new MainObjectProcessor(graphicOverlay, workflowModel));
        graphicOverlay.setOnClickListener(this);

        liveObjectDetectionFragment = LiveObjectDetectionFragment.newInstance();
        cameraModeFragment = CameraModeFragment.newInstance();

        if (savedInstanceState == null) {
            workflowModel.setCurrentMode(WorkflowModel.CameraMode.LIVE);
            switchCameraMode(WorkflowModel.CameraMode.LIVE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (preview.getCameraSource() == null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            Log.d(TAG, "on resume re-init cameraSource");
            preview.initCameraSource(new MainObjectProcessor(graphicOverlay, workflowModel));
            startCameraPreview();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        stopCamera();
        flashButton.setSelected(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        searchEngine.shutdown();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            if (preview.getCameraSource() == null) {
                Log.d(TAG, "on resume re-init cameraSource");
                preview.initCameraSource(new MainObjectProcessor(graphicOverlay, workflowModel));
                startCameraPreview();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // shift the rotation value as the visearch sdk accepts rotation with respect to landscape
        int rotation = (preview.getCameraSource().getRotation() + 270) % 360;
        final Image image = new Image(data, Image.ResizeSettings.CAMERA_HIGH, rotation);
        //save to local path
        String path = null;
        try {
            path = ImageUtils.saveImageByte(image.getByteArray());
        } catch (IOException e) {
            Log.e(TAG, "failed to store the images after taking photo");
        }
        Log.d(TAG, path);
        Uri uri = Uri.fromFile(new File(path));
        Intent intent = new Intent(this, StaticObjectDetectionActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ImageUtils.REQUEST_CODE_PHOTO_LIBRARY
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {
            Uri uri = data.getData();
            Intent intent = new Intent(this, StaticObjectDetectionActivity.class);
            intent.setData(uri);
            startActivity(intent);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void startCameraPreview() {
        if (!workflowModel.isCameraLive() && preview.getCameraSource() != null) {
            Log.d(TAG, "startCameraPreview");
            try {
                workflowModel.markCameraLive();
                Log.d(TAG, "mode: " + workflowModel.isLiveMode());
                preview.start(workflowModel.isLiveMode());
            } catch (IOException e) {
                Log.e(TAG, "Failed to start camera preview!", e);
                preview.release();
            }
        }
    }

    public void takePicture() {
        Log.d(TAG, "takePicture");
        if (workflowModel.isCameraLive()) {
            preview.getCameraSource().takePicture(this);
            workflowModel.markCameraFrozen();
        }
    }

    public void stopCameraPreview() {
        if (workflowModel.isCameraLive()) {
            Log.d(TAG, "stopCameraPreview");
            workflowModel.markCameraFrozen();
            flashButton.setSelected(false);
            preview.stop();
        }
    }

    public void stopCamera() {
        Log.d(TAG, "stopCamera");
        workflowModel.markCameraFrozen();
        flashButton.setSelected(false);
        preview.release();
    }

    private void switchCameraMode(WorkflowModel.CameraMode mode) {
        if (mode.equals(WorkflowModel.CameraMode.LIVE)) {
            liveMode.setTextColor(getResources().getColor(R.color.white));
            cameraMode.setTextColor(getResources().getColor(R.color.text_disabled_color));

            preview.getCameraSource().startProcessing();
            showLiveModeFragment();
        } else if (mode.equals(WorkflowModel.CameraMode.CAMERA)) {
            liveMode.setTextColor(getResources().getColor(R.color.text_disabled_color));
            cameraMode.setTextColor(getResources().getColor(R.color.white));

            preview.getCameraSource().stopProcessing();
            graphicOverlay.clear();
            graphicOverlay.invalidate();
            showCameraModeFragment();
        }
    }

    private void showLiveModeFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (liveObjectDetectionFragment.isAdded()) {
            ft.show(liveObjectDetectionFragment);
        } else {
            ft.add(R.id.fragment_container, liveObjectDetectionFragment);
        }

        if (cameraModeFragment.isAdded()) {
            ft.hide(cameraModeFragment);
        }

        ft.commit();
    }

    private void showCameraModeFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (cameraModeFragment.isAdded()) {
            ft.show(cameraModeFragment);
        } else {
            ft.add(R.id.fragment_container, cameraModeFragment);
        }

        if (liveObjectDetectionFragment.isAdded()) {
            ft.hide(liveObjectDetectionFragment);
        }
        ft.commit();
    }

    private void setUpWorkflowModel() {
        // initialize viewModel, all fragment in this activity share the same workflowModel instance.
        // only observe state change,
        workflowModel = ViewModelProviders.of(this).get(WorkflowModel.class);

        // Observes changes on the object to search, if happens, fire product search request.
        workflowModel.objectToSearch.observe(
                this,
                object -> {
                    searchEngine.search(object, workflowModel);
                    // update bottom sheet thumbnail image
                    int width = thumbnailImage.getWidth();
                    int height = thumbnailImage.getHeight();
                    int cornerRadius = getResources().getDimensionPixelOffset(R.dimen.bounding_box_corner_radius);
                    Bitmap objectThumbnail = ImageUtils.getFixSizeRoundCornerBitmap(object.getSquaredThumbnailBitmap(), width, height, cornerRadius);
                    thumbnailImage.setImageBitmap(objectThumbnail);
                    bottomSheetBehavior.setPeekHeight(preview.getHeight() / 2);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    productListLayout.showProgress();
                });

        // Observes changes on the object that has search completed, if happens, show the bottom sheet
        // to present search result.
        workflowModel.searchedObject.observe(
                this,
                searchedObject -> {
                    if (searchedObject != null) {
                        productListLayout.hideProgress();
                        productListLayout.updateSearchResult(getApplicationContext(), searchedObject.getProductList());
                    }
                });

        workflowModel.searchError.observe(
                this,
                errorMessage -> {
                    productListLayout.hideProgress();
                    productListLayout.updateErrorMessage("Failed to get search results: " + errorMessage);
                }
        );
    }

    private void setUpBottomSheet() {
        findViewById(R.id.back_btn).setOnClickListener(this);
        productListViewContainer = findViewById(R.id.product_list_container);
        thumbnailImage = findViewById(R.id.thumb_img);
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        // create product list layout and add to the bottom sheet container
        productListLayout = new ProductListLayout(getApplicationContext());
        View view = productListLayout.initView(getApplicationContext());
        productListViewContainer.addView(view);

        bottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        Log.d(TAG, "Bottom sheet new state: " + newState);
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                // notify fragment to restart camera etc.
                                productListLayout.clearProductList(getApplicationContext());

                                if (preview.getCameraSource() == null) {
                                    Log.d(TAG, "on resume re-init cameraSource");
                                    preview.initCameraSource(new MainObjectProcessor(graphicOverlay, workflowModel));
                                    startCameraPreview();
                                }

                                workflowModel.setWorkflowState(WorkflowModel.WorkflowState.DETECTING);
                                break;
                            case BottomSheetBehavior.STATE_COLLAPSED:
                            case BottomSheetBehavior.STATE_EXPANDED:
                            case BottomSheetBehavior.STATE_HALF_EXPANDED:
                                break;
                            case BottomSheetBehavior.STATE_DRAGGING:
                            case BottomSheetBehavior.STATE_SETTLING:
                            default:
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        // do nothing
                    }
                });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.flash_button) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                if (flashButton.isSelected()) {
                    flashButton.setSelected(false);
                    preview.getCameraSource().updateFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    flashButton.setSelected(true);
                    preview.getCameraSource().updateFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                flashButton.setSelected(false);
            }
        } else if (id == R.id.album_button) {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            ImageUtils.openImagePicker(this);
        } else if (id == R.id.back_btn) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            if (preview.getCameraSource() == null) {
                Log.d(TAG, "on resume re-init cameraSource");
                preview.initCameraSource(new MainObjectProcessor(graphicOverlay, workflowModel));
                startCameraPreview();
            }
        } else if (id == R.id.live_mode) {
            if (!workflowModel.isLiveMode()) {
                workflowModel.setCurrentMode(WorkflowModel.CameraMode.LIVE);
                switchCameraMode(WorkflowModel.CameraMode.LIVE);
            }
        } else if (id == R.id.camera_mode) {
            if (!workflowModel.isCameraMode()) {
                workflowModel.setCurrentMode(WorkflowModel.CameraMode.CAMERA);
                switchCameraMode((WorkflowModel.CameraMode.CAMERA));
            }
        }
    }
}
