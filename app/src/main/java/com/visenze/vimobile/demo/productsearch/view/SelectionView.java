package com.visenze.vimobile.demo.productsearch.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.productsearch.view.handler.OnBoxChangedListener;
import com.visenze.vimobile.demo.productsearch.view.handler.OnScanFinishedListener;
import com.visenze.vimobile.demo.productsearch.view.model.ScalableBox;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;

/**
 * box that can be scaled and moved
 * Created by yulu on 11/12/14.
 */
public class SelectionView extends View implements View.OnTouchListener {
    private static final String SELECTION_VIEW = "BOX SELECTION VIEW";

    private OnBoxChangedListener onBoxChangedListener;
    private OnScanFinishedListener onScanFinishedListener;
    private EditableImage editableImage;

    private int bitmapWidth;
    private int bitmapHeight;
    private int originX;
    private int originY;

    private List<ScalableBox> displayBoxes;
    private int prevX;
    private int prevY;

    private int prevBoxX1;
    private int prevBoxX2;
    private int prevBoxY1;
    private int prevBoxY2;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float lineWidth;
    private float cornerWidth;
    private float cornerLength;
    private float offset;
    private float offset_2;
    private int lineColor;
    private int cornerColor;
    private int dotColor;
    private int shadowColor;
    private int scanningBoxStartColor;
    private int scanningBoxEndColor;
    private int dotRadius;
    private int boxBuffer;

    // animating parameters
    private boolean animatingExpanding = false;
    private int[] startingCenter = new int[4];
    private int[] targetingBoxes = new int[4];


    private boolean animatingScanning = false;
    private int[] scanningBoxPosition = new int[4];
    private int scanningBoxStep;

    private boolean enableCropping = true;

    public SelectionView(Context context,
                         float lineWidth, float cornerWidth, float cornerLength,
                         int lineColor, int cornerColor, int dotColor, int shadowColor,
                         EditableImage editableImage) {
        super(context);

        setOnTouchListener(this);

        this.editableImage = editableImage;
        this.lineWidth = lineWidth;
        this.cornerWidth = cornerWidth;
        this.cornerLength = cornerLength;
        this.lineColor = lineColor;
        this.cornerColor = cornerColor;
        this.dotColor = dotColor;
        this.shadowColor = shadowColor;
        this.dotRadius = context.getResources().getDimensionPixelOffset(R.dimen.crop_dot_radius);
        this.boxBuffer = context.getResources().getDimensionPixelOffset(R.dimen.crop_box_buffer);
        this.displayBoxes = new ArrayList<>();

        offset = lineWidth / 4;
        offset_2 = lineWidth;

        scanningBoxStartColor = ContextCompat.getColor(context, R.color.trans);
        scanningBoxEndColor = ContextCompat.getColor(context, R.color.scan_box_end_color);
    }

    public void resetBoxSize(int bitmapWidth, int bitmapHeight) {
        this.bitmapWidth = bitmapWidth;
        this.bitmapHeight = bitmapHeight;

        int size = (bitmapWidth < bitmapHeight) ? bitmapWidth : bitmapHeight;

        for (ScalableBox displayBox : displayBoxes) {
            displayBox.setX1((getWidth() - size) / 2);
            displayBox.setX2((getWidth() + size) / 2);
            displayBox.setY1((getHeight() - size) / 2);
            displayBox.setY2((getHeight() + size) / 2);
        }
        invalidate();
    }

    public void setBoxSize(EditableImage editableImage, List<ScalableBox> originalBoxes, int widthX, int heightY) {
        this.bitmapWidth = editableImage.getFitSize()[0];
        this.bitmapHeight = editableImage.getFitSize()[1];
        int originX = (widthX - bitmapWidth) / 2;
        int originY = (heightY - bitmapHeight) / 2;
        this.originX = originX;
        this.originY = originY;

        setupScanning();
        setDisplayBoxes(originalBoxes);

        invalidate();
    }

    public void setOnBoxChangedListener(OnBoxChangedListener listener) {
        this.onBoxChangedListener = listener;
    }

    public void setOnScanFinishedListener(OnScanFinishedListener listener) {
        this.onScanFinishedListener = listener;
    }

