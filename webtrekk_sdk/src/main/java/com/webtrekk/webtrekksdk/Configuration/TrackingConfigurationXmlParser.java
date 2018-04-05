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

package com.webtrekk.webtrekksdk.Configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

/**
 * parses the xml for the tracking configuration
 */
public class TrackingConfigurationXmlParser {

    interface ParameterAction{
        <T> void process(TrackingConfiguration config, XmlPullParser parser, T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException ;
    }

    enum ParType{
        VERSION(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage)  throws XmlPullParserException, IOException  {
                Integer version = (Integer) value;

                if (version > 0) {
                    config.setVersion(version);

                }
            }
        }, Integer.class),

        TRACK_DOMAIN(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException {
                String trackDomain = (String) value;

                if (trackDomain.endsWith("/")) {
                    trackDomain = trackDomain.substring(0, trackDomain.length() - 1);
                }
                config.setTrackDomain(trackDomain);
            }
        }, String.class),

        TRACK_ID(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                String trackId = (String) value;

                config.setTrackId(trackId.replace(" ", ""));
            }
        }, String.class),

        SAMPLING(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Integer sampling = (Integer) value;

                if (sampling != 1 && sampling >= 0) {
                    config.setSampling(sampling);
                }
            }
        }, Integer.class),

        MAX_REQUEST(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Integer maxRequests = (Integer) value;

                if (maxRequests > 99) {
                    config.setMaxRequests(maxRequests);
                } else {
                    WebtrekkLogging.log(errorMessage);
                }
            }
        }, Integer.class),

        SEND_DELAY(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Integer sendDelay = (Integer) value;

                if (sendDelay >= 0) {
                    config.setSendDelay(sendDelay);
                } else {
                    WebtrekkLogging.log(errorMessage);
                }
            }
        }, Integer.class),

        AUTO_TRACKED(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean autoTracked = (Boolean) value;

                config.setAutoTracked(autoTracked);
            }
        }, Boolean.class),

        AUTO_TRACK_UPDATE(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean autoTrackUpdate = (Boolean) value;

                config.setAutoTrackAppUpdate(autoTrackUpdate);
            }
        }, Boolean.class),

        AUTO_TRACK_ADD_CLEAR_ID(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean addClearId = (Boolean) value;

                config.setAutoTrackAdClearId(addClearId);
            }
        }, Boolean.class),

        AUTO_TRACK_ADDVERTISER_ID(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean addVerId = (Boolean) value;

                config.setAutoTrackAdvertiserId(addVerId);
            }
        }, Boolean.class),

        AUTO_TRACK_APP_VERSION_NAME(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackAppVersionName(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_APP_VERSION_CODE(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackAppVersionCode(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_APP_PRE_INSTALLED(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackAppPreInstalled(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_APP_PLAY_STORE_USER_NAME(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackPlaystoreUsername(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_APP_PLAY_STORE_MAIL(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackPlaystoreMail(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_APP_PLAY_STORE_GIVEN_NAME(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackPlaystoreGivenName(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_APP_PLAY_STORE_FAMILY_NAME(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackPlaystoreFamilyName(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_API_LEVEL(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackApiLevel(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_SCREEN_ORIENTATION(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackScreenorientation(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_CONNECTION_TYPE(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackConnectionType(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_ADDVERISEMENT_OPT_OUT(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackAdvertismentOptOut(boolValue);
            }
        }, Boolean.class),

        AUTO_TRACK_REQUEST_URL_STORE_SIZE(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setAutoTrackRequestUrlStoreSize(boolValue);
            }
        }, Boolean.class),

        ENABLE_REMOTE_CONFIGURATION(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setEnableRemoteConfiguration(boolValue);
            }
        }, Boolean.class),

        TRACKING_CONFIGURATION_URL(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException {
                String url = (String) value;

                config.setTrackingConfigurationUrl(url);
            }
        }, String.class),

        RESEND_ON_START_EVENT_TIME(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Integer resendOnStartEventTime = (Integer) value;

                config.setResendOnStartEventTime(resendOnStartEventTime);
            }
        }, Integer.class),

        ERROR_LOG_ENABLED(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean boolValue = (Boolean) value;

                config.setErrorLogEnable(boolValue);
            }
        }, Boolean.class),

        ERROR_LOG_LEVEL(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException {
                Integer level = (Integer) value;

                if (level >= 1 && level <= 3) {
                    config.setErrorLogLevel(level);
                }
            }
        }, Integer.class),

        ENABLE_CAMPAIGN_TRACKING(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @NonNull T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Boolean tracking = (Boolean) value;

                config.setEnableCampaignTracking(tracking);
            }
        }, Boolean.class),

        CUSTOM_PARAMETER(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @Nullable T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Map<String, String> customParameter = new HashMap<>();
                confParser.setCustomParameterConfigurationFromXml(parser, customParameter);
                config.setCustomParameter(customParameter);
                WebtrekkLogging.log("customParameter read from xml");
            }
        }),

        GLOBAL_TRACKING_PARAMETER(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @Nullable T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                TrackingParameter tp = new TrackingParameter();
                TrackingParameter constTp = new TrackingParameter();
                confParser.setTrackingParameterFromXml(parser, tp, constTp);
                config.setGlobalTrackingParameter(tp);
                config.setConstGlobalTrackingParameter(constTp);
                WebtrekkLogging.log("globalTrackingParameter read from xml");
            }
        }),

        RECOMMENDATIONS(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @Nullable T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                Map<String, String> recConfig = confParser.readRecommendationConfig(parser);
                config.setRecommendationConfiguration(recConfig);
            }
        }),

        ACTIVITY_SCREEN(new ParameterAction(){
            @Override
            public <T> void process(TrackingConfiguration config, XmlPullParser parser, @Nullable T value, TrackingConfigurationXmlParser confParser, String errorMessage) throws XmlPullParserException, IOException  {
                ActivityConfiguration act = confParser.readActivityConfiguration(parser, config.isAutoTracked());
                config.getActivityConfigurations().put(act.getClassName(), act);
                WebtrekkLogging.log("activity read from xml: "+ act.getClassName());
            }
        });

        @Nullable
        private final Class<?> mType;
        private final ParameterAction mAction;

        ParType(ParameterAction action, @Nullable Class<?> type){
            mType = type;
            mAction = action;
        }

        ParType(ParameterAction action){
            this(action, null);
        }

        ParameterAction getAction(){
            return mAction;
        }

        Class<?> getType(){
            return mType;
        }
    }

    private static final String ns = null;

    /**
     * parses the TrackingConfiguration from XML String
     *
     * @param in the String containing the xml configuration
     * @return returns a TrackingConfiguration object
     * @throws XmlPullParserException
     * @throws IOException
     */
    public TrackingConfiguration parse(String in) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(new StringReader(in));
        parser.nextTag();
        return readConfig(parser);
    }

    /**
     * reads the tags and sets the related configuration values in the TrackingConfiguration object
     *
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private TrackingConfiguration readConfig(XmlPullParser parser) throws XmlPullParserException, IOException {
        TrackingConfiguration config = new TrackingConfiguration();
        parser.require(XmlPullParser.START_TAG, ns, "webtrekkConfiguration");

        final Map<String,ParType> actionMap = new HashMap<>();

        actionMap.put("version", ParType.VERSION);
        actionMap.put("trackDomain", ParType.TRACK_DOMAIN);
        actionMap.put("trackId", ParType.TRACK_ID);
        actionMap.put("sampling", ParType.SAMPLING);
        actionMap.put("maxRequests", ParType.MAX_REQUEST);
        actionMap.put("sendDelay", ParType.SEND_DELAY);
        actionMap.put("autoTracked", ParType.AUTO_TRACKED);
        actionMap.put("autoTrackAppUpdate", ParType.AUTO_TRACK_UPDATE);
        actionMap.put("autoTrackAdClearId", ParType.AUTO_TRACK_ADD_CLEAR_ID);
        actionMap.put("autoTrackAdvertiserId", ParType.AUTO_TRACK_ADDVERTISER_ID);
        actionMap.put("autoTrackAppVersionName", ParType.AUTO_TRACK_APP_VERSION_NAME);
        actionMap.put("autoTrackAppVersionCode", ParType.AUTO_TRACK_APP_VERSION_CODE);
        actionMap.put("autoTrackAppPreInstalled", ParType.AUTO_TRACK_APP_PRE_INSTALLED);
        actionMap.put("autoTrackPlaystoreUsername", ParType.AUTO_TRACK_APP_PLAY_STORE_USER_NAME);
        actionMap.put("autoTrackPlaystoreMail", ParType.AUTO_TRACK_APP_PLAY_STORE_MAIL);
        actionMap.put("autoTrackPlaystoreGivenName", ParType.AUTO_TRACK_APP_PLAY_STORE_GIVEN_NAME);
        actionMap.put("autoTrackPlaystoreFamilyName", ParType.AUTO_TRACK_APP_PLAY_STORE_FAMILY_NAME);
        actionMap.put("autoTrackApiLevel", ParType.AUTO_TRACK_API_LEVEL);
        actionMap.put("autoTrackScreenOrientation", ParType.AUTO_TRACK_SCREEN_ORIENTATION);
        actionMap.put("autoTrackConnectionType", ParType.AUTO_TRACK_CONNECTION_TYPE);
        actionMap.put("autoTrackAdvertisementOptOut", ParType.AUTO_TRACK_ADDVERISEMENT_OPT_OUT);
        actionMap.put("autoTrackRequestUrlStoreSize", ParType.AUTO_TRACK_REQUEST_URL_STORE_SIZE);
        actionMap.put("enableRemoteConfiguration", ParType.ENABLE_REMOTE_CONFIGURATION);
        actionMap.put("trackingConfigurationUrl", ParType.TRACKING_CONFIGURATION_URL);
        actionMap.put("resendOnStartEventTime", ParType.RESEND_ON_START_EVENT_TIME);
        actionMap.put("errorLogEnable", ParType.ERROR_LOG_ENABLED);
        actionMap.put("errorLogLevel", ParType.ERROR_LOG_LEVEL);
        actionMap.put("enableCampaignTracking", ParType.ENABLE_CAMPAIGN_TRACKING);
        actionMap.put("customParameter", ParType.CUSTOM_PARAMETER);
        actionMap.put("globalTrackingParameter", ParType.GLOBAL_TRACKING_PARAMETER);
        actionMap.put("recommendations", ParType.RECOMMENDATIONS);
        actionMap.put("activity", ParType.ACTIVITY_SCREEN);
        actionMap.put("screen", ParType.ACTIVITY_SCREEN);


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;

            }
            final String name = parser.getName();
            final String errorMessage = "invalid "+name+" value, using default";

            if (name.equals("webtrekkConfiguration")) {
                WebtrekkLogging.log("premature end of configuration");
                break;
            } else {
                ParType parType = actionMap.get(name);

                if (parType != null){
                    parser.require(XmlPullParser.START_TAG, ns, name);

                    Object objValue = null;

                    if (parType.getType() != null) {

                        final String strValue = readText(parser);

                        try {
                            if (parType.getType() == Integer.class) {
                                objValue = Integer.parseInt(strValue);
                            } else if (parType.getType() == String.class) {
                                objValue = strValue;
                            } else if (parType.getType() == Boolean.class) {
                                if (strValue.equals("true")) {
                                    objValue = Boolean.valueOf(true);
                                } else if (strValue.equals("false")) {
                                    objValue = Boolean.valueOf(false);
                                } else {
                                    WebtrekkLogging.log(errorMessage);
                                }
                            }
                        } catch (Exception ex) {
                            WebtrekkLogging.log(errorMessage + ": ", ex);
                        }
                    }

                    // don't call process if objValue == null, but shouldn't
                    if (parType.getType() == null || (parType.getType() != null && objValue != null)) {
                        ParameterAction action = parType.getAction();

                        action.process(config, parser, objValue, this, errorMessage);
                    }

                    parser.require(XmlPullParser.END_TAG, ns, name);

                }else{
                    WebtrekkLogging.log("unknown xml tag: " + name);
                    skip(parser);
                }
            }
        }

        WebtrekkLogging.log("configuration read from xml");

        return config;
    }

    /**
     * ActivityConfigurations are in their own XML Tag, this function reads and parses the Activity Configuration
     *
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private ActivityConfiguration readActivityConfiguration(XmlPullParser parser, boolean globalAutoTracked) throws XmlPullParserException, IOException {

        String className;
        String mappingName;
        boolean isAutoTrack = globalAutoTracked;

        ActivityConfiguration activityConfiguration = new ActivityConfiguration();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("classname")) {
                parser.require(XmlPullParser.START_TAG, ns, "classname");
                className = readText(parser);
                activityConfiguration.setClassName(className);
            } else if (name.equals("mappingname")) {
                parser.require(XmlPullParser.START_TAG, ns, "mappingname");
                mappingName = readText(parser);
                activityConfiguration.setMappingName(mappingName);
            } else if (name.equals("autoTracked")) {
                parser.require(XmlPullParser.START_TAG, ns, "autoTracked");
                String autoTracked = readText(parser);
                // the default value for the activities is based on the global setting
                // when global autotracking is enabled, all activities have true by default
                // when its disabled globally, all activities have false by default
                // each activity can override this value with its own
                if (autoTracked.equals("true")) {
                    isAutoTrack = true;
                }
                if (autoTracked.equals("false")) {
                    isAutoTrack = false;
                }

            } else if (name.equals("activityTrackingParameter") || name.equals("screenTrackingParameter")) {
                parser.require(XmlPullParser.START_TAG, ns, name);
                TrackingParameter tp = new TrackingParameter();
                TrackingParameter constTp = new TrackingParameter();
                setTrackingParameterFromXml(parser, tp, constTp);
                activityConfiguration.setActivityTrackingParameter(tp);
                activityConfiguration.setConstActivityTrackingParameter(constTp);
                parser.require(XmlPullParser.END_TAG, ns, name);
            } else {
                WebtrekkLogging.log("activity: unknown xml tag: " + name);
                skip(parser);
            }
        }
        activityConfiguration.setIsAutoTrack(isAutoTrack);
        return activityConfiguration;
    }

    /**
     * this method is used to parse the global and the activites tracking parameter objects
     * they override the ones set in the code, and allow to set params for all activities or global ones for all requests
     * directly in the xml file
     *
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void setTrackingParameterFromXml(XmlPullParser parser, TrackingParameter tp, TrackingParameter constTp) throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("parameter")) {
                parser.require(XmlPullParser.START_TAG, ns, "parameter");
                String id = parser.getAttributeValue(ns, "id");
                //String value = parser.getAttributeValue(ns, "value");
                String key = parser.getAttributeValue(ns, "key");
                String value = readText(parser);
                if (id == null || (value == null && key == null)) {
                    WebtrekkLogging.log("invalid parameter configuration while reading customParameter, missing key or value");
                } else {
                    Parameter p = Parameter.getParameterByName(id);
                    if(p == null) {
                        WebtrekkLogging.log("invalid parameter name: " + id);
                    } else {
                        // if no key is set, it is a const value
                        if(key == null) {
                            constTp.add(p, value);
                        } else {
                            // else its a mapped value which will be replaced at runtime
                            tp.add(p, key);
                        }
                    }

                }
                //parser.nextTag();
                parser.require(XmlPullParser.END_TAG, ns, "parameter");
            } else if (name.equals("pageParameter")) {
                parser.require(XmlPullParser.START_TAG, ns, "pageParameter");
                setParameterConfigurationFromXml(parser, tp.getPageParameter(), constTp.getPageParameter());
            } else if (name.equals("sessionParameter")) {
                parser.require(XmlPullParser.START_TAG, ns, "sessionParameter");
                setParameterConfigurationFromXml(parser, tp.getSessionParameter(), constTp.getSessionParameter());
            } else if (name.equals("ecomParameter")) {
                parser.require(XmlPullParser.START_TAG, ns, "ecomParameter");
                setParameterConfigurationFromXml(parser, tp.getEcomParameter(), constTp.getEcomParameter());
            } else if (name.equals("userCategories")) {
                parser.require(XmlPullParser.START_TAG, ns, "userCategories");
                setParameterConfigurationFromXml(parser, tp.getUserCategories(), constTp.getUserCategories());
            } else if (name.equals("pageCategories")) {
                parser.require(XmlPullParser.START_TAG, ns, "pageCategories");
                setParameterConfigurationFromXml(parser, tp.getPageCategories(), constTp.getPageCategories());
            } else if (name.equals("adParameter")) {
                parser.require(XmlPullParser.START_TAG, ns, "adParameter");
                setParameterConfigurationFromXml(parser, tp.getAdParameter(), constTp.getAdParameter());
            } else if (name.equals("actionParameter")) {
                parser.require(XmlPullParser.START_TAG, ns, "actionParameter");
                setParameterConfigurationFromXml(parser, tp.getActionParameter(), constTp.getActionParameter());
            } else if (name.equals("productCategories")) {
                parser.require(XmlPullParser.START_TAG, ns, "productCategories");
                setParameterConfigurationFromXml(parser, tp.getProductCategories(), constTp.getProductCategories());
            } else if (name.equals("mediaCategories")) {
                parser.require(XmlPullParser.START_TAG, ns, "mediaCategories");
                setParameterConfigurationFromXml(parser, tp.getMediaCategories(), constTp.getMediaCategories());
            } else {
                WebtrekkLogging.log("trackingparameter: unknown xml tag: " + name);
                skip(parser);
            }
        }
    }

    private void setParameterConfigurationFromXml(XmlPullParser parser, Map<String, String> values, Map<String, String> constValues) throws XmlPullParserException, IOException {

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("parameter")) {
                parser.require(XmlPullParser.START_TAG, ns, "parameter");
                // the id from the parameter identifies the parameter
                String id = parser.getAttributeValue(ns, "id");
                // the key is the key in the custom parameter object
                String key = parser.getAttributeValue(ns, "key");
                String value = readText(parser);

                if (id == null || (value == null && key == null)) {
                    WebtrekkLogging.log("invalid parameter configuration while reading parameter, missing key or value");
                } else {
                    // if no key is set, it is a const value
                    if(key == null) {
                        constValues.put(id, value);
                    } else {
                        // else its a mapped value which will be replaced at runtime
                        values.put(id, key);
                        //parser.nextTag();
                    }
                }

            } else {
                WebtrekkLogging.log("parameter: unknown xml tag: " + name);
                skip(parser);
            }
        }
    }

    void setCustomParameterConfigurationFromXml(XmlPullParser parser, Map<String, String> values) throws XmlPullParserException, IOException {

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("parameter")) {
                parser.require(XmlPullParser.START_TAG, ns, "parameter");
                // the id from the parameter identifies the parameter
                String id = parser.getAttributeValue(ns, "id");
                String value = readText(parser);

                if (id == null || value == null ) {
                    WebtrekkLogging.log("invalid parameter configuration while reading customParameter, missing key or value");
                } else {
                        // else its a mapped value which will be replaced at runtime
                        values.put(id, value);
                }
            } else {
                WebtrekkLogging.log("customparameter: unknown xml tag: " + name);
                skip(parser);
            }
        }
    }

    /**
     * reads a String value from xml tag
     *
     * @param parser
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * skips a tag during parsing the xml string
     *
     * @param parser
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
                default:
                    break;
            }
        }
    }


    private Map<String, String> readRecommendationConfig(XmlPullParser parser) throws XmlPullParserException, IOException {

        Map<String, String> retValue = new HashMap<>();
        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("recommendation"))
            {
                parser.require(XmlPullParser.START_TAG, ns, "recommendation");
                String recName = parser.getAttributeValue(ns, "name");
                String value = readText(parser);

                if (recName == null || value == null || !HelperFunctions.testIsValidURL(value)) {
                    WebtrekkLogging.log("invalid parameter configuration while reading recommendation value, missing name or value or value URL incorrect");
                } else {
                    retValue.put(recName, value);
                }
            }
        }
        return retValue;
    }
}

