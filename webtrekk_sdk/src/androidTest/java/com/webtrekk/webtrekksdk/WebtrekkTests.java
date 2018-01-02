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
 * Created by Thomas Dahlmann on 17.09.15.
 */


package com.webtrekk.webtrekksdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.test.AndroidTestCase;

import static org.mockito.Mockito.*;

import com.webtrekk.webtrekksdk.Modules.Campaign;
import com.webtrekk.webtrekksdk.Request.RequestFactory;
import com.webtrekk.webtrekksdk.Request.RequestUrlStore;
import com.webtrekk.webtrekksdk.Request.TrackingRequest;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.Configuration.ActivityConfiguration;
import com.webtrekk.webtrekksdk.Utils.ActivityListener;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;

import java.util.HashMap;


public class WebtrekkTests extends AndroidTestCase {

    Webtrekk webtrekk;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // android bug workaround: 308
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());

        webtrekk = new Webtrekk();
        SharedPreferences.Editor editor = HelperFunctions.getWebTrekkSharedPreference(getContext()).edit();
        editor.clear();
        editor.apply();

        Campaign.getFirstStartInitiated(getContext(), true);
        editor.putBoolean("CAMPAIGN_PROCESS_FINISHED", true).apply();
    }

    @Override
    protected void tearDown() throws Exception {
        RequestUrlStore request = new RequestUrlStore(getContext());
        request.deleteRequestsFile();
        super.tearDown();
    }

    public void testGetInstance() {
        Webtrekk webtrekk = Webtrekk.getInstance();
        assertNotNull(webtrekk);
        // make shure it always returns the same object
        assertSame(webtrekk, Webtrekk.getInstance());
    }

    public void testInitWebtrekk() {
        try {
            webtrekk.initWebtrekk(null);
            fail("null context, IllegalArgumentException");
        } catch (IllegalArgumentException e){

        }

        webtrekk.initWebtrekk(getContext());
        ActivityListener callbacks = new ActivityListener(webtrekk);
        assertNotNull(webtrekk.getTrackingConfiguration());
        assertNotNull(webtrekk.getContext());
        assertNotNull(webtrekk.getRequestFactory().getRequestUrlStore());
        assertNotNull(webtrekk.getRequestFactory().getWebtrekkParameter());
        assertEquals(0, callbacks.getCurrentActivitiesCount());
        // make shure it fails when init is called twice
//        try {
//            webtrekk.initWebtrekk(getContext());
//            fail("already initalized, IllegalStateException");
//        } catch (IllegalStateException e) {
//
//        }

    }

    public void testInitTrackingConfiguration() {
        webtrekk.setContext(getContext());
        webtrekk.initTrackingConfiguration(R.raw.webtrekk_config);
        assertNotNull(webtrekk.getTrackingConfiguration());
        assertEquals(webtrekk.getTrackingConfiguration().getTrackId(), "1111111111112");
        assertEquals(webtrekk.getTrackingConfiguration().getTrackDomain(), "http://trackingtest.nglab.org");
        assertEquals(webtrekk.getTrackingConfiguration().getSendDelay(), 30);
        assertEquals(webtrekk.getTrackingConfiguration().getMaxRequests(), 4000);
        assertEquals(webtrekk.getTrackingConfiguration().getVersion(), 3);

    }
    public void testInitWebtrekkParameter() {
        // make sure the default params have valid values
        webtrekk.setContext(getContext());
        webtrekk.initTrackingConfiguration(R.raw.webtrekk_config);
        webtrekk.getRequestFactory().init(mContext, webtrekk.getTrackingConfiguration(), webtrekk);
        assertEquals(6, webtrekk.getRequestFactory().getWebtrekkParameter().size());
    }

    public void testUpdateDynamicParameter() {
        // make sure that the values which change with every request are inserted as well
        webtrekk.setContext(getContext());
        webtrekk.initTrackingConfiguration(R.raw.webtrekk_config);
        webtrekk.getRequestFactory().init(mContext, webtrekk.getTrackingConfiguration(), webtrekk);

        RequestUrlStore requestUrlStore = mock(RequestUrlStore.class);
        webtrekk.getRequestFactory().setRequestUrlStore(requestUrlStore);
        when(requestUrlStore.size()).thenReturn(55);

        //set some different default values
        webtrekk.getCustomParameter().put("screenOrientation", "stttr");
        webtrekk.getCustomParameter().put("connectionType", "offline");

        webtrekk.getRequestFactory().updateDynamicParameter();
        assertEquals(7, webtrekk.getRequestFactory().getWebtrekkParameter().size());
        assertTrue(webtrekk.getRequestFactory().getAutoCustomParameter().get("screenOrientation").matches("(portrait|landscape)"));
        String connectionType = webtrekk.getCustomParameter().get("connectionType");
        assertTrue(connectionType.equals("WIFI") || connectionType.equals("offline"));
        assertEquals("55", webtrekk.getRequestFactory().getAutoCustomParameter().get("requestUrlStoreSize"));
    }


    public void testInitCustomParameter() {
        // make sure that the values which change with every request are inserted as well
        webtrekk.setContext(getContext());
        webtrekk.initTrackingConfiguration(R.raw.webtrekk_config);
        webtrekk.getRequestFactory().init(mContext, webtrekk.getTrackingConfiguration(), webtrekk);

        assertTrue(webtrekk.getRequestFactory().getAutoCustomParameter().toString(), webtrekk.getRequestFactory().getAutoCustomParameter().size()>0);
    }

    public void testTrack() {
//        try {
//            webtrekk.track();
//            fail("not initalized, IllegalStateException");
//        } catch (IllegalStateException e) {
//
//        }
        webtrekk.initWebtrekk(getContext());
//        try {
//            webtrekk.track();
//            fail("startActivity not called, IllegalStateException");
//        } catch (IllegalStateException e) {
//
//        }
//        try {
//            webtrekk.track(null);
//            fail("trackingparams is null, IllegalStateException");
//        } catch (IllegalStateException e) {
//
//        }
        ActivityListener lifecycleCallbacks = new ActivityListener(webtrekk);

        SecondActivity activity = mock(SecondActivity.class);

        when(activity.getResources()).thenReturn(mContext.getResources());
        lifecycleCallbacks.onActivityCreated(activity, null);
        lifecycleCallbacks.onActivityStarted(activity);
        assertEquals(1, webtrekk.getRequestFactory().getRequestUrlStore().size());
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("SecondActivity_Proxy,"));
    }



    public static class SecondActivity extends Activity{};

    public void testStartStopActivity() {

        ActivityListener callbacks = new ActivityListener(webtrekk);
        webtrekk.initWebtrekk(getContext());

        Activity activity = mock(Activity.class);
        SecondActivity secondActivity = mock(SecondActivity.class);

        when(activity.getResources()).thenReturn(mContext.getResources());
        when(secondActivity.getResources()).thenReturn(mContext.getResources());

        callbacks.onActivityStarted(activity);

        assertEquals("Activity_Proxy", webtrekk.getCurrentActivityName());
        assertEquals(1, callbacks.getCurrentActivitiesCount());

        // second call
        callbacks.onActivityCreated(secondActivity, null);
        callbacks.onActivityStarted(secondActivity);
        callbacks.onActivityStopped(activity);
        assertEquals(2, callbacks.getCurrentActivitiesCount());
        callbacks.onActivityStopped(secondActivity);
        assertEquals(2, callbacks.getCurrentActivitiesCount());
        when(secondActivity.isFinishing()).thenReturn(true);
        callbacks.onActivityStopped(secondActivity);
        assertEquals(1, callbacks.getCurrentActivitiesCount());
    }

    public void testOnSendIntervalOver() {
        webtrekk.initWebtrekk(getContext());
        ActivityListener callbacks = new ActivityListener(webtrekk);
        RequestUrlStore requestUrlStore = mock(RequestUrlStore.class);
        webtrekk.getRequestFactory().setRequestUrlStore(requestUrlStore);
        webtrekk.getRequestFactory().onSendIntervalOver();

        when(requestUrlStore.size()).thenReturn(5).thenReturn(4).thenReturn(3).thenReturn(2).thenReturn(1).thenReturn(0);
        webtrekk.getRequestFactory().onSendIntervalOver();
    }

    public void testSetOptOut() {
        webtrekk.initWebtrekk(getContext());
        SharedPreferences preferences = getContext().getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        webtrekk.setOptout(true);
        assertTrue(webtrekk.isOptout());
        assertTrue(preferences.getBoolean(RequestFactory.PREFERENCE_KEY_OPTED_OUT, false));

        webtrekk.setOptout(false);
        assertFalse(preferences.getBoolean(RequestFactory.PREFERENCE_KEY_OPTED_OUT, true));
        assertFalse(webtrekk.isOptout());

    }

    public void testInitSamplingNull() {
        webtrekk.initWebtrekk(getContext());
        SharedPreferences preferences = getContext().getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        webtrekk.getTrackingConfiguration().setSampling(0);
        webtrekk.getRequestFactory().init(mContext, webtrekk.getTrackingConfiguration(), webtrekk);
        assertFalse(webtrekk.isSampling());
        assertFalse(preferences.getBoolean(RequestFactory.PREFERENCE_KEY_IS_SAMPLING, true));
        assertEquals(0, preferences.getInt(RequestFactory.PREFERENCE_KEY_SAMPLING, -1));
    }

    public void testInitSamplingNotNull() {
        webtrekk.initWebtrekk(getContext());
        SharedPreferences preferences = getContext().getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);

        webtrekk.getTrackingConfiguration().setSampling(10);
        webtrekk.getRequestFactory().init(mContext, webtrekk.getTrackingConfiguration(), webtrekk);
        assertEquals(10, preferences.getInt(RequestFactory.PREFERENCE_KEY_SAMPLING, -1));
        // also make sure the value is reinitalized after change
        webtrekk.getTrackingConfiguration().setSampling(20);
        webtrekk.getRequestFactory().init(mContext, webtrekk.getTrackingConfiguration(), webtrekk);
        assertEquals(20, preferences.getInt(RequestFactory.PREFERENCE_KEY_SAMPLING, -1));
    }

    public void testFnsParameter() {
        // make shure fns gets send once and only once when a new session starts
        webtrekk.initWebtrekk(getContext());
        SharedPreferences preferences = getContext().getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        ActivityListener lifecycleCallbacks = new ActivityListener(webtrekk);
        Activity activity = mock(Activity.class);

        when(activity.getResources()).thenReturn(mContext.getResources());

        lifecycleCallbacks.onActivityCreated(activity, null);
        lifecycleCallbacks.onActivityStarted(activity);
        //assertEquals(webtrekk.getRequestUrlStore().get(0), "test");
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("&fns=1"));
        webtrekk.track();
        assertEquals(webtrekk.getRequestFactory().getInternalParameter().getDefaultParameter().get(Parameter.FORCE_NEW_SESSION), "0");
        webtrekk.getRequestFactory().getRequestUrlStore().removeLastURL();
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("&fns=0"));
    }

    public void testFirstParameter() {
        // make shure one gets send once and only once when a new session starts
        SharedPreferences preferences = getContext().getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        preferences.edit().remove(Webtrekk.PREFERENCE_KEY_EVER_ID).commit();
        webtrekk.initWebtrekk(getContext());
        ActivityListener lifecycleCallbacks = new ActivityListener(webtrekk);

        Activity activity = mock(Activity.class);

        when(activity.getResources()).thenReturn(mContext.getResources());


        lifecycleCallbacks.onActivityCreated(activity, null);
        lifecycleCallbacks.onActivityStarted(activity);
        //assertEquals(webtrekk.getRequestUrlStore().get(0), "test");
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("&one=1"));

        //webtrekk.initInternalParameter();
        webtrekk.track();
        assertEquals(webtrekk.getRequestFactory().getInternalParameter().getDefaultParameter().get(Parameter.APP_FIRST_START), "0");
        webtrekk.getRequestFactory().getRequestUrlStore().removeLastURL();
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("&one=0"));
    }

    public void testUpdated() {
        webtrekk.initWebtrekk(getContext());
        assertEquals(webtrekk.getRequestFactory().getAutoCustomParameter().get("appUpdated"), "1");
        SharedPreferences preferences = getContext().getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        preferences.edit().remove(Webtrekk.PREFERENCE_APP_VERSIONCODE).commit();
        assertEquals(preferences.getInt(Webtrekk.PREFERENCE_APP_VERSIONCODE, -1), -1);
        ActivityListener lifecycleCallbacks = new ActivityListener(webtrekk);
        Activity activity = mock(Activity.class);

        when(activity.getResources()).thenReturn(mContext.getResources());
        lifecycleCallbacks.onActivityCreated(activity, null);
        lifecycleCallbacks.onActivityStarted(activity);
        webtrekk.track();
        assertEquals(HelperFunctions.getAppVersionCode(getContext()), 0);
        assertEquals(webtrekk.getRequestFactory().getAutoCustomParameter().get("appUpdated"), "0");

        assertEquals(HelperFunctions.updated(getContext(), 5), true);
        assertEquals(preferences.getInt(Webtrekk.PREFERENCE_APP_VERSIONCODE, 0), 5);
    }

    public void testCreateTrackingRequest() {
        TrackingParameter globalTp = new TrackingParameter();
        globalTp.add(Parameter.ACTIVITY_NAME, "testtestact");
        webtrekk.initWebtrekk(getContext());
        TrackingParameter tp = new TrackingParameter();
        webtrekk.setGlobalTrackingParameter(globalTp);
        TrackingRequest tr = webtrekk.getRequestFactory().createTrackingRequest(tp);
        ActivityListener lifecycleCallbacks = new ActivityListener(webtrekk);

        Activity activity = mock(Activity.class);

        when(activity.getResources()).thenReturn(mContext.getResources());
        lifecycleCallbacks.onActivityCreated(activity, null);
        lifecycleCallbacks.onActivityStarted(activity);
        //verify override
        assertEquals(webtrekk.getRequestFactory().getGlobalTrackingParameter().getDefaultParameter().get(Parameter.ACTIVITY_NAME), "testtestact");

    }

    /**
     * verify that when a global tracking parameter was set, that it appears in the url
     */
    public void testGlobalTrackingParameter() {
        // set a global tracking param
        TrackingParameter constGlobalTp = new TrackingParameter();
        constGlobalTp.add(Parameter.ECOM, "4", "testecomparam");
        webtrekk.initWebtrekk(getContext());
        TrackingParameter tp = new TrackingParameter();
        webtrekk.setConstGlobalTrackingParameter(constGlobalTp);
        TrackingRequest tr = webtrekk.getRequestFactory().createTrackingRequest(tp);
        //assertTrue(tp.getEcomParameter().get("1").contains("testecomparam"));
        assertTrue("url string does not contain value: " + tr.getUrlString(), tr.getUrlString().contains("&cb4=testecomparam"));
    }

    /**
     * make sure that an activity only gets tracked when auto tracking is enabled globally or for the specific application
     */
    public void testAutoTrackSettings() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return;
        }

        TrackingConfiguration configuration = new TrackingConfiguration();
        // auto tracking globally enabled
        configuration.setAutoTracked(true);

        // auto tracking true
        ActivityConfiguration act1 = new ActivityConfiguration("act1", "mapping.act1", true, new TrackingParameter(), new TrackingParameter());
        // auto tracking false just for this activity
        ActivityConfiguration act2 = new ActivityConfiguration("act2", "mapping.act2", false, new TrackingParameter(), new TrackingParameter());
        HashMap<String, ActivityConfiguration> actConfigurations = new HashMap<>();
        actConfigurations.put("act1", act1);
        actConfigurations.put("act2", act2);
        configuration.setActivityConfigurations(actConfigurations);

        webtrekk.setTrackingConfiguration(configuration);

        Webtrekk webtrekkSpy = spy(webtrekk);
        doNothing().when(webtrekkSpy).track();

        webtrekkSpy.setCurrentActivityName("act1");
        webtrekkSpy.autoTrackActivity();

        verify(webtrekkSpy, times(1)).track();

        // make sure track is not called when the activity overrides the global autotracking settings
        // so times is still 1
        webtrekkSpy.setCurrentActivityName("act2");
        webtrekkSpy.autoTrackActivity();

        verify(webtrekkSpy, times(1)).track();

    }

}
