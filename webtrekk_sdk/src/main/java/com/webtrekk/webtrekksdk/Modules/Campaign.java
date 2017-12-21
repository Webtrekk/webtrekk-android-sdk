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
 * Created by Arsen Vartbaronov on 03.03.16.
 */

package com.webtrekk.webtrekksdk.Modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.JsonReader;

import com.webtrekk.webtrekksdk.ReferrerReceiver;
import com.webtrekk.webtrekksdk.Request.TrackingRequest;
import com.webtrekk.webtrekksdk.Request.RequestProcessor;
import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class is responsible for calling thread that is get Advertizing ID and processing referrer id. It is extracts click id and sends install request.
 *
 * @hide
 */
public class Campaign extends Thread
{
    private final String mTrackID;
    private final boolean mFirstStart;
    private final boolean mIsAutoTrackAdvID;
    private final Context mContext;
    private final boolean mEnableCampaign;

    private String mMediaCode;

    private static final String ADV_ID = "INSTALL_SETTINGS_ADV_ID";
    private static final String MEDIA_CODE = "INSTALL_SETTINGS_MEDIA_CODE";
    private static final String OPT_OUT = "INSTALL_SETTINGS_OPT_OUT";
    private static final String FIRST_START_INITIATED = "FIRST_START_INITIATED";//old flag need to support old settings
    private static final String FIRST_START_INITIATED_TIME = "FIRST_START_INITIATED_TIME";//old flag need to support old settings
    private static final String CAMPAIGN_MEDIA_CODE_DEFINED_MESSAGE = "com.Webtrekk.CampainMediaMessage";
    private static final String CAMPAIGN_PROCESS_FINISHED = "CAMPAIGN_PROCESS_FINISHED";
    private static final long CAMPAIGN_ANALIZE_TIMEOUT = 60000;
    private static final long CAMPAIGN_ANALIZE_PERIOD = 20000;


    Campaign(Context context, String trackID, boolean isFirstStart, boolean isAutoTrackAdvID, boolean enableCampaign) {

        mContext = context;
        //if it is not first start check if thread was interrupted on first start.
        //It is case when application was closed just after first start.
        mFirstStart = isFirstStart ? isFirstStart : getFirstStartInitiated(context, true);
        mTrackID = trackID;
        mIsAutoTrackAdvID = isAutoTrackAdvID;
        mEnableCampaign = enableCampaign;
    }

    /**
     * @hide
     * Starts thread for collecting Campain data
     * @param context context
     * @param trackID track id
     * @param isFirstStart if this is first start
     * @return instance of Campain class. you need it to interrupt process if application is closed.
     */
    public static Campaign start(Context context, String trackID, boolean isFirstStart, boolean isAutoTrackAdvID, boolean enableCampaign)
    {
        if (trackID == null || trackID.isEmpty())
        {
            WebtrekkLogging.log("Track ID is received to Campain server is null. Campain can't be tracked");
            return null;
        }

        Campaign service = new Campaign(context, trackID, isFirstStart, isAutoTrackAdvID, enableCampaign);
        service.start();
        return service;
    }

    /** @hide */
    private String getStoredReferrer(Context context) {
        String result = null;

        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);

        result = preferences.getString(ReferrerReceiver.REFERRER_KEY_NAME, null);

