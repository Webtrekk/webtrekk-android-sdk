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

import android.test.AndroidTestCase;
import android.util.Log;

import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.Configuration.ActivityConfiguration;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfigurationXmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


public class TrackingConfigurationXmlParserTest extends AndroidTestCase {

    private TrackingConfigurationXmlParser trackingConfigurationXmlParser;
    String invalidXmlConfiguration = "foo";
    String missingTagXmlConfiguration = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<configuration>\n" +
            "<trackdomain type=\"text\">http://q3.webtrekk.net</trackdomain>\n" +
            "<trackid type=\"text\">111111111111\n" +
            "\n" +
            "\n" +
            "    <screen>\n" +
            "        <classname type=\"text\">MainActivity</classname>\n" +
            "        <mappingname type=\"text\">Startseite</mappingname>\n" +
            "        <autotrack type=\"text\">true</autotrack>\n" +
            "    </screen>\n" +
            "\n" +
            "    <customvalues>\n" +
            "        <exampleval1>custom example val 1</exampleval1>\n" +
            "    </customvalues>\n" +
            "\n" +
            "</configuration>";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // android bug workaround: 308
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());

        trackingConfigurationXmlParser = new TrackingConfigurationXmlParser();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * test that the default value is used when an invalid sampling value is provided in the config
     */
    public void testSamplingOneUseDefault(){
        TrackingConfiguration config = null;

        String configString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><webtrekkConfiguration><sampling type=\"text\">1</sampling></webtrekkConfiguration>";
        try {
            config = trackingConfigurationXmlParser.parse(configString);
            assertNotNull(config);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(0, config.getSampling());
    }

    /**
     * test that the default value is used when an invalid send delay value is provided in the config
     */
    public void testSendDelayUseDefault(){
        TrackingConfiguration config = null;

        String configString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><webtrekkConfiguration><sendDelay type=\"text\">-1</sendDelay></webtrekkConfiguration>";
        try {
            config = trackingConfigurationXmlParser.parse(configString);
            assertNotNull(config);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("error: config= " + config, 300, config.getSendDelay());
    }

    /**
     * test that the default value is used when an invalid send delay value is provided in the config
     */
    public void testMaxRequestUseDefault(){
        TrackingConfiguration config = null;

        String configString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><webtrekkConfiguration><maxRequests type=\"text\">16</maxRequests></webtrekkConfiguration>";
        try {
            config = trackingConfigurationXmlParser.parse(configString);
            assertNotNull(config);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("error: config= " + config , 5000, config.getMaxRequests());
    }

    /**
     * test remove trailing slash from trackdomain
     */

    public void testTrailingSlash(){
        TrackingConfiguration config = null;

        String configString = "<?xml version=\"1.0\" encoding=\"utf-8\"?><webtrekkConfiguration><trackDomain type=\"text\">http://test.de/</trackDomain></webtrekkConfiguration>";
        try {
            config = trackingConfigurationXmlParser.parse(configString);
            assertNotNull(config);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("http://test.de", config.getTrackDomain());
    }



    public void testParseValidXmlGlobalSection() {
        TrackingConfiguration config = null;
        try {
            String trackingConfigurationString = HelperFunctions.stringFromStream(getContext().getResources().openRawResource(R.raw.webtrekk_config));
            config = trackingConfigurationXmlParser.parse(trackingConfigurationString);
        } catch (Exception e) {
            android.util.Log.d("WebtrekkSDK", "parsing error", e);
        }
        assertEquals(3, config.getVersion());
        assertEquals("http://trackingtest.nglab.org", config.getTrackDomain());
        assertEquals("1111111111112", config.getTrackId());
        assertEquals(0, config.getSampling());
        assertEquals(30, config.getSendDelay());
        assertEquals(4000, config.getMaxRequests());

        assertEquals(true, config.isAutoTracked());
        assertEquals(true, config.isAutoTrackAppUpdate());
        assertEquals(true, config.isAutoTrackAdvertiserId());
        assertEquals(true, config.isAutoTrackAppVersionName());
        assertEquals(true, config.isAutoTrackAppVersionCode());
        assertEquals(true, config.isAutoTrackAppPreInstalled());
        assertEquals(false, config.isAutoTrackPlaystoreUsername());
        assertEquals(true, config.isAutoTrackPlaystoreMail());
        assertEquals(true, config.isAutoTrackPlaystoreGivenName());
        assertEquals(true, config.isAutoTrackPlaystoreFamilyName());
        assertEquals(false, config.isAutoTrackApiLevel());
        assertEquals(true, config.isAutoTrackScreenorientation());
        assertEquals(true, config.isAutoTrackConnectionType());
        assertEquals(true, config.isAutoTrackAdvertismentOptOut());

        assertEquals(false, config.isEnableRemoteConfiguration());
        assertEquals("http://remotehost/tracking_config.xml", config.getTrackingConfigurationUrl());
        assertEquals(true, config.isAutoTrackRequestUrlStoreSize());
        assertEquals(30, config.getResendOnStartEventTime());

    }

    public void testParseValidXmlGlobalTrackingParameter() {
        TrackingConfiguration config = null;
        try {
            String trackingConfigurationString = HelperFunctions.stringFromStream(getContext().getResources().openRawResource(R.raw.webtrekk_config));
            config = trackingConfigurationXmlParser.parse(trackingConfigurationString);
        } catch (Exception e) {
            Log.d("WebtrekkSDK", "parsing error", e);
        }
        TrackingParameter tp = config.getConstGlobalTrackingParameter();
        assertNotNull(tp);
        assertEquals("test_product", tp.getDefaultParameter().get(Parameter.PRODUCT));
        assertEquals("443", tp.getDefaultParameter().get(Parameter.PRODUCT_COST));

        // check if all parameter maps have been initialized
        assertNotNull(tp.getPageParameter());
        assertNotNull(tp.getSessionParameter());
        assertNotNull(tp.getEcomParameter());
        assertNotNull(tp.getUserCategories());
        assertNotNull(tp.getPageCategories());
        assertNotNull(tp.getAdParameter());
        assertNotNull(tp.getActionParameter());
        assertNotNull(tp.getProductCategories());
        assertNotNull(tp.getMediaCategories());

        // check for correct single values
        assertEquals("test_pageparam1", tp.getPageParameter().get("1"));
        assertEquals("test_pageparam2", tp.getPageParameter().get("2"));
        assertEquals("test_pageparam3", tp.getPageParameter().get("3"));

        assertEquals("test_sessionparam1", tp.getSessionParameter().get("1"));
        assertEquals("test_ecomparam1", tp.getEcomParameter().get("1"));
        assertEquals("test_usercategory1", tp.getUserCategories().get("1"));
        assertEquals("test_pagecategory1", tp.getPageCategories().get("1"));
        assertEquals("test_adparam", tp.getAdParameter().get("1"));
        assertEquals("test_actionparam1", tp.getActionParameter().get("1"));
        assertEquals("test_productcategory1", tp.getProductCategories().get("1"));
        assertEquals("test_mediacategory1", tp.getMediaCategories().get("1"));



    }

    //@Suppress
    public void testParseValidXmlCustomParameters() {
        TrackingConfiguration config = null;
        try {
            String trackingConfigurationString = HelperFunctions.stringFromStream(getContext().getResources().openRawResource(R.raw.webtrekk_config));
            config = trackingConfigurationXmlParser.parse(trackingConfigurationString);
        } catch (Exception e) {
            android.util.Log.d("WebtrekkSDK", "parsing error", e);
        }
        assertNotNull(config.getCustomParameter());
        android.util.Log.d("WebtrekkSDK", "customParameter: " + config.getCustomParameter().toString());
        assertEquals(3, config.getCustomParameter().size());
        assertEquals("value1", config.getCustomParameter().get("test_customer_parameter_key1"));
        assertEquals("value2", config.getCustomParameter().get("test_customer_parameter_key2"));
        assertEquals("value3", config.getCustomParameter().get("test_customer_parameter_key3"));

    }
    public void testParseValidXmlActivityConfiguration() {
        TrackingConfiguration config = null;
        try {
            String trackingConfigurationString = HelperFunctions.stringFromStream(getContext().getResources().openRawResource(R.raw.webtrekk_config));
            config = trackingConfigurationXmlParser.parse(trackingConfigurationString);
        } catch (Exception e) {
            android.util.Log.d("WebtrekkSDK", "parsing error", e);
        }
        assertNotNull(config.getActivityConfigurations());
        assertEquals(2, config.getActivityConfigurations().size());
        assertTrue(config.getActivityConfigurations().containsKey("test.myapplication.MainActivity"));
        ActivityConfiguration act = config.getActivityConfigurations().get("test.myapplication.MainActivity");
        assertNotNull(act);
        assertEquals(act.getMappingName(), "Startseite");
        assertEquals(act.isAutoTrack(), true);
        TrackingParameter tp = act.getConstActivityTrackingParameter();

        // check if all parameter maps have been initialized
        assertNotNull(tp.getPageParameter());
        assertNotNull(tp.getSessionParameter());
        assertNotNull(tp.getEcomParameter());
        assertNotNull(tp.getUserCategories());
        assertNotNull(tp.getPageCategories());
        assertNotNull(tp.getAdParameter());
        assertNotNull(tp.getActionParameter());
        assertNotNull(tp.getProductCategories());
        assertNotNull(tp.getMediaCategories());

        // check for correct single values
        assertEquals("test_pageparam1", tp.getPageParameter().get("1"));
        assertEquals("test_pageparam2", tp.getPageParameter().get("2"));
        assertEquals("test_pageparam3", tp.getPageParameter().get("3"));

        assertEquals("test_sessionparam1", tp.getSessionParameter().get("1"));
        assertEquals("test_ecomparam1", tp.getEcomParameter().get("1"));
        assertEquals("test_usercategory1", tp.getUserCategories().get("1"));
        assertEquals("test_pagecategory1", tp.getPageCategories().get("1"));
        assertEquals("test_adparam", tp.getAdParameter().get("1"));
        assertEquals("test_actionparam1", tp.getActionParameter().get("1"));
        assertEquals("test_productcategory1", tp.getProductCategories().get("1"));
        assertEquals("test_mediacategory1", tp.getMediaCategories().get("1"));

        // check for the mapped values
        TrackingParameter mappedTp = act.getActivityTrackingParameter();
        assertEquals("mediacategory_from_app", mappedTp.getMediaCategories().get("1"));
    }




    public void testParseInvalidXML() {
        TrackingConfiguration config = null;
        try {
            String trackingConfigurationString = HelperFunctions.stringFromStream(getContext().getResources().openRawResource(R.raw.webtrekk_config));
            config = trackingConfigurationXmlParser.parse(invalidXmlConfiguration);
            fail("invalid xml configuration");
        }  catch (Exception e) {
        }

        try {
            String trackingConfigurationString = HelperFunctions.stringFromStream(getContext().getResources().openRawResource(R.raw.webtrekk_config));
            config = trackingConfigurationXmlParser.parse(missingTagXmlConfiguration);
            fail("invalid xml configuration");
        }  catch (Exception e) {
        }
    }

}