    @Override
    protected void onSizeChanged(int x, int y, int oldx, int oldy) {
        super.onSizeChanged(x, y, oldx, oldy);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (animatingExpanding) {
            // Log.d(TAG, "expandBox");
            expandBox(canvas);
        } else if(animatingScanning) {
            scanning(canvas);
        } else {
            if(editableImage.isConfirming()) {
                drawShadow(canvas);
                drawCorner(canvas);
                drawDot(canvas);
            } else {
                drawLines(canvas);
                drawDot(canvas);
            }
        }
    }

    public void enableCropping(boolean enabled) {
        this.enableCropping = enabled;
    }


    private void drawShadow(Canvas canvas) {
        mPaint.setStrokeWidth(0.0f);
        mPaint.setColor(shadowColor);

        if (displayBoxes != null && displayBoxes.size() > 0) {
            ScalableBox displayBox = displayBoxes.get(editableImage.getActiveBoxIdx());
            canvas.drawRect(originX, originY, originX + bitmapWidth, displayBox.getY1(), mPaint);
            canvas.drawRect(originX, displayBox.getY1(), displayBox.getX1(), displayBox.getY2(), mPaint);
            canvas.drawRect(displayBox.getX2(), displayBox.getY1(), originX + bitmapWidth, displayBox.getY2(), mPaint);
            canvas.drawRect(originX, displayBox.getY2(), originX + bitmapWidth, originY + bitmapHeight, mPaint);
        }
    }

    private void drawLines(Canvas canvas) {
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(lineColor);

        if (displayBoxes != null && displayBoxes.size() > 0) {
            ScalableBox displayBox = displayBoxes.get(editableImage.getActiveBoxIdx());
            canvas.drawLine(displayBox.getX1(), displayBox.getY1(), displayBox.getX2(), displayBox.getY1(), mPaint);
            canvas.drawLine(displayBox.getX2(), displayBox.getY1(), displayBox.getX2(), displayBox.getY2(), mPaint);
            canvas.drawLine(displayBox.getX2(), displayBox.getY2(), displayBox.getX1(), displayBox.getY2(), mPaint);
            canvas.drawLine(displayBox.getX1(), displayBox.getY2(), displayBox.getX1(), displayBox.getY1(), mPaint);
        }
    }

    private void drawCorner(Canvas canvas) {
        mPaint.setStrokeWidth(cornerWidth);
        mPaint.setColor(cornerColor);

        if (displayBoxes != null && displayBoxes.size() > 0) {
            ScalableBox displayBox = displayBoxes.get(editableImage.getActiveBoxIdx());
            int x1 = displayBox.getX1();
            int x2 = displayBox.getX2();
            int y1 = displayBox.getY1();
            int y2 = displayBox.getY2();

            int minSize = (int) cornerLength;

            canvas.drawLine(x1 - offset_2, y1 - offset, x1 - offset + minSize, y1 - offset, mPaint);
            canvas.drawLine(x1 - offset, y1 - offset_2, x1 - offset, y1 - offset + minSize, mPaint);

            canvas.drawLine(x2 + offset_2, y1 - offset, x2 + offset - minSize, y1 - offset, mPaint);
            canvas.drawLine(x2 + offset, y1 - offset_2, x2 + offset, y1 - offset + minSize, mPaint);

            canvas.drawLine(x1 - offset_2, y2 + offset, x1 - offset + minSize, y2 + offset, mPaint);
            canvas.drawLine(x1 - offset, y2 + offset_2, x1 - offset, y2 + offset - minSize, mPaint);

            canvas.drawLine(x2 + offset_2, y2 + offset, x2 + offset - minSize, y2 + offset, mPaint);
            canvas.drawLine(x2 + offset, y2 + offset_2, x2 + offset, y2 + offset - minSize, mPaint);
        }
    }