        return result;
    }

    /** @hide */
    //Parse refferer and get media code
    private String getGoogleAnalyticMediaCode(String referrer)
    {
        String campaign = "";
        String content = "";
        String medium = "";
        String source = "";
        String term = "";

        String[] components = referrer.split("&");
        for (String component : components) {
            String parameter[] = component.split("=", 2);
            if (parameter.length < 2) {
                continue;
            }

            String key = HelperFunctions.urlDecode(parameter[0]);
            String value = HelperFunctions.urlDecode(parameter[1]);

            if ("utm_campaign".equals(key)) {
                campaign = value;
            } else if ("utm_content".equals(key)) {
                content = value;
            } else if ("utm_medium".equals(key)) {
                medium = value;
            } else if ("utm_source".equals(key)) {
                source = value;
            } else if ("utm_term".equals(key)) {
                term = value;
            }
        }

        if (campaign.isEmpty() && medium.isEmpty() && content.isEmpty() && source.isEmpty())
            return null;

        String campaignId = "wt_mc%3D" + HelperFunctions.urlEncode(source + "." + medium + "." + content + "." + campaign);
        if(!term.isEmpty()) {
            campaignId += ";wt_kw%3D" + HelperFunctions.urlEncode(term);
        }

        return campaignId;

    }

    /** @hide */
    private String getClickID(String referrer)
    {
        Pattern pattern = Pattern.compile("(^|&)wt_clickid=([^&#=]*)([#&]|$)");
        Matcher matcher = pattern.matcher(referrer);

        if (matcher.find())
        {
            return matcher.group(2);
        } else
            return null;
    }

    /**
     * @hide
     * Main thread
     */
    @Override
    public void run() {

        WebtrekkLogging.log("starting campain thread. Getting advertising ID");
        String advID = null;
        boolean isLimitAdEnabled = false;

        if (mFirstStart)
            setFirstStartInitiated();

        //get adv ID only if either autoTrackAdID or firstSTart

        if (mIsAutoTrackAdvID || mFirstStart) {
            try {
                Object adInfo = null;
                AdvertisingIdClientReflection client = new AdvertisingIdClientReflection();
                adInfo = client.getAdvertisingIdInfo(mContext);

                if (adInfo != null) {
                    advID = client.getId(adInfo);
                    isLimitAdEnabled = client.isLimitAdTrackingEnabled(adInfo);
                }else {
                    WebtrekkLogging.log("Can't get AdvertisingIdClientRef. Might be Google Play Services or Play Store isn't available.");
                }

                WebtrekkLogging.log("advertiserId: " + advID);

            }catch (Throwable e) {
                String exceptionType = e.getClass().getSimpleName();
                if (exceptionType.equals("IOException")){
                    // Unrecoverable error connecting to Google Play services (e.g.,
                    // the old version of the service doesn't support getting AdvertisingId).
                    WebtrekkLogging.log("Unrecoverable error connecting to Google Play services", e);
                    // the only way understand that process was interrupted.
                    if (e.getMessage().equals("Interrupted exception"))
                        return;
                } else if (exceptionType.equals("GooglePlayServicesNotAvailableException")){
                    // Google Play services is not available entirely.
                    WebtrekkLogging.log("GooglePlayServicesNotAvailableException", e);
                } else if (exceptionType.equals("GooglePlayServicesRepairableException")){
                    // maybe will work with another try, recheck
                }
            }
        }

        if (mEnableCampaign){
            //if this is first start wait for referrer for 30 seconds.
            final long waitForReferrerDelay = 10000;
            long timeToEndListener = System.currentTimeMillis() + waitForReferrerDelay;
            String referrer = null;

            if (mFirstStart) {
                //Wait for referrer max 1 minute
                WebtrekkLogging.log("Start waiting for referrer");
                while (System.currentTimeMillis() < timeToEndListener) {
                    if ((referrer = getStoredReferrer(mContext)) != null)
                        break;

                    try {
                        //ask each 5 seconds for referer;
                        sleep(5000);
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
                WebtrekkLogging.log("Referrer is received and readed:"+referrer);
            }
            String clickID = null;
            String googleMediaCode = null;

            // for non first start referrer is always null
            if (referrer != null)
            {
                // is it referrer from Webtrekk
                clickID = getClickID(referrer);

                if (clickID != null)
                {
                    WebtrekkLogging.log("Click ID:" + clickID);
                }else
                {
                    googleMediaCode = getGoogleAnalyticMediaCode(referrer);
                    if (googleMediaCode != null) {
                        SaveCodeAndAdID(googleMediaCode, advID, isLimitAdEnabled);
                        campaignNotificationMessage(googleMediaCode, advID);
                        //delete first start
                        finishProcessCampaign(mContext);
                    }
                }
            }

            String webtrekkMediaCode = null;

            // if this is webtrekk referrer get media code.
            if (googleMediaCode == null) {
                if (mFirstStart) {
                    // if thread isn't interrupted. Request for media code
                    if (!isInterrupted())
                        webtrekkMediaCode = requestMediaCode(advID, clickID, HelperFunctions.getUserAgent());
                    else
                        return;
                }

                SaveCodeAndAdID(webtrekkMediaCode, advID, isLimitAdEnabled);
                campaignNotificationMessage(webtrekkMediaCode, advID);
                //delete first start and set flag for finishing campaign processing
                finishProcessCampaign(mContext);
            }
        }else{
            SaveCodeAndAdID(null, advID, isLimitAdEnabled);
            campaignNotificationMessage("NoCampaignMode", advID);
            //delete first start and set flag for finishing campaign processing
            finishProcessCampaign(mContext);
        }
    }

    /*
    Request media code with Install request. Provides all infromation in request it can.
    Sends request anyway as userAgent should be present.
     */
    private String requestMediaCode(String advID, String clickID, String userAgent)
    {
        RequestProcessor requestProcessor = new RequestProcessor(null);

        final TrackingParameter tp = new TrackingParameter();


        tp.add(TrackingParameter.Parameter.INST_TRACK_ID, mTrackID);

        if (advID != null && !advID.isEmpty())
            tp.add(TrackingParameter.Parameter.INST_AD_ID , advID);

        if (clickID != null && !clickID.isEmpty())
            tp.add(TrackingParameter.Parameter.INST_CLICK_ID, clickID);

        if (userAgent != null)
            tp.add(TrackingParameter.Parameter.USERAGENT, userAgent);

        final TrackingRequest tr = new TrackingRequest(tp, null, TrackingRequest.RequestType.INSTALL);
        final long finishTime = getFirstStartInitiatedTime(mContext) + Campaign.CAMPAIGN_ANALIZE_TIMEOUT;

        do {
            try {
                String installURL = tr.getUrlString();
                WebtrekkLogging.log("Attempt to get media code with install url:" + installURL);

                requestProcessor.sendRequest(new URL(installURL), new RequestProcessor.ProcessOutputCallback() {
                    @Override
                    public void process(int statusCode, HttpURLConnection connection) {
                        JsonReader jsonReader = null;
                        String mediaCodeRaw = null;
                        try {

                            if (statusCode == 200) {
                                InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
                                jsonReader = new JsonReader(reader);

                                jsonReader.beginObject();

                                while (jsonReader.hasNext()) {
                                    final String name = jsonReader.nextName();
                                    if (name.equals("mediacode")) {
                                        mediaCodeRaw = jsonReader.nextString();
                                    } else
                                        jsonReader.skipValue();
                                }

                                jsonReader.endObject();

                                if (mediaCodeRaw != null) {
                                    WebtrekkLogging.log("Media code is received:" + mediaCodeRaw);

                                    mMediaCode = mediaCodeRaw.substring(mediaCodeRaw.indexOf("=") + 1);
                                } else
                                    WebtrekkLogging.log("Media code isn't defined from response.");
                            } else
                                WebtrekkLogging.log("Install request failed with status code:" + statusCode);
                        } catch (IOException e) {
                            WebtrekkLogging.log("Incorrect install Get response:" + e.getMessage());
                        } finally {

                            if (jsonReader != null) {
                                try {
                                    jsonReader.close();
                                } catch (IOException e) {
                                }
                            }
                        }

                    }
                });
                sleep(Campaign.CAMPAIGN_ANALIZE_PERIOD);

            } catch(MalformedURLException e){
                WebtrekkLogging.log("Error constructing INSTALL URL:" + e.getMessage());
            } catch(InterruptedException e){
                WebtrekkLogging.log("Interruption exception error");
            }
        }while (finishTime > System.currentTimeMillis() && mMediaCode == null);

        return  mMediaCode;
    }

    /**
     Save all information to settings. So it can be used in future and in case something happened with application.
     */
    private void SaveCodeAndAdID(String mediaCode, String advertizingID, boolean isOptOut)
    {
        SharedPreferences.Editor editor = HelperFunctions.getWebTrekkSharedPreference(mContext).edit();

        WebtrekkLogging.log("Campain data is saved mediaCode:" + mediaCode + " advertizingID:" + advertizingID + " isOptOut:" + isOptOut);

        if (mediaCode != null)
            editor.putString(MEDIA_CODE, mediaCode);
        if (advertizingID != null)
            editor.putString(ADV_ID, advertizingID);
        editor.putBoolean(OPT_OUT, isOptOut);
        editor.apply();
    }

    /**
     * save if thread was interrupted just after first start. So we can process referrer one more time after second start
     */
    private void setFirstStartInitiated()
    {
        SharedPreferences.Editor editor = HelperFunctions.getWebTrekkSharedPreference(mContext).edit();

        editor.putLong(FIRST_START_INITIATED_TIME, System.currentTimeMillis()).apply();
    }

    private void finishProcessCampaign(@NonNull Context context){
        getFirstStartInitiated(context, true);
        SharedPreferences.Editor editor = HelperFunctions.getWebTrekkSharedPreference(context).edit();
        editor.putBoolean(Campaign.CAMPAIGN_PROCESS_FINISHED, true).apply();
    }

    /**
     * Check if campaign is processed
     * {@hide}
     * @param context
     * @return true if campaign processing is finished
     */
    static public boolean isCampaignProcessingFinished(@NonNull Context context){
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);
        return preferences.getBoolean(Campaign.CAMPAIGN_PROCESS_FINISHED, false);
    }

    /**
     * {@hide}
     * get if thread was interrupted see {@link #setFirstStartInitiated()} delete flag from settings
     * @return
     */
    public static boolean getFirstStartInitiated(@NonNull Context context, boolean deleteFlag)
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);

        boolean isFirstStartOld = preferences.getBoolean(FIRST_START_INITIATED, false);
        boolean isFirstStartTrue = preferences.getLong(FIRST_START_INITIATED_TIME, -1) > 0;


        if (deleteFlag) {
            preferences.edit().remove(FIRST_START_INITIATED).apply();
            preferences.edit().remove(FIRST_START_INITIATED_TIME).apply();
        }

        return isFirstStartOld | isFirstStartTrue;
    }

    /**
     * {@hide}
     * get time of start campaign processing
     * @return
     */
    @IntRange(from=0)
    public static long getFirstStartInitiatedTime(@NonNull Context context)
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);

        return preferences.getLong(FIRST_START_INITIATED_TIME, -1);
    }

    public static String getAdvId(@NonNull Context context)
    {
        return getAndRemoveInstallSpecificCode(context, ADV_ID, false);
    }

    public static String getMediaCode(@NonNull Context context)
    {
        return getAndRemoveInstallSpecificCode(context, MEDIA_CODE, true);
    }

    public static boolean getOptOut(@NonNull Context context)
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);
        return preferences.getBoolean(OPT_OUT, false);
    }

    /**
     * help funtion that get information from setting and optionally remove it
     * @param context
     * @param key
     * @param remove
     * @return
     */
    static private String getAndRemoveInstallSpecificCode(@NonNull Context context, @NonNull String key, boolean remove)
    {
        String value;
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);
        value = preferences.getString(key, null);
        if (value != null && remove)
          preferences.edit().remove(key).apply();
        return value;
    }

    /**
     * Some test message for test application
     * @param mediaCode
     * @param advID
     */
    private void campaignNotificationMessage(String mediaCode, String advID)
    {
        try{
            if (!mFirstStart)
                return;
            Intent intent = new Intent(CAMPAIGN_MEDIA_CODE_DEFINED_MESSAGE);

            intent.putExtra(MEDIA_CODE, mediaCode);
            intent.putExtra(ADV_ID, advID);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }catch (NoClassDefFoundError e){
            WebtrekkLogging.log("Cann't broad cast campain message:"+e.getMessage());
        }
    }

    private static class AdvertisingIdClientReflection {

        Object getAdvertisingIdInfo(Context context) throws Throwable {
            return callMethod("com.google.android.gms.ads.identifier.AdvertisingIdClient", null, "getAdvertisingIdInfo", new Class[]{Context.class}, context);
        }

        String getId(Object info) throws Throwable{
                return callMethod(null, info, "getId", null);
        }

        boolean isLimitAdTrackingEnabled(Object info) throws Throwable{
                Boolean result = callMethod(null, info, "isLimitAdTrackingEnabled", null);
                return result == null ? false : result;
        }

        private <T extends Object> T callMethod(String className, Object classInstance, String methodName, Class[] argumentsTypes, Object... argumentsValues) throws Throwable {
            try {
                Class classObj = classInstance == null ? Class.forName(className) : classInstance.getClass();

                Method method = classObj.getMethod(methodName, argumentsTypes);

                return (T) method.invoke(classInstance, argumentsValues);
            }catch (InvocationTargetException e) {
                throw e.getCause();
            } catch (Exception e){
                return null;
            }
        }
    }
}
