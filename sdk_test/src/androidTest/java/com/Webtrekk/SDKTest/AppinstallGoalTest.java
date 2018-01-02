package com.Webtrekk.SDKTest;

import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

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
 * Created by Arsen Vartbaronov on 20.10.17.
 */

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class AppinstallGoalTest extends WebtrekkBaseMainTest {

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, this);

    @Override
    public void before() throws Exception {
        super.before();
        if (WebtrekkBaseMainTest.mTestName.equals("testGoalReceived")){
            //remove test processed goal
            getPreference().edit().remove("appinstallGoalProcessed").apply();
        }
        this.initWebtrekk(R.raw.webtrekk_config_no_auto_track);
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testGoalReceived(){

        waitForCampaignCompleted(true);
    }

    @Test
    public void testGoalNotReceived(){

        waitForCampaignCompleted(false);
    }

    private void waitForCampaignCompleted(final boolean isExisted){

        waitForFinishedCampaignProcess(new Runnable() {
            @Override
            public void run() {
                checkForcb900(isExisted);
            }
        });
    }

    private void checkForcb900(boolean ifExisted){
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        if (ifExisted){
            final String cb900 = parcel.getValue("cb900");
            assertTrue(cb900, cb900 != null && cb900.equals("1"));
        } else {
            assertTrue(parcel.getValue("cb900") == null);
        }
    }

    private SharedPreferences getPreference(){
        return HelperFunctions.getWebTrekkSharedPreference(mApplication);
    }

}