    private void scanning(Canvas canvas) {
        scanningBoxPosition[2] = scanningBoxPosition[2] + scanningBoxStep;
        scanningBoxPosition[3] = scanningBoxPosition[3] + scanningBoxStep;

        if(scanningBoxPosition[3] <= originY + bitmapHeight) {
            Paint ScanBoxFillPaint = new Paint();
            ScanBoxFillPaint.setStyle(Paint.Style.FILL);
            ScanBoxFillPaint.setColor(Color.WHITE);

            RectF scanBoxRect = new RectF(scanningBoxPosition[0], scanningBoxPosition[2], scanningBoxPosition[1], scanningBoxPosition[3]);

            ScanBoxFillPaint.setShader(new LinearGradient(scanBoxRect.left, scanBoxRect.top,scanBoxRect.left,scanBoxRect.bottom, scanningBoxStartColor,
                    scanningBoxEndColor,
                    Shader.TileMode.CLAMP));
            canvas.drawRect(scanBoxRect, ScanBoxFillPaint);
        } else {
            animatingScanning = false;
            onScanFinishedListener.onScanFinished();
        }
        invalidate();
    }

    private void expandBox(Canvas canvas) {
        int step = 10;
        float aspectRation = (targetingBoxes[3] - targetingBoxes[2]) * 1.0f / (targetingBoxes[1] - targetingBoxes[0]);
        startingCenter[0] = startingCenter[0] - step;
        startingCenter[1]  = startingCenter[1] + step;
        startingCenter[2] = (int)(startingCenter[2] - step * aspectRation);
        startingCenter[3] = (int)(startingCenter[3] + step * aspectRation);


        if (startingCenter[0] <= targetingBoxes[0] || startingCenter[1] >= targetingBoxes[1]
                || startingCenter[2] <= targetingBoxes[2] || startingCenter[3] >= targetingBoxes[3]) {
            startingCenter[0] = targetingBoxes[0];
            startingCenter[1] = targetingBoxes[1];
            startingCenter[2] = targetingBoxes[2];
            startingCenter[3] = targetingBoxes[3];

            animatingExpanding = false;
        }

        // add shade
        mPaint.setStrokeWidth(0.0f);
        mPaint.setColor(shadowColor);
        canvas.drawRect(originX, originY, originX + bitmapWidth, originY + bitmapHeight, mPaint);

        // draw box
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(lineColor);
        int x1 = startingCenter[0];
        int x2 = startingCenter[1];
        int y1 = startingCenter[2];
        int y2 = startingCenter[3];
        int minSize = (int) cornerLength;
        canvas.drawLine(x1, y1, x2, y1, mPaint);
        canvas.drawLine(x2, y1, x2, y2, mPaint);
        canvas.drawLine(x2, y2, x1, y2, mPaint);
        canvas.drawLine(x1, y2, x1, y1, mPaint);

        // draw corner
        mPaint.setColor(cornerColor);
        canvas.drawLine(x1 - offset_2, y1 - offset, x1 - offset + minSize, y1 - offset, mPaint);
        canvas.drawLine(x1 - offset, y1 - offset_2, x1 - offset, y1 - offset + minSize, mPaint);
        canvas.drawLine(x2 + offset_2, y1 - offset, x2 + offset - minSize, y1 - offset, mPaint);
        canvas.drawLine(x2 + offset, y1 - offset_2, x2 + offset, y1 - offset + minSize, mPaint);
        canvas.drawLine(x1 - offset_2, y2 + offset, x1 - offset + minSize, y2 + offset, mPaint);
        canvas.drawLine(x1 - offset, y2 + offset_2, x1 - offset, y2 + offset - minSize, mPaint);
        canvas.drawLine(x2 + offset_2, y2 + offset, x2 + offset - minSize, y2 + offset, mPaint);
        canvas.drawLine(x2 + offset, y2 + offset_2, x2 + offset, y2 + offset - minSize, mPaint);
        invalidate();
    }

    private void setupScanning() {
        animatingScanning = true;
        float heightRatio = 0.3f;
        int height = (int) (bitmapHeight * heightRatio);

        scanningBoxStep = (int) ((bitmapHeight * (1-heightRatio)) / 50f);

        scanningBoxPosition[0] = originX;
        scanningBoxPosition[1] = originX + bitmapWidth;
        scanningBoxPosition[2] = originY;
        scanningBoxPosition[3] = originY + height;
    }

