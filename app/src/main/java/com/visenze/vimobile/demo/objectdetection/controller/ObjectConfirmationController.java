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

import android.os.CountDownTimer;

import com.visenze.vimobile.demo.utils.PreferenceUtils;
import com.visenze.videtection.camera.GraphicOverlay;

import androidx.annotation.Nullable;

/**
 * Controls the progress of object confirmation before performing additional operation on the
 * detected object.
 */
public class ObjectConfirmationController {

    private final CountDownTimer countDownTimer;

    @Nullable
    private Integer objectId = null;
    private float progress = 0;

    /**
     * @param graphicOverlay Used to refresh camera overlay when the confirmation progress updates.
     */
    public ObjectConfirmationController(GraphicOverlay graphicOverlay) {
        long confirmationTimeMs = PreferenceUtils.getConfirmationTimeMs(graphicOverlay.getContext());
        countDownTimer =
                new CountDownTimer(confirmationTimeMs, /* countDownInterval= */ 20) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        progress = (float) (confirmationTimeMs - millisUntilFinished) / confirmationTimeMs;
                        graphicOverlay.invalidate();
                    }

                    @Override
                    public void onFinish() {
                        progress = 1;
                    }
                };
    }

    public void confirming(Integer objectId) {
        if (objectId.equals(this.objectId)) {
            // Do nothing if it's already in confirming.
            return;
        }

        reset();
        this.objectId = objectId;
        countDownTimer.start();
    }

    public boolean isConfirmed() {
        return Float.compare(progress, 1) == 0;
    }

    public void reset() {
        countDownTimer.cancel();
        objectId = null;
        progress = 0;
    }

    /**
     * Returns the confirmation progress described as a float value in the range of [0, 1].
     */
    public float getProgress() {
        return progress;
    }
}
