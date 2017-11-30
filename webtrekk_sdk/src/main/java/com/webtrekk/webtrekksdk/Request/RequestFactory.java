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
 * Created by Arsen Vartbaronov on 11.04.16.
 */

package com.webtrekk.webtrekksdk.Request;

import android.content.Context;
import android.content.SharedPreferences;

import com.webtrekk.webtrekksdk.Configuration.ActivityConfiguration;
import com.webtrekk.webtrekksdk.Modules.AppinstallGoal;
import com.webtrekk.webtrekksdk.Modules.Campaign;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;
import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Class should incapsulate all request data operation and data
 */
public class RequestFactory {

    private boolean mIsOptout;

    private boolean mIsSampling;
    public static final String PREFERENCE_KEY_OPTED_OUT = "optedOut";
    public static final String PREFERENCE_KEY_IS_SAMPLING = "issampling";
    public static final String PREFERENCE_KEY_SAMPLING = "sampling";

    private Context mContext;
    private TrackingConfiguration mTrackingConfiguration;
    private volatile Campaign mCampaign;
    private final AppinstallGoal mAppinstallGoal = new AppinstallGoal();

    //additional customer params, this is a globally available HashMap
    //before the requests are sent, the keys of this HashMap are matched with the keys in the xml configuration or the global/
    //local tracking params, and override them, so for example key = orientation, value = horizontal
    //in the xml configuraton then is the trackingparameter requests defined with ecomerce_parameter "1" and the key orientation
    // before the request url is generated this keys will be replaced with values from this map
    // the customParameter are set by the user and are only valid for the current activity
    private Map<String, String> mCustomParameter;
    private Map<String, String> mAutoCustomParameter;

    // this hashmap contains all the default parameter which are defined by webtrekk and have an url mapping
    private HashMap<Parameter, String> mWebtrekkParameter;

    // this TrackingParameter object contains keys or parameter which needs to be send to manage the internal state
    // this are only 1 once, for one request, the customer must not have access to this ones,
    // after the where appended to the next request they will be removed here from this object again
    // this will be used for example for force new session, one, or app first installed
    private TrackingParameter mInternalParameter;

    // this always contains the name of the current activity as string, important for auto naming button clicks or other inner class events
    private String mCurrentActivityName;


    // this tracking params allows to add parameters globally to all tracking requests in the configured app
    // this values can also be configured in the xml file and will be overriten by the values configured there
    private TrackingParameter mGlobalTrackingParameter;
    // same as the globalTrackingParameter but will not be replaced, fixed values can be added from code or xml
    private TrackingParameter mConstGlobalTrackingParameter;

    private RequestUrlStore mRequestUrlStore;
    private String mCustomPageName;

    private ScheduledExecutorService mURLSendTimerService;
    private ScheduledFuture<?> mURLSendTimerFuture;
    private ExecutorService mExecutorService;
    private Future<?> mRequestProcessorFuture;

    volatile private long mLastTrackTime;
    private ScheduledExecutorService mFlashTimerService;
    private ScheduledFuture<?> mFlashTimerFuture;


    public void init(Context context, TrackingConfiguration trackingConfiguration, Webtrekk wt)
    {
        mContext = context;
        mTrackingConfiguration = trackingConfiguration;

        if(mCustomParameter == null) {
            mCustomParameter = new HashMap<>();
        }

        boolean isFirstStart = HelperFunctions.firstStart(mContext);

        initOptedOut();
        mAppinstallGoal.initAppinstallGoal(mContext);
        startAdvertizingThread(isFirstStart);
        initSampling();
        initInternalParameter(isFirstStart);
        initWebtrekkParameter();
        initAutoCustomParameter();
        initURLSendTimerService();
        initFlashTimerService();

        mRequestUrlStore = new RequestUrlStore(mContext);
        mConstGlobalTrackingParameter = new TrackingParameter();
        mGlobalTrackingParameter = new TrackingParameter();

    }

    public boolean isOptout() {
        return mIsOptout;
    }

    public void setIsOptout(boolean isOptout) {
        if (mIsOptout == isOptout) {
            return;
        }
        mIsOptout = isOptout;

        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(mContext);
        preferences.edit().putBoolean(PREFERENCE_KEY_OPTED_OUT, isOptout).apply();
    }

    public boolean isSampling() {
        return mIsSampling;
    }

    public void setIsSampling(boolean isSampling) {
        mIsSampling = isSampling;
    }