    private void setUpExpanding(int centerX, int centerY, ScalableBox dot) {
        animatingExpanding = true;
        int step = 1;

        float aspectRation = (targetingBoxes[3] - targetingBoxes[2]) * 1.0f / (targetingBoxes[1] - targetingBoxes[0]);
        startingCenter[0] = centerX - step;
        startingCenter[1] = centerX + step;
        startingCenter[2] = (int)(centerY - step * aspectRation);
        startingCenter[3] = (int)(centerY + step * aspectRation);

        targetingBoxes[0] = dot.getX1();
        targetingBoxes[1] = dot.getX2();
        targetingBoxes[2] = dot.getY1();
        targetingBoxes[3] = dot.getY2();
    }

    private void setDisplayBoxes(List<ScalableBox> originalBoxes) {
        displayBoxes.clear();
        for (ScalableBox originalBox : originalBoxes) {
            ScalableBox displayBox = new ScalableBox(originalBox.getX1(), originalBox.getY1(), originalBox.getX2(), originalBox.getY2());

            if (originalBox.getX1() >= 0
                    && originalBox.getX2() > 0
                    && originalBox.getY1() >= 0
                    && originalBox.getY2() > 0) {

                Log.d(SELECTION_VIEW,
                        "original box: + (" + originalBox.getX1() + " " + originalBox.getY1() + ")"
                                + " (" + originalBox.getX2() + " " + originalBox.getY2() + ")");

                float scale = ((float) editableImage.getFitSize()[0]) / editableImage.getActualSize()[0];
                int scaleX1 = (int) Math.ceil((originalBox.getX1() * scale) + originX);
                int scaleX2 = (int) Math.ceil((originalBox.getX2() * scale) + originX);
                int scaleY1 = (int) Math.ceil((originalBox.getY1() * scale) + originY);
                int scaleY2 = (int) Math.ceil((originalBox.getY2() * scale) + originY);

                //resize the box size to image
                displayBox.setX1(scaleX1);
                displayBox.setX2(scaleX2);
                displayBox.setY1(scaleY1);
                displayBox.setY2(scaleY2);
            } else {
                displayBox.setX1(originX);
                displayBox.setX2(originX + bitmapWidth);
                displayBox.setY1(originY);
                displayBox.setY2(originY + bitmapHeight);
            }
            displayBoxes.add(displayBox);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void drawDot(Canvas canvas) {
        mPaint.setStrokeWidth(cornerWidth);

        for (ScalableBox dot : displayBoxes) {
            if (displayBoxes.indexOf(dot) != editableImage.getActiveBoxIdx()) {
                int centerX = (dot.getX1() + dot.getX2()) / 2;
                int centerY = (dot.getY1() + dot.getY2()) / 2;

                mPaint.setColor(dotColor);
                canvas.drawOval(centerX - dotRadius, centerY - dotRadius, centerX + dotRadius,
                        centerY + dotRadius, mPaint);
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int[] loc = new int[2];
        getLocationOnScreen(loc);
        int curX = (int) motionEvent.getRawX();
        int curY = (int) motionEvent.getRawY();


        if (animatingExpanding || animatingScanning || !enableCropping) {
            return false;
        }


        // or box scaling and moving
        int activeIdx = editableImage.getActiveBoxIdx();
        if (activeIdx < 0) {
            return false;
        }


        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                editableImage.setCurrentMode(EditableImage.WorkFlow.CONFIRMING);
                prevX = curX;
                prevY = curY;

                prevBoxX1 = editableImage.getActiveBox().getX1();
                prevBoxX2 = editableImage.getActiveBox().getX2();
                prevBoxY1 = editableImage.getActiveBox().getY1();
                prevBoxY2 = editableImage.getActiveBox().getY2();

                return true;

            case MotionEvent.ACTION_MOVE:
                //Log.d(TAG, "Action Move");
                int diffX = curX - prevX;
                int diffY = curY - prevY;

                if(editableImage.isConfirming()) {
                    displayBoxes.get(activeIdx).resizeBox(curX - loc[0], curY - loc[1], diffX, diffY,
                            (getWidth() - bitmapWidth) / 2,
                            (getHeight() - bitmapHeight) / 2,
                            (getWidth() + bitmapWidth) / 2,
                            (getHeight() + bitmapHeight) / 2,
                            (int) cornerLength);
                    updateOriginalBox();
                    invalidate();
                }
                prevX = curX;
                prevY = curY;
                return true;

            case MotionEvent.ACTION_UP:

                int pointX = curX - loc[0];
                int pointY = curY - loc[1];
                for (ScalableBox dot : displayBoxes) {
                    if (displayBoxes.indexOf(dot) != editableImage.getActiveBoxIdx()) {
                        int x1 = dot.getX1();
                        int x2 = dot.getX2();
                        int y1 = dot.getY1();
                        int y2 = dot.getY2();
                        int dotX = (x1 + x2) / 2;
                        int dotY = (y1 + y2) / 2;

                        if ((dotX - dotRadius <= pointX) && (pointX <= dotX + dotRadius) &&
                                (dotY - dotRadius <= pointY) && (pointY <= dotY + dotRadius)
                                ) {
                            // expand the box
                            setUpExpanding(dotX, dotY, dot);
                            editableImage.setActiveBoxIdx(displayBoxes.indexOf(dot));
                            editableImage.setCurrentMode(EditableImage.WorkFlow.CONFIRMING);

                            // reset the display box
                            setDisplayBoxes(editableImage.getBoxes());
                            invalidate();
                            return false;
                        }
                    }
                }

                editableImage.setCurrentMode(EditableImage.WorkFlow.CONFIRMED);
                ScalableBox originalBox = editableImage.getActiveBox();
                if (onBoxChangedListener != null
                        && (prevBoxX1 != originalBox.getX1()
                        || prevBoxX2 != originalBox.getX2()
                        || prevBoxY1 != originalBox.getY1()
                        || prevBoxY2 != originalBox.getY2())) {
                    onBoxChangedListener.onChanged(originalBox.getX1(), originalBox.getY1(), originalBox.getX2(), originalBox.getY2());
                }
        }
        return false;
    }

    private boolean isClickInShadowArea(int pointX, int pointY) {
        //Log.d(TAG, "pointX: "+ pointX +" pointY: " + pointY);
        ScalableBox box = displayBoxes.get(editableImage.getActiveBoxIdx());
        //Log.d(TAG, "box-> x1: "+box.getX1()+ " x2: "+ box.getX2() + " y1: "+box.getY1()+" y2: "+box.getY2());

        if(box.getX1() - boxBuffer <= pointX && pointX <= box.getX2() + boxBuffer && box.getY1() - boxBuffer <= pointY && pointY <= box.getY2() + boxBuffer) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Calculate the relative position of the box w.r.t the bitmap size
     * Return a new box that can be used in uploading
     */
    public void updateOriginalBox() {
        int viewWidth = editableImage.getViewWidth();
        int viewHeight = editableImage.getViewHeight();
        int width = editableImage.getOriginalImage().getWidth();
        int height = editableImage.getOriginalImage().getHeight();
        ScalableBox displayBox = displayBoxes.get(editableImage.getActiveBoxIdx());

        float ratio = width / (float) height;
        float viewRatio = viewWidth / (float) viewHeight;
        float factor;

        //width dominate, fit w
        if (ratio > viewRatio) {
            factor = viewWidth / (float) width;
        } else {
            //height dominate, fit h
            factor = viewHeight / (float) height;
        }

        float coorX, coorY;
        coorX = (viewWidth - width * factor) / 2f;
        coorY = (viewHeight - height * factor) / 2f;

        int originX1 = (displayBox.getX1() - coorX) / factor <= width ? (int) ((displayBox.getX1() - coorX) / factor) : width;
        int originY1 = (displayBox.getY1() - coorY) / factor <= height ? (int) ((displayBox.getY1() - coorY) / factor) : height;
        int originX2 = (displayBox.getX2() - coorX) / factor <= width ? (int) ((displayBox.getX2() - coorX) / factor) : width;
        int originY2 = (displayBox.getY2() - coorY) / factor <= height ? (int) ((displayBox.getY2() - coorY) / factor) : height;
        editableImage.getActiveBox().setX1(originX1);
        editableImage.getActiveBox().setY1(originY1);
        editableImage.getActiveBox().setX2(originX2);
        editableImage.getActiveBox().setY2(originY2);
    }

}
