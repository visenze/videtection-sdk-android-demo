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

package com.visenze.vimobile.demo.objectdetection.model;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.visenze.videtection.camera.ViImage;
import com.visenze.videtection.model.ViBoundingBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.annotation.Nullable;

public class ViDetectedObject implements DetectedObject {

    private static final String TAG = "DetectedObject";

    private final ViBoundingBox boxInfo;
    private final int objectIndex;
    private final ViImage image;
    private boolean rotateBoundingBoxRect;

    @Nullable
    private Bitmap squaredThumbnailBitmap = null;
    @Nullable
    private Bitmap bitmap = null;
    @Nullable
    private byte[] jpegBytes = null;

    public ViDetectedObject(ViBoundingBox boxInfo, int objectIndex, ViImage image, boolean rotateBoundingBoxRect) {
        this.boxInfo = boxInfo;
        this.objectIndex = objectIndex;
        this.image = image;
        this.rotateBoundingBoxRect = rotateBoundingBoxRect;
    }

    @Nullable
    public Integer getObjectId() {
        //we do not have id yet, use objectIndex instead.
        // object tracking id.
        return boxInfo.id;
    }

    public int getObjectIndex() {
        return objectIndex;
    }

    public Rect getBoundingBox() {
        if (rotateBoundingBoxRect) {
            return new Rect(boxInfo.y1, boxInfo.x1, boxInfo.y2, boxInfo.x2);
        }
        return new Rect(boxInfo.x1, boxInfo.y1, boxInfo.x2, boxInfo.y2);
    }

    public synchronized Bitmap getBitmap() {
        if (bitmap == null) {

            Bitmap bmp = image.getBitmap();
            bitmap = Bitmap.createBitmap(
                            bmp,
                            boxInfo.x1,
                            boxInfo.y1,
                            boxInfo.x2 - boxInfo.x1,
                            boxInfo.y2 - boxInfo.y1);
        }

        return bitmap;
    }

    public synchronized Bitmap getSquaredThumbnailBitmap() {
        if (squaredThumbnailBitmap == null) {
            Bitmap bmp = image.getBitmap();
            int origBitmapWidth = bmp.getWidth();
            int origBitmapHeight = bmp.getHeight();
            int centerX = (boxInfo.x2 - boxInfo.x1) / 2 + boxInfo.x1;
            int centerY = (boxInfo.y2 - boxInfo.y1) / 2 + boxInfo.y1;

            int boxWidth = boxInfo.x2 - boxInfo.x1;
            int boxHeight = boxInfo.y2 - boxInfo.y1;
            int width = boxHeight > boxWidth ? boxHeight : boxWidth;
            int height = boxHeight > boxWidth ? boxHeight : boxWidth;
            int left = centerX - (int) (width / 2.0f);
            int top = centerY - (int) (height / 2.0f);
            if (left < 0) {
                left = 0;
            }
            if (top < 0) {
                top = 0;
            }

            if (left + width > origBitmapWidth) {
                int offset = left + width - origBitmapWidth;
                if (left - offset > 0) {
                    left = left - offset;
                } else {
                    left = 0;
                    if (width > origBitmapWidth) {
                        width = origBitmapWidth;
                    }
                }
            }

            if (top + height > origBitmapHeight) {
                int offset = top + height - origBitmapHeight;
                if (top - offset > 0) {
                    top = top - offset;
                } else {
                    top = 0;
                    if (height > origBitmapHeight) {
                        height = origBitmapHeight;
                    }
                }
            }

            squaredThumbnailBitmap = Bitmap.createBitmap(bmp,
                    left,
                    top,
                    width,
                    height);

        }
        return squaredThumbnailBitmap;
    }


    @Nullable
    public synchronized byte[] getImageData() {
        if (jpegBytes == null) {
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                getBitmap().compress(Bitmap.CompressFormat.JPEG, /* quality= */ 100, stream);
                jpegBytes = stream.toByteArray();
            } catch (IOException e) {
                Log.e(TAG, "Error getting object image data!");
            }
        }

        return jpegBytes;
    }
}