    public void setCustomParameter(Map<String, String> customParameter) {
        mCustomParameter = customParameter;
    }

    public Map<String, String> getCustomParameter() {
        return mCustomParameter;
    }

    public HashMap<Parameter, String> getWebtrekkParameter() {
        return mWebtrekkParameter;
    }

    public TrackingParameter getInternalParameter() {
        return mInternalParameter;
    }

    public Map<String, String> getAutoCustomParameter() {
        return mAutoCustomParameter;
    }

    public String getCurrentActivityName() {
        return mCurrentActivityName;
    }

    public void setCurrentActivityName(String currentActivityName) {
        mCurrentActivityName = currentActivityName;
        mCustomPageName = null;
    }

    public void setCustomPageName(String customPageName) {
        mCustomPageName = customPageName;
    }

    public RequestUrlStore getRequestUrlStore() {
        return mRequestUrlStore;
    }

    public void setRequestUrlStore(RequestUrlStore requestUrlStore) {
        mRequestUrlStore = requestUrlStore;
    }

    public TrackingParameter getGlobalTrackingParameter() {
        return mGlobalTrackingParameter;
    }

    public void setGlobalTrackingParameter(TrackingParameter globalTrackingParameter) {
        mGlobalTrackingParameter = globalTrackingParameter;
    }

    public TrackingParameter getConstGlobalTrackingParameter() {
        return mConstGlobalTrackingParameter;
    }

    public void setConstGlobalTrackingParameter(TrackingParameter constGlobalTrackingParameter) {
        mConstGlobalTrackingParameter = constGlobalTrackingParameter;
    }

    public void setTrackingConfiguration(TrackingConfiguration trackingConfiguration) {
        mTrackingConfiguration = trackingConfiguration;
    }

    public TrackingConfiguration getTrackingConfiguration() {
        return mTrackingConfiguration;
    }

    private void initInternalParameter(boolean isFirstStart) {
        if(mInternalParameter == null) {
            mInternalParameter = new TrackingParameter();
        }
        forceNewSession();
        // if the app is started for the first time, the param "one" is 1 otherwise its always 0
        if(isFirstStart) {
            mInternalParameter.add(Parameter.APP_FIRST_START, "1");
        } else {
            mInternalParameter.add(Parameter.APP_FIRST_START, "0");
        }
    }

    public void forceNewSession()
    {
        // first initalization of the webtrekk instance, so set fns to 1
        mInternalParameter.add(Parameter.FORCE_NEW_SESSION, "1");
    }

    /**
     * initializes the opt out based on the shared preference settings
     */
    private void initOptedOut() {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(mContext);
        mIsOptout = preferences.getBoolean(PREFERENCE_KEY_OPTED_OUT, false);
        WebtrekkLogging.log("optedOut = " + mIsOptout);
    }

    /**
     * initializes the sampling, which means that if a sampling value X is configured, only
     * every X user data will be tracked, sampling will be stores in the shared prefs once initialized
     * it can be reset with changing the xml config
     */
    private void initSampling() {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(mContext);;

        if(preferences.contains(PREFERENCE_KEY_IS_SAMPLING)) {
            // key exists so set sampling value and return
            mIsSampling = preferences.getBoolean(PREFERENCE_KEY_IS_SAMPLING, false);
            // check if the sampling value is unchanged, if so return
            if(preferences.getInt(PREFERENCE_KEY_SAMPLING, -1) == mTrackingConfiguration.getSampling()) {
                return;
            }
        }
        // from here on they sampling either changed or is missing, so reinitialize it
        SharedPreferences.Editor editor = preferences.edit();
        // calculate if the device is sampling
        if(mTrackingConfiguration.getSampling()>1) {
            mIsSampling = (Long.valueOf(HelperFunctions.getEverId(mContext)) % mTrackingConfiguration.getSampling()) != 0;
        } else {
            mIsSampling = false;
        }
        // store the preference keys if the device is sampling and the sampling value
        editor.putBoolean(PREFERENCE_KEY_IS_SAMPLING, mIsSampling);
        editor.putInt(PREFERENCE_KEY_SAMPLING, mTrackingConfiguration.getSampling());
        editor.apply();
        WebtrekkLogging.log("isSampling = " + mIsSampling + ", samplingRate = " + mTrackingConfiguration.getSampling());
    }

