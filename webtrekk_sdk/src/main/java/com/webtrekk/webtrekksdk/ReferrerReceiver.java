/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Webtrekk GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by Robert Goericke on 15.12.15.
 */

package com.webtrekk.webtrekksdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

public class ReferrerReceiver extends BroadcastReceiver {

    private static final String TAG = BroadcastReceiver.class.getName();

    /* @hide */
    public static final String REFERRER_KEY_NAME = "WEBTREKK_REFERRER";
    /* @hide */
        public static final String KEY_REFERRER = "referrer";

    /**
     * @hide
     * Process referrer after first install. It just saved referrer to preference.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        WebtrekkLogging.log("New action for referrer is received:"+intent.getAction());

        String campaign = intent.getStringExtra(KEY_REFERRER);

        if (!"com.android.vending.INSTALL_REFERRER".equals(intent.getAction()) || campaign == null) {
            return;
        }

        SharedPreferences preference = HelperFunctions.getWebTrekkSharedPreference(context);

        preference.edit().putString(REFERRER_KEY_NAME, campaign).apply();

        WebtrekkLogging.log("New referrer is received:"+campaign);
    }
}