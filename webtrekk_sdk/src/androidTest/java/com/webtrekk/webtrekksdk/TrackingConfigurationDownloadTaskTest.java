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
 * Created by Thomas Dahlmann on 02.11.15.
 */

package com.webtrekk.webtrekksdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.webtrekk.webtrekksdk.Utils.AsyncTest;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfigurationDownloadTask;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class TrackingConfigurationDownloadTaskTest extends InstrumentationTestCase implements AsyncTest {
    private static boolean called;
    Webtrekk webtrekk;
    private SynchronizedWaiter synchronizedWaiter;

    protected void setUp() throws Exception {
        super.setUp();
        // android bug workaround: 308
        System.setProperty("dexmaker.dexcache", getInstrumentation().getContext().getCacheDir().toString());
        called = false;
        webtrekk = new Webtrekk();
        webtrekk.initWebtrekk(getInstrumentation().getContext());

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * make sure that it never throws an exception or overrides the config with null,
     * in case of a network error it uses the local one
     * @throws Throwable
     */
    public void testTrackingConfigurationDownloadTaskNetworkError() throws Throwable {
        synchronizedWaiter = new SynchronizedWaiter();
        AsyncTest asyncTest = this;
        Log.d("TEST", "task started");
        TrackingConfigurationDownloadTask task = new TrackingConfigurationDownloadTask(webtrekk, asyncTest);
        task = spy(task);
        try {
            task.execute(webtrekk.getTrackingConfiguration().getTrackingConfigurationUrl());

        } catch (Exception e) {
            fail("should never throw an exception, just use the local configuration");
        }
        synchronizedWaiter.doWait();
        assertNotNull(webtrekk.getTrackingConfiguration());
    }
    /**
     * make sure that it never throws an exception or overrides the config with null,
     * in case of an invalid config it uses the local one
     * @throws Throwable
     */
    public void testTrackingConfigurationDownloadTaskInvalidConfig() throws Throwable {
        synchronizedWaiter = new SynchronizedWaiter();
        AsyncTest asyncTest = this;
        Log.d("TEST", "task started");
        TrackingConfigurationDownloadTask task = new TrackingConfigurationDownloadTask(webtrekk, asyncTest);
        task = spy(task);
        doReturn("foo").when(task).getXmlFromUrl("http://foourl.de/config.xml");
        try {
            task.execute("http://nglab.org/config.xml");

        } catch (Exception e) {
            fail("should never throw an exception, just use the local configuration");
        }
        synchronizedWaiter.doWait();
        assertNotNull(webtrekk.getTrackingConfiguration());
    }

    /**
     * make sure that it never throws an exception or overrides the config with null,
     * in case of an invalid config it uses the local one
     * @throws Throwable
     */
    public void testTrackingConfigurationDownloadTaskSameConfigVersion() throws Throwable {
        synchronizedWaiter = new SynchronizedWaiter();
        AsyncTest asyncTest = this;
        Log.d("TEST", "task started");
        TrackingConfigurationDownloadTask task = new TrackingConfigurationDownloadTask(webtrekk, asyncTest);
        assertNotNull(webtrekk.getTrackingConfiguration());
        task = spy(task);
        webtrekk = spy(webtrekk);
        String config = "<webtrekkConfiguration>\n" +
                "    <!--the version number for this configuration file -->\n" +
                "    <version>2</version><trackDomain>http://trackingtest.nglab.org</trackDomain><trackId>12345</trackId></webtrekkConfiguration>";
        doReturn(config).when(task).getXmlFromUrl(anyString());
        try {
            task.execute("http://nglab.org/config.xml");

        } catch (Exception e) {
            fail("should never throw an exception, just use the local configuration");
        }
        synchronizedWaiter.doWait();

        // make sure that no new tracking configuration is set
        //verify(webtrekk, times(0)).setTrackingConfiguration((TrackingConfiguration)any());
        assertEquals("1111111111112", webtrekk.getTrackingIDs().get(0));
        assertEquals("http://trackingtest.nglab.org", webtrekk.getTrackDomain());
    }

    /**
     * make sure that it never throws an exception or overrides the config with null,
     * in case of an invalid config it uses the local one
     * @throws Throwable
     */
    public void testTrackingConfigurationDownloadTaskNewConfigVersion() throws Throwable {
        SharedPreferences preferences = getInstrumentation().getContext().getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        preferences.edit().remove(Webtrekk.PREFERENCE_KEY_EVER_ID).commit();
        synchronizedWaiter = new SynchronizedWaiter();
        AsyncTest asyncTest = this;
        String config = "<webtrekkConfiguration>\n" +
                "    <!--the version number for this configuration file -->\n" +
                "    <version>3</version></webtrekkConfiguration>";

        TrackingConfigurationDownloadTask task = spy(new TrackingConfigurationDownloadTask(webtrekk, asyncTest));
        doReturn(config).when(task).getXmlFromUrl(anyString());
        //when(task.getXmlFromUrl(anyString())).thenReturn(config);
                assertNotNull(webtrekk.getTrackingConfiguration());
        // make sure it uses config version 2 before
       // assertEquals(2, webtrekk.getTrackingConfiguration().getVersion());
        //webtrekk = spy(webtrekk);

        //final HttpURLConnection mockURLConnection = mock(HttpURLConnection.class);
        //when(mockURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream(config.getBytes("UTF-8")));
        //doNothing().when(mockURLConnection).connect();
        //task.setConn(mockURLConnection);


        try {
            task.execute("https://supportcenter.webtrekk.com/android_config.xml");

        } catch (Exception e) {
            fail("should never throw an exception, just use the local configuration");
        }
        synchronizedWaiter.doWait();
        // make sure the current tracking configuration has been set and has the new version number
        //verify(webtrekk, times(1)).setTrackingConfiguration((TrackingConfiguration)any());
        assertEquals(3, webtrekk.getTrackingConfiguration().getVersion());
    }

    @Override
    public void workDone() {
        synchronizedWaiter.doNotify();

    }
}
