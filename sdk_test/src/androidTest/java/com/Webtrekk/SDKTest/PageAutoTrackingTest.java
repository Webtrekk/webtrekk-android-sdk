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
 * Created by Arsen Vartbaronov on 09.05.16.
 */

package com.Webtrekk.SDKTest;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.filters.LargeTest;
import android.test.TouchUtils;
import android.util.Log;
import android.widget.Button;

import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class PageAutoTrackingTest extends WebtrekkBaseMainTest {

    @Rule
    public final WebtrekkTestRule<MainActivity> mActivityRule =
            new WebtrekkTestRule<>(MainActivity.class, null, false, true);

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        this.initWebtrekk();
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testActivityChanging() {

        Instrumentation instrumentation = getInstrumentation();

        TrackActivityIsStopped callback = new TrackActivityIsStopped("PageExampleActivity");
        mApplication.registerActivityLifecycleCallbacks(callback);

        //track main activity
        initWaitingForTrack(null);
        Activity mainActivity = mActivityRule.launchActivity(null);

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);
        assertTrue(parcel.getValue("p").contains("Startseite"));

        //track first page activity
        initWaitingForTrack(null);

        onView(withId(R.id.button_page)).perform(click());

        URL = waitForTrackedURL();
        parcel.parseURL(URL);
        assertTrue(parcel.getValue("p").contains("Seite"));

        //track next page activity
        initWaitingForTrack(null);

        onView(withId(R.id.button_next_page)).perform(click());

        URL = waitForTrackedURL();
        parcel.parseURL(URL);
        assertTrue(parcel.getValue("p").contains("NextPage"));

        //track first page activity
        initWaitingForTrack(null);

        //wait while PageExampleActivity is stopped
        while (!callback.isStoped()){
            instrumentation.waitForIdleSync();
        }

        pressBack();

        URL = waitForTrackedURL();
        parcel.parseURL(URL);
        assertTrue(parcel.getValue("p").contains("Seite"));

        //track main activity
        initWaitingForTrack(null);

        pressBack();

        URL = waitForTrackedURL();
        parcel.parseURL(URL);
        assertTrue(parcel.getValue("p").contains("Startseite"));

        mApplication.unregisterActivityLifecycleCallbacks(callback);
    }

    static class TrackActivityIsStopped implements Application.ActivityLifecycleCallbacks{
        private final String mActivityName;
        private boolean mIsStoped;

        TrackActivityIsStopped(String activityName){
            mActivityName = activityName;
        }

        boolean isStoped(){
            return mIsStoped;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (activity.getLocalClassName().equals(mActivityName)){
                mIsStoped = false;
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity.getLocalClassName().equals(mActivityName)){
                mIsStoped = true;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}