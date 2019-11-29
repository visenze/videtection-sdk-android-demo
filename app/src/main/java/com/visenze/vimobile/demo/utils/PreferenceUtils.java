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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.visenze.vimobile.demo.R;

import androidx.annotation.StringRes;


/** Utility class to retrieve shared preferences. */
public class PreferenceUtils {

  public static int getConfirmationTimeMs(Context context) {
    return getIntPref(context, R.string.pref_key_confirmation_time_in_auto_search, 1000);
  }

  public static void putIntPref(Context context, int prefKeyId, int value) {
    PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putInt(context.getString(prefKeyId), value)
            .apply();
  }

  public static int getIntPref(Context context, @StringRes int prefKeyId, int defaultValue) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getInt(prefKey, defaultValue);
  }

  public static void putBooleanPref(Context context, int prefKeyId, boolean value) {
    PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(context.getString(prefKeyId), value)
            .apply();
  }

  public static boolean getBooleanPref(
          Context context, @StringRes int prefKeyId, boolean defaultValue) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String prefKey = context.getString(prefKeyId);
    return sharedPreferences.getBoolean(prefKey, defaultValue);
  }
}
