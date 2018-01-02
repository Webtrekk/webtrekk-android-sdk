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
 * Created by Arsen Vartbaronov on 26.04.16.
 */

package com.Webtrekk.SDKTest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.test.filters.LargeTest;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class MiscellaneousTest  extends WebtrekkBaseMainTest {

    private final String TOO_SHORT_EID = "123456789012345678";
    private final String TOO_LONG_EID = "12345678901324567890";
    private final String VALID_EID = "1234567890123465788";


    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, this);

    @Override
    public void before() throws Exception{
        super.before();
        this.initWebtrekk(R.raw.webtrekk_config_no_auto_track);
    }


    @Override
    @After
    public void after() throws Exception {
        super.after();
    }


    @Test
    public void testOverrideEverID() {
        everIDtest(TOO_SHORT_EID, false);
        everIDtest(TOO_LONG_EID, false);
        everIDtest(VALID_EID, true);
        everIDtest(HelperFunctions.generateEverid(), true);
    }

    @Test
    public void testTrackingID() {
        assertEquals(mWebtrekk.getTrackingIDs().get(0), "123451234512345");
        assertEquals(mWebtrekk.getTrackingIDs().get(1), "123451234512346");
        assertEquals(mWebtrekk.getTrackingIDs().get(2), "123451234512347");
        assertEquals(mWebtrekk.getTrackingIDs().get(3), "123451234512348");
        assertEquals(mWebtrekk.getTrackingIDs().get(4), "123451234512349");
    }


    private void everIDtest(String everID, boolean eidIsValid) {
        String oldEverID = mWebtrekk.getEverId();

        mWebtrekk.setEverId(everID);

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        if (eidIsValid) {
            assertTrue(URL.contains(everID));
            assertFalse(URL.contains(oldEverID));
        }else {
            assertFalse(URL.contains(everID));
            assertTrue(URL.contains(oldEverID));
        }
        // check for event tracking
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                TrackingParameter pt = new TrackingParameter();
                pt.add(TrackingParameter.Parameter.ACTION_NAME, "test");
                mWebtrekk.track(pt);
            }
        });

        URL = waitForTrackedURL();

        if (eidIsValid) {
            assertTrue(URL.contains(everID));
            assertFalse(URL.contains(oldEverID));
        }else {
            assertFalse(URL.contains(everID));
            assertTrue(URL.contains(oldEverID));
        }
    }


    @Test
    public void testUserAgent() {
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();
        URLParsel parcel = new URLParsel();
        parcel.parseURL(URL);

        Context targetContext = getInstrumentation().getTargetContext();

        String version = Build.VERSION.RELEASE;

        assertEquals(HelperFunctions.urlDecode(parcel.getValue("X-WT-UA")),
                "Tracking Library " + Webtrekk.mTrackingLibraryVersionUI + " (Android "+version+"; unknown Android SDK built for x86; en_US)");
    }


    @Test
    public void testMediaCodeSet() {
        final String mediaCode = "mediaCode";

        // just ordinary track. no media code
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        assertFalse(URL.contains("&"+TrackingParameter.Parameter.ADVERTISEMENT+"="));

        // media code check
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.setMediaCode(mediaCode);
                mWebtrekk.track();
            }
        });

        URL = waitForTrackedURL();

        assertTrue(URL.contains("&"+TrackingParameter.Parameter.ADVERTISEMENT+"="+mediaCode));

        //next track - no media code
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        URL = waitForTrackedURL();

        assertFalse(URL.contains("&"+TrackingParameter.Parameter.ADVERTISEMENT+"="));
    }


    @Test
    public void testCustomPageOverride()
    {
        final String customPageName = "customPageName";

        //next track - no media code
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        assertTrue(URL.contains("Seite"));
        assertFalse(URL.contains(customPageName));

        //next track - no media code
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.setCustomPageName(customPageName);
                mWebtrekk.track();
            }
        });

        URL = waitForTrackedURL();

        assertFalse(URL.contains("Seite"));
        assertTrue(URL.contains(customPageName));

        onView(withId(R.id.page_example_activity_start)).perform(click());

        //next track - no media code
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        URL = waitForTrackedURL();

        assertTrue(URL.contains("Seite"));
        assertFalse(URL.contains(customPageName));
        pressBack();
    }

    @Test
    public void testDifferentParameterTrack()
    {
        final String s1 = "logged.in1";
        final String s2 = "logged.in2";
        final String search = "someSearch";
        final String customerID = "customerID";
        final String cat1 = "userCat1";
        final String cat2 = "userCat2";
        // Session parameter, internal search and custom visitor ID
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                TrackingParameter tp = new TrackingParameter();
                tp.add(TrackingParameter.Parameter.SESSION, "1", s1);
                tp.add(TrackingParameter.Parameter.SESSION, "2", s2);
                tp.add(TrackingParameter.Parameter.INTERN_SEARCH, search);
                tp.add(TrackingParameter.Parameter.CUSTOMER_ID, customerID);
                tp.add(TrackingParameter.Parameter.USER_CAT, "1", cat1);
                tp.add(TrackingParameter.Parameter.USER_CAT, "2", cat2);
                mWebtrekk.track(tp);
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        assertTrue(parcel.parseURL(URL));

        assertEquals(parcel.getValue("cs1"), s1);
        assertEquals(parcel.getValue("cs2"), s2);
        assertEquals(parcel.getValue("is"), search);
        assertEquals(parcel.getValue("cd"), customerID);
        assertEquals(parcel.getValue("uc1"), cat1);
        assertEquals(parcel.getValue("uc2"), cat2);
    }

    @Test
    public void testPageURL()
    {
        internalTestPU(HelperFunctions.urlEncode("http://www.yandex.ru"));

        String google = HelperFunctions.urlEncode("http://wwww.google.com");

        assertTrue(Webtrekk.getInstance().setPageURL("http://wwww.google.com"));

        internalTestPU(google);

        onView(withId(R.id.page_example_activity_start)).perform(click(longClick()));

        internalTestPU(null);

        pressBack();
    }

    private void internalTestPU(String valueToTest)
    {
        //test URL page in configuration xml
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        assertTrue(parcel.parseURL(URL));
        assertEquals(valueToTest, parcel.getValue("pu"));
    }

    @Test
    public void testAutoAdvertiserId() {

        if (isRestrictedMode()){
            return;
        }

        //test URL page in configuration xml
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        assertTrue(parcel.parseURL(URL));
        assertFalse(parcel.getValue("cb100").isEmpty());
        assertEquals("false", parcel.getValue("cb200"));
    }

    @Test
    public void testCustomValueIsSaved()
    {
        customValueIsSavedTestInternal();

        onView(withId(R.id.page_example_activity_start)).perform(click());

        //no any new activity start is influence on custom parameters
        customValueIsSavedTestInternal();

        pressBack();
    }

    private void customValueIsSavedTestInternal()
    {
        Webtrekk.getInstance().getCustomParameter().put("testCustomParameter", "customValue");

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });


        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("customValue", parcel.getValue("cp113"));
        assertEquals("customValue", parcel.getValue("cr"));
    }

/*
    public void testGoogleEmail()
    {
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertFalse(parcel.getValue("cp9").isEmpty());
    }
*/

    @Test
    public void testActionPageNamePriority()
    {
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                TrackingParameter pt = new TrackingParameter();
                pt.add(TrackingParameter.Parameter.ACTION_NAME, "test");
                mWebtrekk.track(pt);
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertTrue(parcel.getValue("p").contains("Seite"));

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                TrackingParameter pt = new TrackingParameter();
                pt.add(TrackingParameter.Parameter.ACTION_NAME, "test");
                mWebtrekk.setCustomPageName("CustomPage");
                mWebtrekk.track(pt);
            }
        });

        URL = waitForTrackedURL();

        parcel = new URLParsel();

        parcel.parseURL(URL);

        assertTrue(parcel.getValue("p").contains("CustomPage"));
    }
}