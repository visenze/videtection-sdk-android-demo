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


package com.visenze.vimobile.demo.productsearch;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.objectdetection.model.DetectedObject;
import com.visenze.vimobile.demo.productsearch.model.Product;
import com.visenze.vimobile.demo.productsearch.model.ProductQuerySchema;
import com.visenze.visearch.android.BaseSearchParams;
import com.visenze.visearch.android.UploadSearchParams;
import com.visenze.visearch.android.ViSearch;
import com.visenze.visearch.android.model.ImageResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fake search engine to help simulate the complete work flow.
 */
public class SearchEngine {

    private static final String TAG = "SearchEngine";

    public interface SearchResultListener {
        void onSearchCompleted(DetectedObject object, List<Product> productList, String errorMessage);
    }

    private ViSearch mVisearch;

    public SearchEngine(Context context) {

        String appKey = context.getResources().getString(R.string.app_key);
        String endPoint = context.getResources().getString(R.string.endpoint);

        try {
            mVisearch = new ViSearch.Builder(appKey)
                    .setApiEndPoint(new URL(endPoint))
                    .build(context);

            ProductQuerySchema.loadSchemaConfig();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void search(final DetectedObject object, SearchResultListener listener) {
        mVisearch.setListener(new ViSearch.ResultListener() {
            @Override
            public void onSearchResult(com.visenze.visearch.android.ResultList resultList) {
                handleSearchResult(object, resultList, listener);
            }

            @Override
            public void onSearchError(String errorMessage) {
                handleSearchError(object, errorMessage, listener);
            }

            @Override
            public void onSearchCanceled() {
                Log.d(TAG, "SearchCanceled");
            }
        });

        CreateSearchImageTask task = new CreateSearchImageTask(image -> {
            UploadSearchParams uploadSearchParams = new UploadSearchParams(image);
            BaseSearchParams baseSearchParams = new BaseSearchParams();
            Map<String, String> internal = new HashMap<>();
            internal.put("vtt_source", "visenze_admin");
            baseSearchParams.setCustom(internal);
            List<String> fl = ProductQuerySchema.getFieldList();
            baseSearchParams.setFl(fl);
            uploadSearchParams.setBaseSearchParams(baseSearchParams);
            mVisearch.uploadSearch(uploadSearchParams);
        });

        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, object);

    }

    private void handleSearchResult(DetectedObject object, com.visenze.visearch.android.ResultList resultList, SearchResultListener listener) {
        if (resultList.getImageList().size() == 0) {
            handleSearchError(object, "No product found", listener);
        } else {
            List<Product> productList = new ArrayList<>();
            for (ImageResult imageResult : resultList.getImageList()) {
                Map<String, String> metaData = imageResult.getMetaData();
                Product prod = ProductQuerySchema.fromMetaData(metaData);
                productList.add(prod);
            }

            listener.onSearchCompleted(object, productList, null);
        }
    }

    private void handleSearchError(DetectedObject object, String errorMessage, SearchResultListener listener) {
        Log.e(TAG, errorMessage);
        listener.onSearchCompleted(object, new ArrayList<>(), errorMessage);
    }

    public void shutdown() {
        if (mVisearch != null) {
            mVisearch.cancelSearch();
        }
    }

    public static class CreateSearchImageTask extends AsyncTask<DetectedObject, Void, com.visenze.visearch.android.model.Image> {

        private final OnTaskSuccess mListener;

        CreateSearchImageTask(OnTaskSuccess listener) {
            mListener = listener;
        }

        @Override
        protected com.visenze.visearch.android.model.Image doInBackground(DetectedObject... detectedObjects) {
            DetectedObject obj = detectedObjects[0];
            byte[] objectImageData = obj.getImageData();
            if (objectImageData != null) {
                return new com.visenze.visearch.android.model.Image(objectImageData);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(com.visenze.visearch.android.model.Image image) {
            if (mListener != null) {
                mListener.onSuccess(image);
            }
        }

        public interface OnTaskSuccess {
            void onSuccess(com.visenze.visearch.android.model.Image image);
        }
    }
}
