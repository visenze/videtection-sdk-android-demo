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

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;

import com.visenze.vimobile.demo.R;
import com.visenze.videtection.camera.GraphicOverlay;
import com.visenze.videtection.camera.GraphicOverlay.Graphic;

import androidx.core.content.ContextCompat;

public class ObjectCircleRippleGraphic extends Graphic {

    private final ObjectCircleAnimator animator;
    private final Paint circlePaint;
    private final int circleRadius;

    private final Paint textPaint;
    private final int textSize;
    private final int textAlpha;

    private final Paint ripplePaint;
    private final int rippleSizeOffset;
    private final int rippleStrokeWidth;
    private final int rippleAlpha;

    public ObjectCircleRippleGraphic(GraphicOverlay overlay, ObjectCircleAnimator animator) {
        super(overlay);

        this.animator = animator;

        Resources resources = overlay.getResources();
        circlePaint = new Paint();
        circlePaint.setStyle(Style.STROKE);
        circlePaint.setColor(ContextCompat.getColor(context, R.color.white));
        circlePaint.setStrokeWidth(resources.getDimensionPixelOffset(R.dimen.object_reticle_outer_ring_stroke_width));
        circlePaint.setStrokeCap(Cap.ROUND);

        circleRadius = resources.getDimensionPixelOffset(R.dimen.object_circle_ripple_radius);

        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Style.FILL);
        textPaint.setColor(ContextCompat.getColor(context, R.color.white));
        textSize = resources.getDimensionPixelSize(R.dimen.object_circle_ripple_text_size);
        textPaint.setTextSize(textSize);

        textAlpha = textPaint.getAlpha();

        ripplePaint = new Paint();
        ripplePaint.setStyle(Style.STROKE);
        ripplePaint.setColor(ContextCompat.getColor(context, R.color.reticle_ripple));


        rippleSizeOffset = resources.getDimensionPixelOffset(R.dimen.object_circle_ripple_size_offset);
        rippleStrokeWidth = resources.getDimensionPixelOffset(R.dimen.object_reticle_ripple_stroke_width);
        rippleAlpha = ripplePaint.getAlpha();
    }

    @Override
    public void draw(Canvas canvas) {

        float cx = canvas.getWidth() / 2f;
        float cy = canvas.getHeight() / 2f;

        canvas.drawCircle(cx, cy, circleRadius, circlePaint);
        textPaint.setAlpha((int) (textAlpha * animator.getTextAlphaScale()));
        float yPos = cy - ((textPaint.descent() + textPaint.ascent()) / 2.0f);
        canvas.drawText("finding", cx, yPos, textPaint);

        // Draws the ripple to simulate the breathing animation effect.
        ripplePaint.setAlpha((int) (rippleAlpha * animator.getRippleAlphaScale()));
        ripplePaint.setStrokeWidth(rippleStrokeWidth * animator.getRippleStrokeWidthScale());
        float radius = circleRadius + rippleSizeOffset * animator.getRippleSizeScale();
        canvas.drawCircle(cx, cy, radius, ripplePaint);
    }
}