    /**
     * collecting all data and set the appropriate parameter
     * this function inserts all data into the mWebtrekkParameter HashMap for which tracking is enabled in the xml
     * customers can decide on their own based on their xml tracking config, which values they are interested in
     * this method initializes the mWebtrekkParameter hashmap, so all parameters for which an url is defined
     */
    private void initWebtrekkParameter() {
        // collect all static device information which remain the same for all requests
        if(mWebtrekkParameter == null) {
            mWebtrekkParameter = new HashMap<>();
        }

        mWebtrekkParameter.put(Parameter.SCREEN_DEPTH, HelperFunctions.getDepth(mContext));
        mWebtrekkParameter.put(Parameter.TIMEZONE, HelperFunctions.getTimezone());
        mWebtrekkParameter.put(Parameter.USERAGENT, HelperFunctions.getUserAgent());
        mWebtrekkParameter.put(Parameter.DEV_LANG, HelperFunctions.getCountry());


        // for compatibility reasons always add the sampling rate param to the url
        mWebtrekkParameter.put(Parameter.SAMPLING, "" + mTrackingConfiguration.getSampling());

        // always track the wt everid
        initEverID();

        WebtrekkLogging.log("collected static automatic data");
    }

    public void initEverID() {
        mWebtrekkParameter.put(Parameter.EVERID, HelperFunctions.getEverId(mContext));
    }

    /**
     * this method initializes the custom parameter values which are predefined by Webtrekk
     * the customer can also add new ones, unknown entries will be ignored by the server
     */
    public void initAutoCustomParameter() {
        if(mAutoCustomParameter == null) {
            mAutoCustomParameter = new HashMap<>();
        }

        if(mCustomParameter == null) {
            mCustomParameter = new HashMap<>();
        }

        if(mTrackingConfiguration.isAutoTrackAppVersionName()) {
            mAutoCustomParameter.put("appVersion", HelperFunctions.getAppVersionName(mContext));
        }

        if(mTrackingConfiguration.isAutoTrackAppVersionCode()) {
            mAutoCustomParameter.put("appVersionCode", String.valueOf(HelperFunctions.getAppVersionCode(mContext)));
        }

        if(mTrackingConfiguration.isAutoTrackPlaystoreUsername()) {
            Map<String, String> playstoreprofile = HelperFunctions.getUserProfile(mContext);
            mAutoCustomParameter.put("playstoreFamilyname", playstoreprofile.get("sname"));
            mAutoCustomParameter.put("playstoreGivenname", playstoreprofile.get("gname"));
        }

        if(mTrackingConfiguration.isAutoTrackPlaystoreMail()) {
            //Map<String, String> playstoreprofile = HelperFunctions.getUserProfile(mContext);
            //customParameter.put("playstoreMail", playstoreprofile.get("email"));
            mAutoCustomParameter.put("playstoreMail", HelperFunctions.getMailByAccountManager(mContext));
        }

        if (mTrackingConfiguration.isAutoTrackAppPreInstalled()) {
            mAutoCustomParameter.put("appPreinstalled", String.valueOf(HelperFunctions.isAppPreinstalled(mContext)));
        }

        if(mTrackingConfiguration.isAutoTrackAdClearId()) {
            mAutoCustomParameter.put("adClearId", String.valueOf(HelperFunctions.getAdClearId(mContext)));
        }

        // if the app was updated, send out the update request once

        if(mTrackingConfiguration.isAutoTrackAppUpdate()) {
            int currentVersion = HelperFunctions.getAppVersionCode(mContext);
            // store the app version code to check for updates
            if(HelperFunctions.firstStart(mContext)) {
                HelperFunctions.setAppVersionCode(currentVersion, mContext);
            }

            if(HelperFunctions.updated(mContext, currentVersion)) {
                mAutoCustomParameter.put("appUpdated", "1");
            } else  {
                mAutoCustomParameter.put("appUpdated", "0");
            }

        }
        if(mTrackingConfiguration.isAutoTrackApiLevel()) {
            mAutoCustomParameter.put("apiLevel", HelperFunctions.getAPILevel());

        }

    }

