package com.visenze.vimobile.demo.productsearch.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.visenze.vimobile.demo.R;
import com.visenze.vimobile.demo.productsearch.view.handler.OnBoxChangedListener;
import com.visenze.vimobile.demo.productsearch.view.handler.OnScanFinishedListener;
import com.visenze.vimobile.demo.productsearch.view.util.ImageHelper;


/**
 * Created by yulu on 12/3/15.
 * <p>
 * View to display photo and selection box
 */
public class EditPhotoView extends FrameLayout {

    private static final int LINE_WIDTH = 2;
    private static final int CORNER_WIDTH = 3;
    private static final int CORNER_LENGTH = 30;

    private Context context;

    private ImageView imageView;
    private SelectionView selectionView;
    private EditableImage editableImage;

    private float lineWidth;
    private float cornerWidth;
    private float cornerLength;
    private int lineColor;
    private int cornerColor;
    private int dotColor;
    private int shadowColor;

    public EditPhotoView(Context context) {
        super(context);
        this.context = context;
    }

    public EditPhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        obtainAttributes(context, attrs);
    }

    public EditPhotoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        obtainAttributes(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateViews(w, h);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void updateViews(int w, int h) {
        //set the default image and selection view
        if (editableImage != null) {
            editableImage.setViewSize(w, h);
            imageView.setImageBitmap(editableImage.getOriginalImage());
            selectionView.setBoxSize(editableImage, editableImage.getBoxes(), w, h);
        }
    }


    /**
     * update view with editable image
     *
     * @param context       activity
     * @param editableImage image to be edited
     */
    public void initView(Context context, EditableImage editableImage) {

        this.editableImage = editableImage;

        selectionView = new SelectionView(context,
                lineWidth, cornerWidth, cornerLength,
                lineColor, cornerColor, dotColor, shadowColor, editableImage);
        imageView = new ImageView(context);

        imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        selectionView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (removeAllChild()) {
            addView(imageView, 0);
            addView(selectionView, 1);
            int width = getWidth();
            int height = getHeight();
            updateViews(width, height);
        } else {
            addView(imageView, 0);
            addView(selectionView, 1);
        }

    }

    public void enableCropping(boolean enable) {
        if (selectionView != null) {
            selectionView.enableCropping(enable);
        }
    }

    // return if there is child view in. remove it if any. this is to update the view.
    private boolean removeAllChild() {
        // Log.d("EditPhotoView", "childCount: "+getChildCount());
        if (getChildCount() > 0) {
            removeAllViews();
            return true;
        } else {
            return false;
        }
    }

    public void setOnBoxChangedListener(OnBoxChangedListener onBoxChangedListener) {
        selectionView.setOnBoxChangedListener(onBoxChangedListener);
    }

    public void setScanFinishedListener(OnScanFinishedListener onScanFinishedListener) {
        selectionView.setOnScanFinishedListener(onScanFinishedListener);
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropLayout);

        lineWidth = ta.getDimension(R.styleable.CropLayout_crop_line_width, ImageHelper.dpToPx(context.getResources(), LINE_WIDTH));
        lineColor = ta.getColor(R.styleable.CropLayout_crop_line_color, Color.parseColor("#ffffff"));
        dotColor = ta.getColor(R.styleable.CropLayout_crop_dot_color, Color.parseColor("#ffffff"));
        cornerWidth = ta.getDimension(R.styleable.CropLayout_crop_corner_width, ImageHelper.dpToPx(context.getResources(), CORNER_WIDTH));
        cornerLength = ta.getDimension(R.styleable.CropLayout_crop_corner_length, ImageHelper.dpToPx(context.getResources(), CORNER_LENGTH));
        cornerColor = ta.getColor(R.styleable.CropLayout_crop_corner_color, Color.parseColor("#ffffff"));
        shadowColor = ta.getColor(R.styleable.CropLayout_crop_shadow_color, Color.parseColor("#aa111111"));

        ta.recycle();
    }

}
