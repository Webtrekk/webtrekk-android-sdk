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
 * Created by Arsen Vartbaronov on 14.04.16.
 */

package com.Webtrekk.SDKTest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestRunner;

import com.mixpanel.android.util.StringUtils;
import com.webtrekk.webtrekksdk.Modules.ExceptionHandler;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ErrorHandlerTest extends WebtrekkBaseMainTest {

    public ErrorHandlerTest(){
        super();
        mIsErrorHandlerTest = true;
    }

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, null, false, false);

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
    public void testCatchedError()
    {
        deleteErrorHandlerFile(mApplication);
        this.initWebtrekk();

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                String s = null;
                try {
                    s.length();
                } catch (NullPointerException e) {
                    mWebtrekk.trackException(e);
                }
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();
        parcel.parseURL(URL);

        URL = URL.substring(URL.indexOf("ct=webtrekk_ignore"), URL.length());
        assertEquals(parcel.getValue("ct"), "webtrekk_ignore");
        assertEquals(parcel.getValue("ck910"), "2");
        assertEquals(parcel.getValue("ck911"), "java.lang.NullPointerException");
        if (!isRestrictedMode()) {
            assertEquals(parcel.getValue("ck912"), "Attempt+to+invoke+virtual+method+%27int+java.lang.String.length%28%29%27+on+a+null+object+reference");
        }
        assertEquals(getNormString(parcel.getValue("ck914")), "com.Webtrekk.SDKTest.ErrorHandlerTest%241.run%28ErrorHandlerTest.java%3A%29%7Cjava.lang.Thread.run%28Thread.java%29");
    }

    @Test
    public void testInfoError()
    {
        deleteErrorHandlerFile(mApplication);
        this.initWebtrekk();
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {

                mWebtrekk.trackException("nameEx", "messsage Ex");
            }
        });

        String URL = waitForTrackedURL();
        URL = URL.substring(URL.indexOf("ct=webtrekk_ignore"), URL.length());
        assertEquals(URL, "ct=webtrekk_ignore&ck910=3&ck911=nameEx&ck912=messsage+Ex&eor=1");
        assertTrue(URL.contains("ct=webtrekk_ignore"));
    }

    /**
     * do unit taste for message more than 255 characters. As logic is the same and case happens rarely we can test only simple case only
     */
    @Test
    public void testStringNormalization(){
        deleteErrorHandlerFile(mApplication);
        this.initWebtrekk();
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                final char[] nameArr = new char[300];
                final char[] messageArr = new char[300];

                Arrays.fill(nameArr, 'n');
                Arrays.fill(messageArr, 'm');

                mWebtrekk.trackException(new String(nameArr), new String(messageArr));
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();
        parcel.parseURL(URL);

        assertEquals(255, parcel.getValue("ck911").length());
        assertEquals(255, parcel.getValue("ck912").length());
    }

    @Test
    public void testFatalCompeteSimple()
    {
        internalTestFatalComplete(1);
    }

    @Test
    public void testFatalCompeteComplex()
    {
        internalTestFatalComplete(2);
    }

    private void internalTestFatalComplete(int errorNumbers)
    {
        mStringNumbersToWait = errorNumbers;

        if (!mIsExternalCall)
            return;

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                ErrorHandlerTest.this.initWebtrekk();
            }
        }, errorNumbers);

        List<String> URL = waitForTrackedURLs();
        assertEquals(URL.size(), errorNumbers);


        for (int i = 0 ; i < URL.size(); i++)
        {
            URLParsel parcel = new URLParsel();
            parcel.parseURL(URL.get(i));

            assertEquals(parcel.getValue("ct"), "webtrekk_ignore");
            assertTrue(HelperFunctions.urlDecode(parcel.getValue("ck911")).length() <= 255);
            assertTrue(HelperFunctions.urlDecode(parcel.getValue("ck912")).length() <= 255);
            assertTrue(HelperFunctions.urlDecode(parcel.getValue("ck913")).length() <= 255);
            assertTrue(HelperFunctions.urlDecode(parcel.getValue("ck914")).length() <= 255);
            assertTrue(HelperFunctions.urlDecode(parcel.getValue("ck915")).length() <= 255);
            assertEquals(parcel.getValue("ck910"), "1");

            switch (i)
            {
                case 0:
                    assertEquals(parcel.getValue("ck911"), "java.lang.RuntimeException");
                    assertEquals(getNormString(parcel.getValue("ck912")), "Unable+to+start+activity+ComponentInfo%7Bcom.Webtrekk.SDKTest%2Fcom.Webtrekk.SDKTest.ThrowExceptionActivity%7D%3A+java.lang.NullPointerException%3A+Attempt+to+invoke+virtual+method+%27int+java.lang.String.length%28%29%27+on+a+null+object+reference");
                    assertEquals(getNormString(parcel.getValue("ck913")), "Attempt+to+invoke+virtual+method+%27int+java.lang.String.length%28%29%27+on+a+null+object+reference");
                    assertEquals(getNormString(parcel.getValue("ck914")), "android.app.ActivityThread.performLaunchActivity%28ActivityThread.java%29%7Candroid.app.ActivityThread.handleLaunchActivity%28ActivityThread.java%29%7Candroid.app.ActivityThread.access%24800%28ActivityThread.java%29%7C");
                    assertEquals(getNormString(parcel.getValue("ck915")), "com.Webtrekk.SDKTest.ThrowExceptionActivity.onCreate%28ThrowExceptionActivity.java%3A%29%7Candroid.app.Activity.performCreate%28Activity.java%29%7Candroid.app.Instrumentation.callActivityOnCreate%28Instrumentation.java%29%7C");
                    break;
                case 1:
                    assertEquals(parcel.getValue("ck911"), "java.lang.RuntimeException");
                    assertEquals(getNormString(parcel.getValue("ck912")), "Unable+to+start+activity+ComponentInfo%7Bcom.Webtrekk.SDKTest%2Fcom.Webtrekk.SDKTest.ThrowExceptionActivity%7D%3A+java.lang.NumberFormatException%3A+Invalid+int%3A+%22sdfsdf%22");
                    assertEquals(getNormString(parcel.getValue("ck913")), "Invalid+int%3A+%22sdfsdf%22");
                    assertEquals(getNormString(parcel.getValue("ck914")), "android.app.ActivityThread.performLaunchActivity%28ActivityThread.java%29%7Candroid.app.ActivityThread.handleLaunchActivity%28ActivityThread.java%29%7Candroid.app.ActivityThread.access%24800%28ActivityThread.java%29%7C");
                    assertEquals(getNormString(parcel.getValue("ck915")), "java.lang.Integer.invalidInt%28Integer.java%29%7Cjava.lang.Integer.parse%28Integer.java%29%7Cjava.lang.Integer.parseInt%28Integer.java%29%7Cjava.lang.Integer.parseInt%28Integer.java%29%7Cjava.lang.Integer.valueOf%28Integer.java%29%7C");
                    break;
            }

        }
    }

    private String getNormString(String orig)
    {
        return orig.replaceAll("%3A([-\\d]+)", "%3A");
    }
}