    /**
     * this method updates the webtrekk and customer parameter which change with every request
     * @return
     */
    public void updateDynamicParameter() {
        // put the screen orientation to into the custom parameter, will change with every request
        if(mAutoCustomParameter != null) {
            mAutoCustomParameter.put("screenOrientation", HelperFunctions.getOrientation(mContext));
            mAutoCustomParameter.put("connectionType", HelperFunctions.getConnectionString(mContext));

            if(mTrackingConfiguration.isAutoTrackAdvertiserId() && !mAutoCustomParameter.containsKey("advertiserId")
                    && Campaign.getAdvId(mContext) != null) {
                mAutoCustomParameter.put("advertiserId", Campaign.getAdvId(mContext));

                if (mTrackingConfiguration.isAutoTrackAdvertismentOptOut() && !mAutoCustomParameter.containsKey("advertisingOptOut"))
                    mAutoCustomParameter.put("advertisingOptOut", String.valueOf(Campaign.getOptOut(mContext)));
            }


            if(mRequestUrlStore != null && mTrackingConfiguration.isAutoTrackRequestUrlStoreSize()) {
                mAutoCustomParameter.put("requestUrlStoreSize", String.valueOf(mRequestUrlStore.size()));
            }
        }

        if(mWebtrekkParameter != null) {
            // also update the webtrekk parameter
            mWebtrekkParameter.put(Parameter.SCREEN_RESOLUTION, HelperFunctions.getResolution(mContext));
        }
    }

    /*
    Process campaignData
     */
    private void processInstallGoals(TrackingRequest request)
    {
        final boolean isCampaingProcessFinished = Campaign.isCampaignProcessingFinished(mContext);

        if (isCampaingProcessFinished) {

            if (mAppinstallGoal.isAppinstallGoal(mContext)){
                request.mTrackingParameter.add(Parameter.ECOM, "900", "1");
                mAppinstallGoal.finishAppinstallGoal(mContext);
            }

            final String mediaCode = Campaign.getMediaCode(mContext);

            if (mediaCode != null && !mediaCode.isEmpty()) {
                request.mTrackingParameter.add(Parameter.ADVERTISEMENT, mediaCode);
                request.mTrackingParameter.add(Parameter.ADVERTISEMENT_ACTION, "c");
            } else {
                String deepLinkMediaCode = HelperFunctions.getDeepLinkMediaCode(mContext, true);
                if (deepLinkMediaCode != null && !deepLinkMediaCode.isEmpty()) {
                    request.mTrackingParameter.add(Parameter.ADVERTISEMENT, deepLinkMediaCode);
                }
            }
        }
    }

    public void onFirstStart()
    {
        restore();
        //restart referrer getting if applicaiton was paused and resumed back
        if (Campaign.getFirstStartInitiated(mContext, false) && (mCampaign == null || (mCampaign != null && !mCampaign.isAlive()))) {
            startAdvertizingThread(true);
        }
    }

    public void stop()
    {
        flush();
        if (mCampaign != null && mCampaign.isAlive() && !mCampaign.isInterrupted()) {
            mCampaign.interrupt();
        }
    }

    public void restore()
    {
        mRequestUrlStore.reset();
        // remove the old backupfile after the requests are loaded into memory/requestUrlStore
        //mRequestUrlStore.deleteRequestsFile();
    }

    public void flush()
    {
        stopSendURLProcess();
        mRequestUrlStore.flush();
    }

