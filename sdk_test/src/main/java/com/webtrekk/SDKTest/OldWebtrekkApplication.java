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
 * Created by Arsen Vartbaronov on 25.05.16.
 */

package com.webtrekk.SDKTest;

import android.app.Application;

import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

public class OldWebtrekkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WebtrekkLogging.log("Old webtrekk init");
        com.webtrekk.android.tracking.Webtrekk.setContext(this);
        com.webtrekk.android.tracking.Webtrekk.setLoggingEnabled(true);
        com.webtrekk.android.tracking.Webtrekk.setOptedOut(false);
        com.webtrekk.android.tracking.Webtrekk.setSamplingRate(0);
        com.webtrekk.android.tracking.Webtrekk.setSendDelay(10000);
        com.webtrekk.android.tracking.Webtrekk.setServerUrl("https://widgetlabsgmbh01.wt-eu02.net");
        com.webtrekk.android.tracking.Webtrekk.setTrackId("164398353394712");
    }
}
