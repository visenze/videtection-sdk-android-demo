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

import android.app.Application;
import android.content.Context;

import com.visenze.vimobile.demo.objectdetection.model.DetectedObject;
import com.visenze.vimobile.demo.productsearch.SearchEngine;
import com.visenze.vimobile.demo.productsearch.model.Product;
import com.visenze.vimobile.demo.productsearch.model.SearchedObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * View model for handling application workflow based on camera preview.
 */
public class WorkflowModel extends AndroidViewModel implements SearchEngine.SearchResultListener {

    /**
     * State set of the application workflow.
     */
    public enum WorkflowState {
        NOT_STARTED,
        DETECTING,
        DETECTED,
        CONFIRMING,
        CONFIRMED,
        SEARCHED
    }

    public enum CameraMode {
        LIVE,
        CAMERA
    }

    public final MutableLiveData<CameraMode> currentMode = new MutableLiveData<>();
    public final MutableLiveData<WorkflowState> workflowState = new MutableLiveData<>();
    public final MutableLiveData<DetectedObject> objectToSearch = new MutableLiveData<>();
    public final MutableLiveData<SearchedObject> searchedObject = new MutableLiveData<>();
    public final MutableLiveData<String> searchError = new MutableLiveData<>();


    private final Set<Integer> objectIdsToSearch = new HashSet<>();

    private boolean isCameraLive = false;
    @Nullable
    private DetectedObject confirmedObject;

    public WorkflowModel(Application application) {
        super(application);
    }

    @MainThread
    public void setWorkflowState(WorkflowState workflowState) {
        if (!workflowState.equals(WorkflowState.CONFIRMED)
                && !workflowState.equals(WorkflowState.SEARCHED)) {
            confirmedObject = null;
        }
        this.workflowState.setValue(workflowState);
    }

    @MainThread
    public void setCurrentMode(CameraMode mode) {
        if (!mode.equals(currentMode.getValue())) {
            currentMode.setValue(mode);
            workflowState.setValue(WorkflowState.NOT_STARTED);
            confirmedObject = null;
        }
    }

    @MainThread
    public void confirmingObject(DetectedObject object, float progress) {
        boolean isConfirmed = (Float.compare(progress, 1f) == 0);
        if (isConfirmed) {
            confirmedObject = object;
            triggerSearch(object);
            setWorkflowState(WorkflowState.CONFIRMED);
        } else {
            setWorkflowState(WorkflowState.CONFIRMING);
        }
    }

    private void triggerSearch(DetectedObject object) {
        Integer objectId = checkNotNull(object.getObjectId());
        if (objectIdsToSearch.contains(objectId)) {
            // Already in searching.
            return;
        }

        objectIdsToSearch.add(objectId);
        objectToSearch.setValue(object);
    }

    public void markCameraLive() {
        isCameraLive = true;
        objectIdsToSearch.clear();
    }

    public void markCameraFrozen() {
        isCameraLive = false;
    }

    public boolean isCameraLive() {
        return isCameraLive;
    }

    public boolean isLiveMode() {
        return CameraMode.LIVE.equals(currentMode.getValue());
    }

    public boolean isCameraMode() {
        return CameraMode.CAMERA.equals(currentMode.getValue());
    }


    @Override
    public void onSearchCompleted(DetectedObject object, List<Product> productList, String errorMessage) {
        if (!object.equals(confirmedObject)) {
            // Drops the search result from the object that has lost focus.
            return;
        }

        objectIdsToSearch.remove(object.getObjectId());

        if (errorMessage != null) {
            searchError.setValue(errorMessage);
        } else {
            searchedObject.setValue(
                    new SearchedObject(getContext().getResources(), confirmedObject, productList));
        }

        setWorkflowState(WorkflowState.SEARCHED);
    }

    private Context getContext() {
        return getApplication().getApplicationContext();
    }
}
