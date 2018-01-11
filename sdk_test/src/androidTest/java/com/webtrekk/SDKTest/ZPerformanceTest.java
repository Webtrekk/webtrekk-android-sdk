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
 * Created by Arsen Vartbaronov on 01.06.16.
 */

package com.webtrekk.SDKTest;

import android.support.test.filters.LargeTest;

import com.webtrekk.webtrekksdk.Request.RequestUrlStore;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Name it with Z to call it at last.
 */
@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class ZPerformanceTest extends WebtrekkBaseMainTest {

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, this);

    @Override
    public void before() throws Exception{
        super.before();
        boolean useManual = WebtrekkBaseMainTest.mTestName.equals("testFileCorruption")
                || WebtrekkBaseMainTest.mTestName.equals("testTimePerformance")
                || WebtrekkBaseMainTest.mTestName.equals("testSavingToFlashByTimeout");
        int configurationXMLID = useManual ? R.raw.webtrekk_config_manual_flush : R.raw.webtrekk_config_performance_test;
        this.initWebtrekk(configurationXMLID);
        mHttpServer.resetRequestNumber();
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }


    @Test
    //works in reality only on real HW
    public void testTimePerformance()
    {
        if (isRestrictedMode()){
            return;
        }

        final int numberOfTest = 1000;
        long timeDifSum = 0;

        setStartMessageNumber();

        for (int i = 0; i<numberOfTest; i++)
        {
            Thread.yield();
            long before = System.currentTimeMillis();
            mWebtrekk.track();
            long timeOfTrack = System.currentTimeMillis() - before;
            WebtrekkLogging.log("time of track("+i+"):"+timeOfTrack);
            timeDifSum += timeOfTrack;
        }

        float result = timeDifSum/numberOfTest;

        WebtrekkLogging.log("Performance test is shown:"+result + " milliseconds per call");

        mWebtrekk.send();
        waitForMessages(numberOfTest);

        assertTrue("Performance test is shown:"+result + " milliseconds per call", result < 10);
    }

    @Test
    public void testMessageNumberPerformance() {

        if (isRestrictedMode()){
            return;
        }

        final int numberOfTest = 20000;

        setStartMessageNumber();

        for (int i = 0; i < numberOfTest; i++) {
            WebtrekkLogging.log("track:" + i);
            mWebtrekk.track();
        }

        waitForMessages(numberOfTest);
    }

    @Test
    public void testSavingToFlashByTimeout()
    {
        RequestUrlStore urlStore = new RequestUrlStore(getInstrumentation().getTargetContext());

        File file = urlStore.getRequestStoreFile();
        long length = file.length();
        mWebtrekk.track();

        // sleep for a while to make saving happends

        try {
            for(int i = 0; i< 100 && file.length() <= length; i++)
            {
                Thread.sleep(950);
                Thread.yield();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(file.length() > length);

        initWaitingForTrack(null);
        mWebtrekk.send();
        waitForTrackedURLs();
    }

    @Test
    public void testFileCorruption()
    {
        RequestUrlStore urlStore = new RequestUrlStore(getInstrumentation().getTargetContext());
        final File file = urlStore.getRequestStoreFile();

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 30; i++) {
                    mWebtrekk.track();
                }
                file.setReadable(false, false);
            }
        }, 20);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mWebtrekk.send();
        waitForTrackedURLs();
        file.setReadable(true, false);
    }
}
