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

package com.visenze.vimobile.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.visenze.videtection.ViMobileSDKManager;
import com.visenze.videtection.code.Code;
import com.visenze.vimobile.demo.utils.PermUtils;
import com.visenze.vimobile.demo.utils.PreferenceUtils;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    private final static String TAG = "EntryActivity";
    private static final int DURATION = 100;
    private final Handler mHideHandler = new Handler();
    private AlertDialog alertDialog;
    private final Runnable goToLiveMode = () -> {
        Activity activity = SplashActivity.this;
        activity.startActivity(new Intent(activity, CameraActivity.class));
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        View mContentView = findViewById(R.id.progress);

        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        loadDetectionSDK();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PermUtils.allPermissionsGranted(this)) {
            PreferenceUtils.putBooleanPref(this, R.string.is_permenant_disabled_permission, false);
            gotoLiveMode(DURATION);
        } else {
            boolean permenantDenied = PreferenceUtils.getBooleanPref(this, R.string.is_permenant_disabled_permission, false);
            if (permenantDenied) {
                Log.d(TAG, "on resume. denied permanently");
                getGotoSetting();
            }
        }
    }

    /**
     * initialize visenze detection SDK
     */
    private void loadDetectionSDK() {
        String token = getApplication().getResources().getString(R.string.videtection_token);
        ViMobileSDKManager.getInstance()
                .initWithToken(
                        getApplicationContext(), token,
                        (code, message) -> {
                            if (code != Code.SUCCESS.getCode()) {
                                Log.d(TAG, "failed to init sdk: " + message);
                                String errorMessage = "Cannot init videtection SDK: " + message;
                                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG);
                                toast.show();
                            } else {
                                // successfully init, popup the permission dialog
                                Log.d(TAG, "required permission");
                                PermUtils.requestRuntimePermissions(this);
                            }
                        }
                );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NotNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                // This is Case 2 (Permission is now granted)
            } else {
                // This is Case 1 again as Permission is not granted by user

                //Now further we check if used denied permanently or not
                if (!ActivityCompat.shouldShowRequestPermissionRationale(SplashActivity.this,
                        permissions[i])) {
                    // case 4 User has denied permission but not permanently
                    Log.d(TAG, "result, denied permanently");
                    PreferenceUtils.putBooleanPref(this, R.string.is_permenant_disabled_permission, true);
                    getGotoSetting();
                } else {
                    Log.d(TAG, "required permission");
                    PermUtils.requestRuntimePermissions(this);
                }
            }
        }
    }

    private void getGotoSetting() {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_required)
                    .setMessage(R.string.go_to_setting)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    })
                    .setCancelable(false)
                    .create();
        }
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getResources().getColor(R.color.darkBlue));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.white));
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void gotoLiveMode(int duration) {
        mHideHandler.postDelayed(goToLiveMode, duration);
    }
}
