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
 * Created by Thomas Dahlmann on 16.12.15.
 */

package com.webtrekk.webtrekksdk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

import com.webtrekk.webtrekksdk.Modules.Campaign;
import com.webtrekk.webtrekksdk.Request.TrackingRequest;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfigurationXmlParser;
import com.webtrekk.webtrekksdk.Utils.ActivityListener;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationTests extends AndroidTestCase {
    private TrackingConfigurationXmlParser trackingConfigurationXmlParser;
    private Webtrekk webtrekk;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // android bug workaround: 308
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());

        trackingConfigurationXmlParser = new TrackingConfigurationXmlParser();
        webtrekk = Webtrekk.getInstance();
        webtrekk.initWebtrekk(getContext());

        Campaign.getFirstStartInitiated(getContext(), true);
        SharedPreferences.Editor editor = HelperFunctions.getWebTrekkSharedPreference(getContext()).edit();
        editor.putBoolean("CAMPAIGN_PROCESS_FINISHED", true).apply();
    }

    /**
     * test custom const global parameter from config to url
     */

    public void testGlobalConstParameter(){
        TrackingConfiguration config = null;

        (new File(getContext().getFilesDir(), "wt-pending-requests.json")).deleteOnExit();

        String configString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><webtrekkConfiguration><globalTrackingParameter><parameter id=\"PRODUCT\">test_product</parameter><ecomParameter><parameter id=\"1\">test_ecomparam1</parameter></ecomParameter></globalTrackingParameter></webtrekkConfiguration>";
        try {
            config = trackingConfigurationXmlParser.parse(configString);
            assertNotNull(config);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // first step is to make sure the product parameter is in the config
        assertTrue(config.getConstGlobalTrackingParameter().containsKey(TrackingParameter.Parameter.PRODUCT));
        //check the value
        assertEquals("test_product", config.getConstGlobalTrackingParameter().getDefaultParameter().get(Parameter.PRODUCT));
        // set the config in the webtrekk object
        webtrekk.setTrackingConfiguration(config);
        // test request creation
        TrackingRequest tr = webtrekk.getRequestFactory().createTrackingRequest(new TrackingParameter());
        assertTrue(tr.getTrackingParameter().getDefaultParameter().containsKey(Parameter.PRODUCT));
        String url = tr.getUrlString();
        assertTrue(url, url.contains("ba=test_product"));
        // now call track method
        ActivityListener lifecycleCallbacks = new ActivityListener(webtrekk);
        Activity activity = mock(Activity.class);

        when(activity.getResources()).thenReturn(mContext.getResources());

        lifecycleCallbacks.onActivityCreated(activity, null);
        lifecycleCallbacks.onActivityStarted(activity);
        //make sure the tracking request is created correct and the url is on the requesturlstore
        assertEquals(1, webtrekk.getRequestFactory().getRequestUrlStore().size());
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("ba=test_product"));

        // make sure the custom ecom parameter is available
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek(), webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("cb1=test_ecomparam1"));
    }

    /**
     * test custom mapped global parameter from config to url
     */

    public void testGlobalMappedParameter(){
        TrackingConfiguration config = null;
        // clear request store before
        webtrekk.getRequestFactory().getRequestUrlStore().clearAllTrackingData();

        String configString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><webtrekkConfiguration><globalTrackingParameter><parameter id=\"PRODUCT\">test_product</parameter><ecomParameter><parameter id=\"1\" key=\"example-key\"></parameter></ecomParameter></globalTrackingParameter></webtrekkConfiguration>";
        try {
            config = trackingConfigurationXmlParser.parse(configString);
            assertNotNull(config);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // first step is to make sure the product parameter is in the config
        assertTrue(config.getGlobalTrackingParameter().getEcomParameter().containsKey("1"));
        //check the value
        assertEquals("example-key", config.getGlobalTrackingParameter().getEcomParameter().get("1"));
        // set the config in the webtrekk object
        webtrekk.setTrackingConfiguration(config);
        webtrekk.getCustomParameter().put("example-key", "dynamic-value");
        // test request creation
        TrackingRequest tr = webtrekk.getRequestFactory().createTrackingRequest(new TrackingParameter());
        assertTrue(tr.getTrackingParameter().getDefaultParameter().containsKey(Parameter.PRODUCT));
        String url = tr.getUrlString();
        assertTrue(url, url.contains("cb1=dynamic-value"));

        // now call track method
        ActivityListener lifecycleCallbacks = new ActivityListener(webtrekk);
        Activity activity = mock(Activity.class);

        when(activity.getResources()).thenReturn(mContext.getResources());

        lifecycleCallbacks.onActivityCreated(activity, null);
        lifecycleCallbacks.onActivityStarted(activity);
        //make sure the tracking request is created correct and the url is on the requesturlstore
        assertEquals(1, webtrekk.getRequestFactory().getRequestUrlStore().size());
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek(), webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("cb1=dynamic-value"));
        // assert that the constant param is also set
        assertTrue(webtrekk.getRequestFactory().getRequestUrlStore().peek().contains("ba=test_product"));
        webtrekk.stopTracking();
    }
}
