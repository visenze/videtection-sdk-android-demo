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

package com.visenze.vimobile.demo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentActivity;

public class ImageUtils {
    public static final int REQUEST_CODE_PHOTO_LIBRARY = 1;

    private static final String TAG = "ImageUtils";

    public static Bitmap getRoundCornerBitmap(Bitmap srcBitmap, int cornerRadius) {
        if (srcBitmap == null) return srcBitmap;
        Bitmap dstBitmap =
                Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        RectF rectF = new RectF(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(srcBitmap, 0, 0, paint);
        return dstBitmap;
    }

    public static Bitmap getFixSizeRoundCornerBitmap(Bitmap srcBitmap, int width, int height, int cornerRadius) {

        Bitmap bmp =
                Bitmap.createScaledBitmap(srcBitmap, width, height, false);
        return getRoundCornerBitmap(bmp, cornerRadius);
    }

    public static String saveImageByte(byte[] bytes) throws IOException {
        File imageFile = null;
        File root;
        //Create directory..
        long timeStamp = SystemClock.elapsedRealtime();  // time stamp in second
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/Visenze");
        File dir = new File(root + File.separator);
        if (!dir.exists()) dir.mkdir();

        //Create file..
        String fileName = timeStamp + ".jpg";
        imageFile = new File(dir, fileName);

        try (FileOutputStream out = new FileOutputStream(imageFile)) {
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile.getPath();
    }

    public static void openImagePicker(FragmentActivity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_CODE_PHOTO_LIBRARY);
    }

    public static Bitmap loadImage(Context context, Uri imageUri, int maxImageDimension) throws IOException {
        InputStream inputStreamForSize = null;
        InputStream inputStreamForImage = null;
        try {
            inputStreamForSize = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStreamForSize, /* outPadding= */ null, opts);
            int inSampleSize =
                    Math.max(opts.outWidth / maxImageDimension, opts.outHeight / maxImageDimension);

            opts = new BitmapFactory.Options();
            opts.inSampleSize = inSampleSize;
            inputStreamForImage = context.getContentResolver().openInputStream(imageUri);
            Bitmap decodedBitmap =
                    BitmapFactory.decodeStream(inputStreamForImage, /* outPadding= */ null, opts);
            return maybeTransformBitmap(context.getContentResolver(), imageUri, decodedBitmap);

        } finally {
            if (inputStreamForSize != null) {
                inputStreamForSize.close();
            }
            if (inputStreamForImage != null) {
                inputStreamForImage.close();
            }
        }
    }

    private static Bitmap maybeTransformBitmap(ContentResolver resolver, Uri uri, Bitmap bitmap) {
        int orientation = getExifOrientationTag(resolver, uri);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
                // Set the matrix to be null to skip the image transform.
                matrix = null;
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix = new Matrix();
                matrix.postScale(-1.0f, 1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180.0f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1.0f, -1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(-90.0f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(-90.0f);
                matrix.postScale(-1.0f, 1.0f);
                break;
            default:
                // Set the matrix to be null to skip the image transform.
                matrix = null;
                break;
        }

        if (matrix != null) {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            return bitmap;
        }
    }

    private static int getExifOrientationTag(ContentResolver resolver, Uri imageUri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(imageUri.getScheme())
                && !ContentResolver.SCHEME_FILE.equals(imageUri.getScheme())) {
            return 0;
        }

        ExifInterface exif = null;
        try (InputStream inputStream = resolver.openInputStream(imageUri)) {
            if (inputStream != null) {
                exif = new ExifInterface(inputStream);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to open file to read rotation meta data: " + imageUri, e);
        }

        return exif != null
                ? exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                : ExifInterface.ORIENTATION_UNDEFINED;
    }
}
