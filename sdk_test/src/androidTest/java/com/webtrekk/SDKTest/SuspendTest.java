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
 * Created by Arsen Vartbaronov on 10.05.16.
 */

package com.webtrekk.SDKTest;

import com.webtrekk.webtrekksdk.Request.RequestUrlStore;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SuspendTest extends WebtrekkBaseMainTest {
    private Webtrekk mWebtrekk;
    final static long DELAY_FOR_SEND = 30000;
    final static int MESSAGES_NUMBER = 500;
    final static String SUSPEND_TEST_RECEIVED_MESSAGE = "SUSPEND_TEST_RECEIVED_MESSAGE";

    @Rule
    public final WebtrekkTestRule<SuspendActivity> mActivityRule =
            new WebtrekkTestRule<>(SuspendActivity.class, null, false, false);

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        mWebtrekk = Webtrekk.getInstance();
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testBeforeGoBackgroundHome()
    {
        if (!mIsExternalCall)
            return;

        RequestUrlStore store = new RequestUrlStore(getInstrumentation().getTargetContext());
        store.deleteRequestsFile();
        long currentMessageNumber = mHttpServer.getCurrentRequestNumber();

        mActivityRule.launchActivity(null);
        mHttpServer.setDelay(100);

        mStringNumbersToWait = MESSAGES_NUMBER;

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < MESSAGES_NUMBER; i++)
                {
                    mWebtrekk.track();
                }
            }
        });

        try {
            Thread.sleep(DELAY_FOR_SEND);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long messageReceived = mHttpServer.getCurrentRequestNumber() - currentMessageNumber;

        WebtrekkLogging.log("Backgroud test. Wait for message number:"+(MESSAGES_NUMBER - messageReceived));
        mHttpServer.setDelay(0);
        mWaitMilliseconds = 20000;
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {

            }
        }, MESSAGES_NUMBER - messageReceived);

        waitForTrackedURLs();
    }
}