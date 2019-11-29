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

package com.visenze.vimobile.demo.objectdetection.controller;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;

import com.visenze.vimobile.demo.objectdetection.model.DetectedObject;
import com.visenze.videtection.camera.GraphicOverlay;

import java.util.ArrayList;
import java.util.List;

public class TrackingController {
    private final static int BUFFLEN = 10;
    private final static int STABLETIME = 500;

    private final int sdLimit;
    private final int radius;
    private final List<Float[]> relativeDistance;
    private final List<Long> addTime;


    public TrackingController(int radius, int sdLimit) {
        this.radius = radius;
        this.sdLimit = sdLimit;
        relativeDistance = new ArrayList<>();
        addTime = new ArrayList<>();
    }

    public boolean objectBoxOverlapConfirmReticle(GraphicOverlay graphicOverlay, DetectedObject object) {
        RectF boxRect = graphicOverlay.translateRect(object.getBoundingBox());
        float reticleCenterX = graphicOverlay.getWidth() / 2f;
        float reticleCenterY = graphicOverlay.getHeight() / 2f;

        RectF reticleRect = new RectF(
                reticleCenterX - radius,
                reticleCenterY - radius,
                reticleCenterX + radius,
                reticleCenterY + radius);

        return reticleRect.intersect(boxRect);
    }

    public void addObject(GraphicOverlay graphicOverlay, DetectedObject object) {
        // Considers an object as selected when the camera reticle touches the object dot.
        RectF box = graphicOverlay.translateRect(object.getBoundingBox());
        PointF objectCenter = new PointF((box.left + box.right) / 2f, (box.top + box.bottom) / 2f);
        PointF center = new PointF(graphicOverlay.getWidth() / 2f, graphicOverlay.getHeight() / 2f);

        addPoint(objectCenter, center);
    }

    public boolean isStable() {
        if (relativeDistance.size() < BUFFLEN) {
            return false;
        }

        List<Float> xDist = new ArrayList<>();
        List<Float> yDist = new ArrayList<>();
        int beginIndex = getTimeBoundIndex();
        // Log.d(TAG, "beginIndex: " + beginIndex);
        for (int i = beginIndex; i < BUFFLEN; i++) {
            xDist.add(relativeDistance.get(i)[0]);
            yDist.add(relativeDistance.get(i)[1]);
        }

        float sdx = standardDeviation(xDist);
        float sdy = standardDeviation(yDist);
        return sdx < sdLimit && sdy < sdLimit;
    }

    private void addPoint(PointF p, PointF c) {
        if (relativeDistance.size() >= BUFFLEN) {
            addTime.remove(0);
            relativeDistance.remove(0);
        }
        addTime.add(SystemClock.elapsedRealtime());
        relativeDistance.add(computeDistance(p, c));
    }

    private Float[] computeDistance(PointF p, PointF c) {
        Float[] data = new Float[2];
        data[0] = p.x - c.x;
        data[1] = p.y - c.y;
        return data;
    }

    private int getTimeBoundIndex() {
        int size = addTime.size();
        long endTime = addTime.get(size - 1);
        for (int i = size - 1; i >= 0; i--) {
            long t = addTime.get(i);

            if (endTime - t > STABLETIME) {
                return i;
            }
        }

        return 0;
    }

    private float standardDeviation(List<Float> distance) {
        float mean = 0;
        for (float d : distance) {
            mean += d;
        }
        mean = mean / distance.size();

        float sd = 0;

        for (float d : distance) {
            sd += (d - mean) * (d - mean);
        }
        sd = (float) Math.sqrt(sd / (distance.size() - 1));
        return sd;
    }
}
