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
 * Created by Arsen Vartbaronov on 11.05.16.
 */

package com.Webtrekk.SDKTest;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static java.lang.Thread.sleep;

public class TagIntegrationTest extends WebtrekkBaseMainTest {

    @Rule
    public final WebtrekkTestRule<TagIntegrationActivity> mActivityRule =
            new WebtrekkTestRule<>(TagIntegrationActivity.class, this);

    @Override
    public void before() throws Exception {
        super.before();
        cleanConfigPreference();
        this.initWebtrekk(R.raw.webtrekk_config_tag_integration_test);
        mWebtrekk.getCustomParameter().put("AT1", "AT1Value");
        mWebtrekk.getCustomParameter().put("AT2", "AT2Value");
        mWebtrekk.getCustomParameter().put("AT3", "AT3Value");
        mWebtrekk.getCustomParameter().put("asdf", "seachString");
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testTagIntegration()
    {
        while (!HelperFunctions.getWebTrekkSharedPreference(getInstrumentation().getTargetContext()).
                  contains(Webtrekk.PREFERENCE_KEY_CONFIGURATION))
        {
            getInstrumentation().waitForIdleSync();
        }

        mWaitMilliseconds = 20000;
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("ATS1", parcel.getValue("cg11"));
        assertEquals("AT1Value", parcel.getValue("cg10"));
        assertEquals("ATS1", parcel.getValue("cp3"));
        assertEquals("AT1Value", parcel.getValue("cp4"));
        assertEquals("AT1Value", parcel.getValue("cp8"));
        assertEquals(parcel.getValue("cp9"), "ATS1");
        assertEquals(parcel.getValue("cs5"), "ATS1");
        assertEquals(parcel.getValue("cs6"), "AT1Value");
        assertEquals(parcel.getValue("cd"), "AT1Value");
        assertEquals(parcel.getValue("uc7"), "AT1Value");
        assertEquals(parcel.getValue("co"), "AT1Value");
        assertEquals(parcel.getValue("is"), "seachString");
    }

    @Test
    public void testGlobalParameter(){
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.getCustomParameter().put("key", "KeyValue");
                mWebtrekk.getGlobalTrackingParameter().add(TrackingParameter.Parameter.PAGE, "103", "key");
                mWebtrekk.track();
            }
        });
        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("KeyValue", parcel.getValue("cp103"));
    }
}

