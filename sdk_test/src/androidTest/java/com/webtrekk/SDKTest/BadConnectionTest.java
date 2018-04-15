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
 * Created by Arsen Vartbaronov on 24.05.16.
 */

package com.webtrekk.SDKTest;

import android.support.test.filters.LargeTest;

import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class BadConnectionTest extends WebtrekkBaseMainTest {

    private static final int TRACKING_CALLS_STACK = 1000;


    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, this);

    @Override
    public void before() throws Exception {
        super.before();
        this.initWebtrekk(R.raw.webtrekk_config_connection_broken_request);
    }

    @After
    @Override
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testLostConnection()
    {
        long messageReeivedCounter = mHttpServer.getCurrentRequestNumber();

        mWaitMilliseconds = 70000;
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < TRACKING_CALLS_STACK; i++)
                {
                    mWebtrekk.track();
                }
            }
        });

        //Wait for some message starts to send.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            WebtrekkLogging.log("Sleep interruction");
        }

        //stop http server - emulator connection brakes
        mHttpServer.stop();
        WebtrekkLogging.log("Stop HTTP Server");

        //wait sometime
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            WebtrekkLogging.log("Sleep interruction");
        }

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                try {
                    WebtrekkLogging.log("Start HTTP Server");
                    synchronized (mHttpServer) {
                        mHttpServer.start();
                    }
                } catch (IOException e) {
                    WebtrekkLogging.log("testLostConnection. Can't start server one more time");
                }
            }
        }, TRACKING_CALLS_STACK - (mHttpServer.getCurrentRequestNumber() - messageReeivedCounter));


        waitForTrackedURLs();
    }

    @Test
    public void testSlowConnection(){
        final int delay = 30*1000;

        // make significant delay in response
        WebtrekkLogging.log("Setup HTTP request delay");
        mHttpServer.setBeforeDelay(delay);

        try {

            // do some track
            mWaitMilliseconds = delay;
            initWaitingForTrack(new Runnable() {
                @Override
                public void run() {
                    mWebtrekk.track();
                }
            });

            //Wait for 2 seconds to make SDK start to send message.
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                WebtrekkLogging.log("Sleep interruction");
            }

            //make sure you can close activity in less then 5 sec (stop is called)

            EmptyActivity activity = (EmptyActivity) mActivityRule.getActivity();

            activity.finish();

            // due to difference time when onStop start to process
            // on a test server and a local machine at first wait while onStart is started
            // to call

            try {
                WebtrekkLogging.log("start to wait activity stop procedure");

                // wait while on start is called
                while (!activity.isStartedToStopping()) {
                    Thread.yield();
                }

                WebtrekkLogging.log("on stoping is started");

                // just wait 5 seconds to check ANR not happened
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                WebtrekkLogging.log("Sleep interruction");
            }

            // check that activity is stopped if not this is ANR
            assertTrue(activity.isStopped());

            // wait for finishing activity
            mActivityRule.afterActivityFinished();
        }finally {
            //change delay to zero and cancel current delay
            WebtrekkLogging.log("Cancel HTTP request delay.");
            mHttpServer.setBeforeDelay(0);
            mHttpServer.stopBeforeDelay();
        }

        // receive tracks
        waitForTrackedURLs();

    }
}