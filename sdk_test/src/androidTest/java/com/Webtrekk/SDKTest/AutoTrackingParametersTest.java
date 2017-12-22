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
 * Created by Arsen Vartbaronov on 17.11.16.
 */

package com.Webtrekk.SDKTest;

import android.support.test.filters.LargeTest;
import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Webtrekk;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class AutoTrackingParametersTest extends WebtrekkBaseMainTest {
    private final String cs807New = "newcs807";

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, this);

    @Override
    public void before() throws Exception {
        super.before();
        mWebtrekk = Webtrekk.getInstance();
        if (WebtrekkBaseMainTest.mTestName.equals("testSimpleAutoTest"))
        {
            this.initWebtrekk(R.raw.webtrekk_config_parameters_auto_track_test);
        } else
        {
            this.initWebtrekk(R.raw.webtrekk_config_parameters_auto_track_test_complex);
        }
    }

    @After
    @Override
    public void after() throws Exception {
        super.after();
    }

// 811 and 812 isn't supported on emulator
    @Test
    public void testSimpleAutoTest()
    {
        if (isRestrictedMode()){
            return;
        }

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertTrue(parcel.getValue("cs804") != null && !parcel.getValue("cs804").isEmpty());
        assertTrue(parcel.getValue("cs805") != null && !parcel.getValue("cs805").isEmpty());
        assertTrue(parcel.getValue("cs807") != null && !parcel.getValue("cs807").isEmpty());
        assertTrue(parcel.getValue("cs809") != null && !parcel.getValue("cs809").isEmpty());
        //assertTrue(parcel.getValue("cs810") != null && !parcel.getValue("cs810").isEmpty());
        assertTrue(parcel.getValue("cs813") != null && !parcel.getValue("cs813").isEmpty());
        assertTrue(parcel.getValue("cs814") != null && !parcel.getValue("cs814").isEmpty());
        assertTrue(parcel.getValue("cs815") != null && !parcel.getValue("cs815").isEmpty());
        assertTrue(parcel.getValue("cs816") != null && !parcel.getValue("cs816").isEmpty());
        assertTrue(parcel.getValue("cp783") != null && !parcel.getValue("cp783").isEmpty());
        assertTrue(parcel.getValue("cp784") != null && !parcel.getValue("cp784").isEmpty());
        assertTrue(parcel.getValue("cs808") == null);
    }

    // test how overwriten works
    @Test
    public void testComplexAutoTest()
    {
        if (isRestrictedMode()){
            return;
        }

        final String difValue = "differentValue";
        final String difPageValue = "newcp783";
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                TrackingParameter tp = new TrackingParameter();
                tp.add(TrackingParameter.Parameter.SESSION, "809", difValue);
                mWebtrekk.getCustomParameter().put("newcp783key", difPageValue);

                mWebtrekk.track(tp);
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertTrue(parcel.getValue("cs804") != null && !parcel.getValue("cs804").isEmpty());
        assertTrue(parcel.getValue("cs805") != null && !parcel.getValue("cs805").isEmpty());
        assertEquals(cs807New, parcel.getValue("cs807"));
        assertEquals(difValue, parcel.getValue("cs809"));
        //assertTrue(parcel.getValue("cs810") != null && !parcel.getValue("cs810").isEmpty());
        assertTrue(parcel.getValue("cs813") != null && !parcel.getValue("cs813").isEmpty());
        assertTrue(parcel.getValue("cs814") != null && !parcel.getValue("cs814").isEmpty());
        assertTrue(parcel.getValue("cs815") != null && !parcel.getValue("cs815").isEmpty());
        assertTrue(parcel.getValue("cs816") != null && !parcel.getValue("cs816").isEmpty());
        assertEquals(difPageValue, parcel.getValue("cp783"));
        assertTrue(parcel.getValue("cp784") != null && !parcel.getValue("cp784").isEmpty());
        assertTrue(parcel.getValue("cs808") != null && !parcel.getValue("cs808").isEmpty());
    }
}
