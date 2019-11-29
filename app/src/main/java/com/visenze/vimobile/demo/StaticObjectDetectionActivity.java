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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.visenze.videtection.ViMobileSDKManager;
import com.visenze.vimobile.demo.objectdetection.model.DetectedObject;
import com.visenze.vimobile.demo.objectdetection.model.ViDetectedObject;
import com.visenze.vimobile.demo.productsearch.SearchEngine;
import com.visenze.vimobile.demo.productsearch.model.Product;
import com.visenze.vimobile.demo.productsearch.model.SearchedObject;
import com.visenze.vimobile.demo.productsearch.view.EditPhotoView;
import com.visenze.vimobile.demo.productsearch.view.EditableImage;
import com.visenze.vimobile.demo.productsearch.view.model.ScalableBox;
import com.visenze.vimobile.demo.ui.view.ProductListLayout;
import com.visenze.vimobile.demo.utils.ImageUtils;
import com.visenze.videtection.camera.ViImage;
import com.visenze.videtection.model.ViBoundingBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Demonstrates the object detection and visual search workflow using static image.
 */
public class StaticObjectDetectionActivity extends AppCompatActivity
        implements View.OnClickListener, SearchEngine.SearchResultListener {

    private static final String TAG = "StaticObjectActivity";
    private static final int MAX_IMAGE_DIMENSION = 1024;

    // ui elements
    private ViewGroup viewContainer;
    private ImageButton backButton;
    private EditPhotoView editPhotoView;
    private EditableImage editableImage;
    private ImageView thumbnailImage;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    // image search engine and models
    private Bitmap inputBitmap;
    private ViImage visenzeImage;
    private SearchEngine searchEngine;

    // search result ui elements
    private ViewGroup productListContainer;
    private ProductListLayout productListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchEngine = new SearchEngine(getApplicationContext());

        setContentView(R.layout.activity_static_object);

        backButton = findViewById(R.id.back_btn);
        backButton.setOnClickListener(this);

        viewContainer = findViewById(R.id.view_container);
        editPhotoView = findViewById(R.id.edit_photo_view);

        setUpBottomSheet();

        ImageView closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(this);

        findViewById(R.id.photo_library_button).setOnClickListener(this);

        if (getIntent().getData() != null) {
            detectObjects(getIntent().getData());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searchEngine.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ImageUtils.REQUEST_CODE_PHOTO_LIBRARY
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            detectObjects(data.getData());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.close_button) {
            onBackPressed();
        } else if (id == R.id.photo_library_button) {
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
            ImageUtils.openImagePicker(this);
        } else if (id == R.id.back_btn) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void showSearchResults(SearchedObject searchedObject) {
        int width = thumbnailImage.getWidth();
        int height = thumbnailImage.getHeight();
        Bitmap bmp = searchedObject.getFixSizeThumbnail(width, height);
        thumbnailImage.setImageBitmap(bmp);

        bottomSheetBehavior.setPeekHeight(((View) viewContainer.getParent()).getHeight() / 2);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        productListLayout.updateProductList(getApplicationContext(), searchedObject.getProductList());
    }

    private void setUpBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        productListContainer = findViewById(R.id.product_list_container);
        thumbnailImage = findViewById(R.id.thumb_img);
        productListLayout = new ProductListLayout(getApplicationContext());
        View view = productListLayout.initView(getApplicationContext());
        productListContainer.addView(view);

        // config bottom sheet behavior
        bottomSheetBehavior.setBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        Log.d(TAG, "Bottom sheet new state: " + newState);
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            productListLayout.clearProductList(getApplicationContext());
                            editPhotoView.enableCropping(true);
                        } else {
                            editPhotoView.enableCropping(false);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        // do nothing
                    }
                });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void detectObjects(Uri imageUri) {
        try {
            inputBitmap = ImageUtils.loadImage(this, imageUri, MAX_IMAGE_DIMENSION);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load file: " + imageUri, e);
            return;
        }

        List<ViBoundingBox> result = ViMobileSDKManager.getInstance().detect(inputBitmap);

        showDetectedObjects(inputBitmap, result);
    }

    private void showDetectedObjects(Bitmap bmp, List<ViBoundingBox> res) {
        if (res.size() == 0) {
            // if there's no detected box, the whole image should be the bounding box
            ViBoundingBox bb = ViBoundingBox.fromBoxInfo(0, bmp.getWidth(), 0, bmp.getHeight());
            res.add(bb);
        }
        editableImage = new EditableImage(bmp);
        List<ScalableBox> boxes = getDetectionBoxes(res);
        editableImage.setBoxes(boxes);
        editPhotoView.initView(this, editableImage);

        visenzeImage = ViImage.fromBitmap(bmp);
        DetectedObject primaryObj = new ViDetectedObject(res.get(0), 0, visenzeImage, false);
        searchEngine.search(primaryObj, this);

        editPhotoView.setOnBoxChangedListener((x1, y1, x2, y2) -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                // if further actions performed during waiting, discard the callback
                if (editableImage.isConfirming()) {
                    return;
                }
                // Actions to do after 1 seconds
                bottomSheetBehavior.setPeekHeight(((View) viewContainer.getParent()).getHeight() / 2);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                productListLayout.showProgress();

                ViBoundingBox box = ViBoundingBox.fromBoxInfo(x1, x2, y1, y2);
                DetectedObject obj = new ViDetectedObject(box, 0, visenzeImage, false);
                searchEngine.search(obj, this);
            }, 1000);
        });

        // pull up bottom view in first entry.
        editPhotoView.setScanFinishedListener(() -> {
            bottomSheetBehavior.setPeekHeight(((View) viewContainer.getParent()).getHeight() / 2);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            productListLayout.hideProgress();
        });
    }

    private List<ScalableBox> getDetectionBoxes(List<ViBoundingBox> boxes) {
        List<ScalableBox> b = new ArrayList<>();
        for (ViBoundingBox box : boxes) {
            ScalableBox searchBox = new ScalableBox();
            if (box != null) {
                searchBox.setX1(box.x1);
                searchBox.setX2(box.x2);
                searchBox.setY1(box.y1);
                searchBox.setY2(box.y2);
                b.add(searchBox);
            }
        }
        return b;
    }

    @Override
    public void onSearchCompleted(DetectedObject object, List<Product> productList, String errorMessage) {
        if (errorMessage != null) {
            productListLayout.updateErrorMessage("Failed to get search results: " + errorMessage);
        } else {
            SearchedObject searchedObject = new SearchedObject(getResources(), object, productList);
            showSearchResults(searchedObject);
        }
    }
}
