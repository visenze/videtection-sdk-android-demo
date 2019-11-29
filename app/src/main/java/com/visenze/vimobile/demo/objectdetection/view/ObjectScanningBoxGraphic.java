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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;

import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.objectdetection.controller.ObjectConfirmationController;
import com.visenze.vimobile.demo.objectdetection.model.DetectedObject;
import com.visenze.videtection.camera.GraphicOverlay;
import com.visenze.videtection.camera.GraphicOverlay.Graphic;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

public class ObjectScanningBoxGraphic extends Graphic {

    private final DetectedObject object;
    private final ObjectConfirmationController confirmationController;

    private final Paint scrimPaint;
    private final Paint eraserPaint;
    private final Paint boxPaint;
    private final Paint ScanBoxFillPaint;
    @ColorInt
    private final int boxGradientStartColor;
    @ColorInt
    private final int boxGradientEndColor;
    private final int boxCornerRadius;

    public ObjectScanningBoxGraphic(GraphicOverlay overlay, DetectedObject object,
                                    ObjectConfirmationController confirmationController) {
        super(overlay);
        this.object = object;
        this.confirmationController = confirmationController;

        scrimPaint = new Paint();
        if (confirmationController.isConfirmed()) {
            scrimPaint.setShader(
                    new LinearGradient(
                            0,
                            0,
                            overlay.getWidth(),
                            overlay.getHeight(),
                            ContextCompat.getColor(context, R.color.object_confirmed_bg_gradient_start),
                            ContextCompat.getColor(context, R.color.object_confirmed_bg_gradient_end),
                            Shader.TileMode.CLAMP));

        } else {
            scrimPaint.setShader(
                    new LinearGradient(
                            0,
                            0,
                            overlay.getWidth(),
                            overlay.getHeight(),
                            ContextCompat.getColor(context, R.color.object_detected_bg_gradient_start),
                            ContextCompat.getColor(context, R.color.object_detected_bg_gradient_end),
                            Shader.TileMode.CLAMP));
        }

        eraserPaint = new Paint();
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(
                context
                        .getResources()
                        .getDimensionPixelOffset(
                                confirmationController.isConfirmed()
                                        ? R.dimen.bounding_box_confirmed_stroke_width
                                        : R.dimen.bounding_box_stroke_width));
        boxPaint.setColor(Color.WHITE);


        boxGradientStartColor = ContextCompat.getColor(context, R.color.bounding_box_gradient_start);
        boxGradientEndColor = ContextCompat.getColor(context, R.color.bounding_box_gradient_end);
        boxCornerRadius =
                context.getResources().getDimensionPixelOffset(R.dimen.bounding_box_corner_radius);

        ScanBoxFillPaint = new Paint();
        ScanBoxFillPaint.setStyle(Paint.Style.FILL);
        ScanBoxFillPaint.setColor(boxGradientEndColor);
    }

    @Override
    public void draw(Canvas canvas) {
        RectF rect = overlay.translateRect(object.getBoundingBox());

        // Draws the dark background scrim and leaves the object area clear.
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), scrimPaint);
        canvas.drawRoundRect(rect, boxCornerRadius, boxCornerRadius, eraserPaint);

        // Draws the bounding box with a gradient border color at vertical.

        boxPaint.setShader(
                confirmationController.isConfirmed()
                        ? null
                        : new LinearGradient(
                        rect.left,
                        rect.top,
                        rect.left,
                        rect.bottom,
                        boxGradientStartColor,
                        boxGradientEndColor,
                        Shader.TileMode.CLAMP));
        canvas.drawRoundRect(rect, boxCornerRadius, boxCornerRadius, boxPaint);


        float progress = confirmationController.getProgress();
        if (progress > 0.1f) {

            float scanBoxRation = 0.3f;
            float scanBoxHeight = (rect.bottom - rect.top) * scanBoxRation;

            if (progress > 0.95f) { // make the progress to 1, to make the scanning go to the end when nearly complete.
                progress = 1f;
            }
            float rectStartY = rect.top + (rect.bottom - rect.top) * (1 - scanBoxRation) * progress;
            float rectEndY = rectStartY + scanBoxHeight;
            RectF scanBoxRect = new RectF(rect.left, rectStartY, rect.right, rectEndY);


            ScanBoxFillPaint.setShader(new LinearGradient(scanBoxRect.left, scanBoxRect.top, scanBoxRect.left, scanBoxRect.bottom, boxGradientStartColor,
                    boxGradientEndColor,
                    Shader.TileMode.CLAMP));
            canvas.drawRoundRect(scanBoxRect, boxCornerRadius, boxCornerRadius, ScanBoxFillPaint);
        }
    }
}