    /**
     * This method creates a TrackingRequest object and applies the various overrides from the xml
     * configuration. While doing so, the hierarchy for overriding values is taken into account.
     *
     * @param tp
     * @return
     */
    public TrackingRequest createTrackingRequest(TrackingParameter tp) {
        // create a new trackingParameter object
        TrackingParameter trackingParameter = new TrackingParameter();
        // add the name of the current activity
        trackingParameter.add(Parameter.ACTIVITY_NAME, mCurrentActivityName);

        trackingParameter.add(Parameter.TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        //add the internal parameter
        trackingParameter.add(mInternalParameter);

        // action params are a special case, no other params but the ones given as parameter in the code
        if(tp.containsKey(Parameter.ACTION_NAME)) {
            applyActivityConfiguration(trackingParameter, true);
            overrideCustomPageName(trackingParameter);
            trackingParameter.add(Parameter.SCREEN_RESOLUTION, mWebtrekkParameter.get(Parameter.SCREEN_RESOLUTION));
            trackingParameter.add(Parameter.SCREEN_DEPTH, mWebtrekkParameter.get(Parameter.SCREEN_DEPTH));
            trackingParameter.add(Parameter.USERAGENT, mWebtrekkParameter.get(Parameter.USERAGENT));
            trackingParameter.add(Parameter.EVERID, mWebtrekkParameter.get(Parameter.EVERID));
            trackingParameter.add(Parameter.SAMPLING, mWebtrekkParameter.get(Parameter.SAMPLING));
            trackingParameter.add(Parameter.TIMEZONE, mWebtrekkParameter.get(Parameter.TIMEZONE));
            trackingParameter.add(Parameter.DEV_LANG, mWebtrekkParameter.get(Parameter.DEV_LANG));

            // Also add the auto parameters that are defined to be send with action requests
            if(mAutoCustomParameter!= null) {
                trackingParameter.add(mTrackingConfiguration.getAutoTrackedParameters(mAutoCustomParameter, true));
            }

            trackingParameter.add(tp);
            return new TrackingRequest(trackingParameter, mTrackingConfiguration);
        }

        // update the dynamic parameter which change with every request
        updateDynamicParameter();
        // add the default parameter
        trackingParameter.add(mWebtrekkParameter);

        // add the autotracked custom params to the custom params
        if(mCustomParameter!= null) {
            mCustomParameter.putAll(mAutoCustomParameter);
        }


        // first add the globally configured trackingparams which are defined in the webtrekk.globalTrackingParameter, if there are any
        if(mConstGlobalTrackingParameter != null) {
            trackingParameter.add(mConstGlobalTrackingParameter);
        }

        // apply autotracking parameters
        if(mAutoCustomParameter!= null) {
            trackingParameter.add(mTrackingConfiguration.getAutoTrackedParameters(mAutoCustomParameter, false));
        }

        //now map the string values from the code tracking parameters to the custom values defined by webtrekk or the customer
        if(mCustomParameter!= null && mGlobalTrackingParameter != null) {
            // first map the global tracking parameter
            TrackingParameter mappedTrackingParameter = mGlobalTrackingParameter.applyMapping(mCustomParameter);
            trackingParameter.add(mappedTrackingParameter);
        }

        // second add the globally configured const trackingparams from the xml which may override the ones above
        if(mTrackingConfiguration.getConstGlobalTrackingParameter() != null) {
            trackingParameter.add(mTrackingConfiguration.getConstGlobalTrackingParameter());
        }
        // also add the globally configured mapped trackingparams from the xml which may override the ones above
        if(mTrackingConfiguration.getGlobalTrackingParameter() != null) {
            TrackingParameter mappedTrackingParameter = mTrackingConfiguration.getGlobalTrackingParameter().applyMapping(mCustomParameter);
            trackingParameter.add(mappedTrackingParameter);
        }

        // third add the local ones from the activity which may override all of the above params, this are passed from the track call
        trackingParameter.add(tp);

        //forth add the local ones which each activity has defined in its xml configuration, they will override the ones above
        applyActivityConfiguration(trackingParameter, false);
        overrideCustomPageName(trackingParameter);

        return new TrackingRequest(trackingParameter, mTrackingConfiguration);

    }

    private void applyActivityConfiguration(TrackingParameter trackingParameter, boolean onlyNameApply)
    {
        if(mTrackingConfiguration.getActivityConfigurations()!= null && mTrackingConfiguration.getActivityConfigurations().containsKey(mCurrentActivityName)){
            ActivityConfiguration activityConfiguration = mTrackingConfiguration.getActivityConfigurations().get(mCurrentActivityName);
            if(activityConfiguration != null) {
                if(activityConfiguration.getConstActivityTrackingParameter() != null && !onlyNameApply) {
                    trackingParameter.add(activityConfiguration.getConstActivityTrackingParameter());
                }
                TrackingParameter mappedTrackingParameter = activityConfiguration.getActivityTrackingParameter();
                if( mappedTrackingParameter != null && !onlyNameApply) {
                    //now map the string values from the xml/code tracking parameters to the custom values defined by webtrekk or the customer
                    if(mCustomParameter!= null) {
                        // first map the global tracking parameter
                        trackingParameter.add(mappedTrackingParameter.applyMapping(mCustomParameter));
                    }
                }
                // override the activityname if a mapping name is given
                if(activityConfiguration.getMappingName() != null) {
                    trackingParameter.add(Parameter.ACTIVITY_NAME, activityConfiguration.getMappingName());
                }
            }
        }
    }

    private void overrideCustomPageName(TrackingParameter trackingParameter)
    {
        if (mCustomPageName != null)
            trackingParameter.add(Parameter.ACTIVITY_NAME, mCustomPageName);
    }

    /**
     * Stores the generated URLS of the requests in the local RequestUrlStore until they are send
     * by the timer function.
     *
     * @param request the Tracking Request
     */
    public void addRequest(TrackingRequest request)  {

        processInstallGoals(request);

        // only track if not opted out
        if(!mIsOptout && !mIsSampling) {
            String urlString = request.getUrlString();
            WebtrekkLogging.log("adding url: " + urlString);
            mRequestUrlStore.addURL(urlString);
        }

        // after the url is created reset the internal parameters to zero
        //TODO: special case where they could not be send and the url got removed?
        mInternalParameter.add(Parameter.FORCE_NEW_SESSION, "0");
        mInternalParameter.add(Parameter.APP_FIRST_START, "0");
        mAutoCustomParameter.put("appUpdated", "0");
    }

    /**
     * Start thread for advertazing campaign and getting adv ID.
     * After thread is finished make link to object null for GC
     * @param isFirstStart
     */
    public void startAdvertizingThread(boolean isFirstStart)
    {
        if (!mIsOptout) {
            mCampaign = Campaign.start(mContext, mTrackingConfiguration.getTrackId(), isFirstStart,
                    mTrackingConfiguration.isAutoTrackAdvertiserId(), mTrackingConfiguration.isEnableCampaignTracking());
        }
    }


    /**
     * starts the timer service, it executes after initial send delay for the first time, and then
     * every sendDelay seconds, it processes the stored requests in a separate thread
     */
    void initURLSendTimerService() {
        if (mTrackingConfiguration.getSendDelay() > 0) {
            // start the timer service
            mURLSendTimerService = Executors.newSingleThreadScheduledExecutor();
            mURLSendTimerFuture = mURLSendTimerService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    onSendIntervalOver();
                }
            }, mTrackingConfiguration.getSendDelay(), mTrackingConfiguration.getSendDelay(), TimeUnit.SECONDS);
            WebtrekkLogging.log("timer service started");
        }
    }

    /**
     * Init service that do flush by 1 min timeout
     */
    private void initFlashTimerService() {
        if (mURLSendTimerService == null)
            mURLSendTimerService = Executors.newSingleThreadScheduledExecutor();
        mFlashTimerService = Executors.newSingleThreadScheduledExecutor();
        mFlashTimerFuture = mURLSendTimerService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                flashByTimeout();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * this method gets called whenever the send delay is over, it executes the requesthandler in a
     * new thread
     * @return true if send is done and false if previous send is still in progress or there is no message to send
     */
    public boolean onSendIntervalOver() {
        //WebtrekkLogging.log("onSendIntervalOver: request urls: " + mRequestUrlStore.size()
                //+ " thread done:"+(mRequestProcessorFuture == null ? "null": mRequestProcessorFuture.isDone()));
        if(mRequestUrlStore.size() > 0  && (mRequestProcessorFuture == null || mRequestProcessorFuture.isDone())) {
            if (mExecutorService == null) {
                // use daemon thread.
                mExecutorService = Executors.newSingleThreadExecutor(new ThreadFactory() {

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = Executors.defaultThreadFactory().newThread(r);
                        t.setDaemon(true);
                        return t;
                    }
                });
            }
            mRequestProcessorFuture = mExecutorService.submit(new RequestProcessor(mRequestUrlStore));
            return true;
        }else
            return false;
    }

    private void flashByTimeout()
    {
        if (mFlashTimerFuture == null)
            return;
        if ((System.currentTimeMillis() - mLastTrackTime) > 60000 && (mRequestProcessorFuture == null || mRequestProcessorFuture.isDone()))
            flush();
    }

    public void stopSendURLProcess()
    {
        if (mRequestProcessorFuture != null && !mRequestProcessorFuture.isDone()) {
            mRequestProcessorFuture.cancel(true);
            mExecutorService.shutdownNow();
            try {
                // waiting for 4 seconds to avoid ANR
                WebtrekkLogging.log("Start waiting for send URL thread to stop");
                boolean isTerminated = mExecutorService.awaitTermination(4, TimeUnit.SECONDS);
                WebtrekkLogging.log("Stop to wait for send URL thread. Process stopped result is:"+isTerminated);
            } catch (InterruptedException e) {
                WebtrekkLogging.log("Can't terminate sending process");
            }
            mExecutorService = null;
            WebtrekkLogging.log("Processing URL is canceled");
        }
    }

    public void setLasTrackTime(long lasTrackTime) {
        mLastTrackTime = lasTrackTime;
    }
}
