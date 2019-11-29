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

package com.visenze.vimobile.demo.objectdetection.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;

import com.visenze.videtection.camera.GraphicOverlay;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class ObjectCircleAnimator {
    private static final long DURATION_TEXT_FADE_IN_MS = 500;
    private static final long DURATION_TEXT_FADE_OUT_MS = 783;
    private static final long START_DELAY_TEXT_FADE_OUT_MS = 550;
    private static final long DURATION_TOTAL_DURATION = 1500;
    private final AnimatorSet animatorSet;

    private float textAlphaScale = 0f;

    private static final long DURATION_RIPPLE_FADE_IN_MS = 333;
    private static final long DURATION_RIPPLE_FADE_OUT_MS = 500;
    private static final long DURATION_RIPPLE_EXPAND_MS = 833;
    private static final long DURATION_RIPPLE_STROKE_WIDTH_SHRINK_MS = 833;
    private static final long START_DELAY_RIPPLE_FADE_OUT_MS = 667;
    private static final long START_DELAY_RIPPLE_EXPAND_MS = 333;
    private static final long START_DELAY_RIPPLE_STROKE_WIDTH_SHRINK_MS = 333;

    private float rippleAlphaScale = 0f;
    private float rippleSizeScale = 0f;
    private float rippleStrokeWidthScale = 1f;

    public ObjectCircleAnimator(final GraphicOverlay graphicOverlay) {
        ValueAnimator rippleFadeInAnimator =
                ValueAnimator.ofFloat(0f, 1f).setDuration(DURATION_RIPPLE_FADE_IN_MS);
        rippleFadeInAnimator.addUpdateListener(
                animation -> {
                    rippleAlphaScale = (float) animation.getAnimatedValue();
                    graphicOverlay.postInvalidate();
                });

        ValueAnimator rippleFadeOutAnimator =
                ValueAnimator.ofFloat(1f, 0f).setDuration(DURATION_RIPPLE_FADE_OUT_MS);
        rippleFadeOutAnimator.setStartDelay(START_DELAY_RIPPLE_FADE_OUT_MS);
        rippleFadeOutAnimator.addUpdateListener(
                animation -> {
                    rippleAlphaScale = (float) animation.getAnimatedValue();
                    graphicOverlay.postInvalidate();
                });

        ValueAnimator rippleExpandAnimator =
                ValueAnimator.ofFloat(0f, 1f).setDuration(DURATION_RIPPLE_EXPAND_MS);
        rippleExpandAnimator.setStartDelay(START_DELAY_RIPPLE_EXPAND_MS);
        rippleExpandAnimator.setInterpolator(new FastOutSlowInInterpolator());
        rippleExpandAnimator.addUpdateListener(
                animation -> {
                    rippleSizeScale = (float) animation.getAnimatedValue();
                    graphicOverlay.postInvalidate();
                });

        ValueAnimator rippleStrokeWidthShrinkAnimator =
                ValueAnimator.ofFloat(1f, 0.5f).setDuration(DURATION_RIPPLE_STROKE_WIDTH_SHRINK_MS);
        rippleStrokeWidthShrinkAnimator.setStartDelay(START_DELAY_RIPPLE_STROKE_WIDTH_SHRINK_MS);
        rippleStrokeWidthShrinkAnimator.setInterpolator(new FastOutSlowInInterpolator());
        rippleStrokeWidthShrinkAnimator.addUpdateListener(
                animation -> {
                    rippleStrokeWidthScale = (float) animation.getAnimatedValue();
                    graphicOverlay.postInvalidate();
                });


        ValueAnimator textFadeInAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(DURATION_TEXT_FADE_IN_MS);
        textFadeInAnimator.addUpdateListener(
                animation -> {
                    textAlphaScale = (float) animation.getAnimatedValue();
                    graphicOverlay.postInvalidate();
                }
        );

        ValueAnimator textFadeOutAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(DURATION_TEXT_FADE_OUT_MS);
        textFadeOutAnimator.setStartDelay(START_DELAY_TEXT_FADE_OUT_MS);
        textFadeOutAnimator.addUpdateListener(
                animation -> {
                    textAlphaScale = (float) animation.getAnimatedValue();
                    graphicOverlay.postInvalidate();
                }
        );

        ValueAnimator fakeAnimatorForRestartDelay =
                ValueAnimator.ofInt(0, 0).setDuration(DURATION_TOTAL_DURATION);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                textFadeInAnimator,
                textFadeOutAnimator,
                rippleFadeInAnimator,
                rippleFadeOutAnimator,
                rippleExpandAnimator,
                rippleStrokeWidthShrinkAnimator,
                fakeAnimatorForRestartDelay
        );
    }

    public void start() {
        if (!animatorSet.isRunning()) {
            animatorSet.start();
        }
    }

    public void cancel() {
        animatorSet.cancel();
        textAlphaScale = 0f;
        rippleAlphaScale = 0f;
        rippleSizeScale = 0f;
        rippleStrokeWidthScale = 1f;
    }

    public float getTextAlphaScale() {
        return textAlphaScale;
    }

    public float getRippleAlphaScale() {
        return rippleAlphaScale;
    }

    /**
     * Returns the scale value of ripple size ranges in [0, 1].
     */
    public float getRippleSizeScale() {
        return rippleSizeScale;
    }

    /**
     * Returns the scale value of ripple stroke width ranges in [0, 1].
     */
    public float getRippleStrokeWidthScale() {
        return rippleStrokeWidthScale;
    }
}
