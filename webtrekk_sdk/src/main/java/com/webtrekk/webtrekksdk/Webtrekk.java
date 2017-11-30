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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.webkit.WebView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.webtrekk.webtrekksdk.Modules.ExceptionHandler;
import com.webtrekk.webtrekksdk.Request.RequestFactory;
import com.webtrekk.webtrekksdk.Request.TrackingRequest;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.Configuration.ActivityConfiguration;
import com.webtrekk.webtrekksdk.Utils.ActivityListener;
import com.webtrekk.webtrekksdk.Utils.ActivityTrackingStatus;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfigurationDownloadTask;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfigurationXmlParser;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

/**
 * The WebtrekkSDK main class, the developer/customer interacts with the SDK through this class.
 */
public class Webtrekk implements ActivityListener.Callback {


    //name of the preference strings
    public static final String PREFERENCE_FILE_NAME = "webtrekk-preferences";
    public static final String PREFERENCE_KEY_EVER_ID = "everId";
    public static final String PREFERENCE_APP_VERSIONCODE = "appVersion";
    public static final String PREFERENCE_KEY_INSTALLATION_FLAG = "InstallationFlag";
    public static final String PREFERENCE_KEY_CONFIGURATION = "webtrekkTrackingConfiguration";
    public static String mTrackingLibraryVersionUI;
    public static String mTrackingLibraryVersion;

    final private RequestFactory mRequestFactory = new RequestFactory();
    private TrackingConfiguration trackingConfiguration;

    private Context mContext;

    //Current status of activities
    private ActivityListener mActivityStatus;
    final private ExceptionHandler mExceptionHandler = new ExceptionHandler();
    private ProductListTracker mProductListTracker;
    private boolean mIsInitialized;


    /**
     * non public constructor to create a Webtrekk Instance as
     * makes use of the Singleton Pattern here.
     */

    Webtrekk() {
    }

    // https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
    private static class SingletonHolder {
        static final Webtrekk webtrekk = new Webtrekk();
    }

    /**
     * public method to get the singleton instance of the webtrekk object,
     * @return webtrekk instance
     */
    public static Webtrekk getInstance() {
        return SingletonHolder.webtrekk;
    }

    /**
     * this initializes the webtrekk tracking configuration, it has to be called only once when the
     * application starts, for example in the Application Class or the Main Activitys onCreate.
     * Use R.raw.webtrekk_config as default configID
     * @param app application instance
     *
     */
    final public void initWebtrekk(final Application app)
    {
        initWebtrekk(app, R.raw.webtrekk_config);
    }

    /**
     * this initializes the webtrekk tracking configuration, it has to be called only once when the
     * application starts, for example in the Application Class or the Main Activitys onCreate.
     * @param app application instance
     * @param configResourceID id of config resource
     *
     */
    final public void initWebtrekk(final Application app, int configResourceID) {
        if (app == null) {
            throw new IllegalArgumentException("no valid app");
        }
        initVersions(app.getApplicationContext());
        initAutoTracking(app);
        initWebtrekk(app.getApplicationContext(), configResourceID);
    }

    /**
     * @deprecated use {@link #initWebtrekk(Application, int)} instead
     * this initializes the webtrekk tracking configuration, it has to be called only once when the
     * application starts, for example in the Application Class or the Main Activitys onCreate.
     * Use R.raw.webtrekk_config as default configID
     * @param c
     *
     */
    void initWebtrekk(final Context c)
    {
        initWebtrekk(c, R.raw.webtrekk_config);
    }

    /**
     * this initializes the webtrekk tracking configuration, it has to be called only once when the
     * application starts, for example in the Application Class or the Main Activitys onCreate
     * @param c the application mContext / mContext of the main activity
     * @param configResourceID resource config ID.
     */
    void initWebtrekk(final Context c, int configResourceID) {
        if (c == null) {
            throw new IllegalArgumentException("no valid mContext");
        }
        if (mIsInitialized) {
            //this can also occur on screen orientation changes
            //TODO: recheck desired behaviour
            return;
            //throw new IllegalStateException("The initWebtrekk method must be called only once");
        }
        this.mContext = c;

        boolean isFirstStart = HelperFunctions.firstStart(mContext);

        initTrackingConfiguration(configResourceID);
        mRequestFactory.init(mContext, trackingConfiguration, this);
        //TODO: make sure this can not break
        //Application act = (Application) mContext.getApplicationContext();
        mExceptionHandler.init(mRequestFactory, mContext);

        mProductListTracker = new ProductListTracker(trackingConfiguration, mContext);


        WebtrekkLogging.log("requestUrlStore created: max requests - " + trackingConfiguration.getMaxRequests());

        WebtrekkLogging.log("tracking initialized");
        mIsInitialized = true;

    }

    /**
     * returns if initWebtrekk was called successfully for this object
     * @return true if initialization was called and false otherwise.
     */
    public boolean isInitialized()
    {
        return mIsInitialized;
    }

    final void initTrackingConfiguration(int configResourceID) {
        initTrackingConfiguration(null, configResourceID);
    }

    /**
     * initializes the tracking configuration, either from xml, or shared prefs, or remote
     * always takes the newest version, and stores it in the shared preferences for future reference
     * in case it can not get a new remote config and remote config is enabled, the old config is used
     *
     * @param configurationString for unit testing only
     */
    final void initTrackingConfiguration(final String configurationString, int configResourceID) {

            // always parse the local raw config version first, this is fallback, default and also the way to fix broken online configs
            //TODO: this could me more elegant by only parsing it when its a new app version which needs to be set anyway
            String trackingConfigurationString;
            String defaultConfigurationString = null;
            if (configurationString == null) {
                try {
                    trackingConfigurationString = HelperFunctions.stringFromStream(mContext.getResources().openRawResource(configResourceID));
                } catch (IOException e) {
                    WebtrekkLogging.log("no custom config was found, illegal state, provide a valid config in res id:"+configResourceID);
                    throw new IllegalStateException("can not load xml configuration file, invalid state");
                }
                if(trackingConfigurationString.length() < 80) {
                    // neccesary to make sure it uses the placeholder which has 66 chars length
                    WebtrekkLogging.log("no custom config was found, illegal state, provide a valid config in res id:"+configResourceID);
                    throw new IllegalStateException("can not load xml configuration file, invalid state");
                }
            } else {
                trackingConfigurationString = configurationString;
            }

        try {
            // parse default configuration without default, will throw exceptions when its not valid
            trackingConfiguration = new TrackingConfigurationXmlParser().parse(trackingConfigurationString);
        } catch (Exception e) {
            throw new IllegalStateException("invalid xml configuration file, invalid state: " + e.getMessage() + "\n"+trackingConfigurationString);
        }

        if(trackingConfiguration != null && trackingConfiguration.isEnableRemoteConfiguration()) {
            SharedPreferences sharedPrefs = HelperFunctions.getWebTrekkSharedPreference(mContext);
            // second check if a newer remote config version is stored locally
            if(sharedPrefs.contains(Webtrekk.PREFERENCE_KEY_CONFIGURATION)) {
                WebtrekkLogging.log("found trackingConfiguration in preferences");
                // in this case we already have a configuration xml stored
                // parse the existing one and check if an update is online available
                trackingConfigurationString = sharedPrefs.getString(Webtrekk.PREFERENCE_KEY_CONFIGURATION, null);
                TrackingConfiguration sharedPreferencetrackingConfiguration = null;
                try {
                    sharedPreferencetrackingConfiguration = new TrackingConfigurationXmlParser().parse(trackingConfigurationString);
                    if(sharedPreferencetrackingConfiguration.getVersion() > trackingConfiguration.getVersion()) {
                        // in this case there is a newer, so replace th old one
                        trackingConfiguration = sharedPreferencetrackingConfiguration;
                    }
                } catch (IOException e) {
                    WebtrekkLogging.log("ioexception parsing the configuration string", e);
                } catch(XmlPullParserException e) {
                    WebtrekkLogging.log("exception parsing the configuration string", e);
                }


            }
            // third check online for newer versions
            //TODO: maybe store just the version number locally in preferences might reduce some parsing
            new TrackingConfigurationDownloadTask(this, null).execute(trackingConfiguration.getTrackingConfigurationUrl());
        }

        // check if we have a valid configuration
        if(trackingConfiguration != null && trackingConfiguration.validateConfiguration()) {
            WebtrekkLogging.log("xml trackingConfiguration value: trackid - " + trackingConfiguration.getTrackId());
            WebtrekkLogging.log("xml trackingConfiguration value: trackdomain - " + trackingConfiguration.getTrackDomain());
            WebtrekkLogging.log("xml trackingConfiguration value: send_delay - " + trackingConfiguration.getSendDelay());

            for(ActivityConfiguration cfg : trackingConfiguration.getActivityConfigurations().values()) {
                WebtrekkLogging.log("xml trackingConfiguration activity for: " + cfg.getClassName() + " mapped to: " + cfg.getMappingName() + " autotracked: " + cfg.isAutoTrack());
            }
        } else {
            WebtrekkLogging.log("error loading the configuration - can not initialize tracking");
            throw new IllegalStateException("could not get valid configuration, invalid state");
        }

        WebtrekkLogging.log("tracking configuration initialized");

    }

    /**
     * this functions enables the automatic activity tracking in case its enabled in the configuration
     * @param app application object of the tracked app, can either be a custom one or required by getApplication()
     */
    void initAutoTracking(Application app){
        if(mActivityStatus == null) {
            WebtrekkLogging.log("enabling callbacks");
            mActivityStatus = new ActivityListener(this);
            mActivityStatus.init(app);
        }
    }


    /**
     * this method immediately stops tracking, for example when a user opted out
     * tracking will be in invalid state, until init is called again
     */
    public void stopTracking() {
        if(mRequestFactory.getRequestUrlStore() != null) {
            mRequestFactory.stopSendURLProcess();
            mRequestFactory.getRequestUrlStore().clearAllTrackingData();
        }
    }

    /**
     * checks if logging is enabled
     * @return boolean if logging is enabled
     */
    public static boolean isLoggingEnabled() {
        return WebtrekkLogging.isLogging();
    }

    /**
     * enables the logging for all SDK log outputs
     * @param logging enables/disables the webtrekk logging
     */
    public static void setLoggingEnabled(boolean logging) {
        WebtrekkLogging.setIsLogging(logging);
    }

    public boolean isSampling() {
        return mRequestFactory.isSampling();
    }

    /**
     * returns the mContext with which the webtrekk instance was initialized, this can not be changed
     *
     * @return mContext
     */
    public Context getContext() {
        return mContext;
    }

    public String getCurrentActivityName() {
        return mRequestFactory.getCurrentActivityName();
    }

    /**
     * @deprecated
     * Don't call this function. If you need override page name call {@link Webtrekk#setCustomPageName(String)} instead
     * @param currentActivityName
     */
    public void setCurrentActivityName(String currentActivityName) {
        mRequestFactory.setCurrentActivityName(currentActivityName);
    }

    /**
     * set custom page name. This name overrides page name that either provided by activity name or
     *set in <mappingname> tag in configuration xml. name is cleaned on next activity start.
     * @param pageName
     */
    public void setCustomPageName(String pageName)
    {
        mRequestFactory.setCustomPageName(pageName);
    }

    /**
     * this is the default tracking method which creates an empty tracking trackingParameter object
     * it only tracks the auto tracked values like lib version, resolution, page name
     */
    public void track() {
        track(new TrackingParameter());
    }

    /**
     * this method gets called when auto tracking is enabled and one of the lifycycle methods is called
     */
    void autoTrackActivity() {
        // only track if auto tracking is enabled for that activity
        // the default value and the activities autoTracked value is based on the global xml settings
        boolean autoTrack = trackingConfiguration.isAutoTracked();
        if(trackingConfiguration.getActivityConfigurations()!= null && trackingConfiguration.getActivityConfigurations().containsKey(mRequestFactory.getCurrentActivityName())) {
            autoTrack = trackingConfiguration.getActivityConfigurations().get(mRequestFactory.getCurrentActivityName()).isAutoTrack();
        }
        if(autoTrack) {
            track();
        }
    }

    /**
     * allows tracking of a requests with a custom set of trackingparams
     *
     * @param tp the TrackingParams for the request
     * @throws IllegalStateException when the SDK has not benn initialized, activity was not started or the trackingParameter are invalid
     */
    public void track(final TrackingParameter tp) {
        if (mRequestFactory.getRequestUrlStore() == null || trackingConfiguration == null) {
            WebtrekkLogging.log("webtrekk has not been initialized");
            return;
        }

        if (mRequestFactory.getCurrentActivityName() == null) {
            WebtrekkLogging.log("no running activity, call startActivity first");
            return;
        }

        if(tp == null) {
            WebtrekkLogging.log("TrackingParams is null");
            return;
        }

        boolean addCDBRequestType = false;

        if (WebtrekkUserParameters.needUpdateCDBRequest(mContext)){

            WebtrekkUserParameters userPar = new WebtrekkUserParameters();

            if (userPar.restoreFromSettings(mContext)){
                tp.add(userPar.getParameters());
                tp.setCustomUserParameters(userPar.getCustomParameters());
                addCDBRequestType = true;
            }
        }

        TrackingRequest request = mRequestFactory.createTrackingRequest(tp);

        if (addCDBRequestType){
            request.setMergedRequest(TrackingRequest.RequestType.CDB);
        }

        mRequestFactory.addRequest(request);
        mRequestFactory.setLasTrackTime(System.currentTimeMillis());
    }

    /**
     * Send CDB request with user parameters. You should use constructor of WebtrekkUserParameters to define any parameters you would like to include
     * For example:
     * webtrekk.track(new WebtrekkUserParameters.</br>
     * setEmail("some email").</br>
     * setPhone("some phone"))
     * @param userParameters - user parameters
     */
    public void track(WebtrekkUserParameters userParameters)
    {
        if (userParameters.saveToSettings(mContext))
            WebtrekkLogging.log("CDB request is received and saved to settings");
        else {
            WebtrekkLogging.log("Nothing to send as request don't have any not null parameters.");
            return;
        }

        TrackingParameter trackingParameter = new TrackingParameter();
        trackingParameter.add(userParameters.getParameters());
        trackingParameter.add(Parameter.EVERID, getEverId());
        trackingParameter.setCustomUserParameters(userParameters.getCustomParameters());

        TrackingRequest request = new TrackingRequest(trackingParameter, trackingConfiguration, TrackingRequest.RequestType.CDB);
        mRequestFactory.addRequest(request);
        WebtrekkLogging.log("CDB request is sent");
        WebtrekkUserParameters.updateCDBRequestDate(mContext);
    }

    /**
     * track exception that is caught by internal application handler.
     * @param ex - caught exception
     */
    public void trackException(Throwable ex)
    {
        mExceptionHandler.trackCatched(ex);
    }

    /**
     * track exception info that user can provide.
     * @param name max 255 characters
     * @param message max 255 characters
     */
    public void trackException(String name, String message) {
        mExceptionHandler.trackInfo(name, message);
    }

    /**
     * this function is be called automatically by activity flow listener
     * @hide
     * */
    @Override
    public void onStart(boolean isRecreationInProcess, ActivityTrackingStatus.STATUS status,
                        long inactivityTime, String activityName) {
        if (mRequestFactory.getRequestUrlStore() == null || trackingConfiguration == null) {
            WebtrekkLogging.log("webtrekk has not been initialized");
            return;
        }

        //reset page URL if activity is changed
        if (!isRecreationInProcess) {
            resetPageURLTrack();
            mRequestFactory.setCurrentActivityName(activityName);
        }

        if(status == ActivityTrackingStatus.STATUS.FIRST_ACTIVITY_STARTED) {
            onFirstActivityStart();
        }

        // track only if it isn't in background and session timeout isn't passed
        if (status == ActivityTrackingStatus.STATUS.RETURNINIG_FROM_BACKGROUND){
            if (inactivityTime > trackingConfiguration.getResendOnStartEventTime())
                mRequestFactory.forceNewSession();
             mRequestFactory.restore();
        }

        autoTrackActivity();
    }

    /**
     * this method gets called when the first activity of the application has started
     * it loads the old requests from the backupfile and tries to send them
     *@hide
     */
    private void onFirstActivityStart() {
        mRequestFactory.onFirstStart();
        mRequestFactory.onSendIntervalOver();
    }

    /**
     * this function is be called automatically by activity flow listener
     * open activities and knows when to exit
     * @hide
     */
    @Override
    public void onStop(ActivityTrackingStatus.STATUS status) {
        if (mRequestFactory.getRequestUrlStore() == null || trackingConfiguration == null) {
            throw new IllegalStateException("webtrekk has not been initialized");
        }

        switch (status)
        {
            case SHUT_DOWNING:
                stop();
                break;
            case GOING_TO_BACKGROUND:
                flush();
                break;
        }
    }

    /**
     * @hide
     * Is called when activity is destroyed to double check that queries are saved and threads are stopped
     * as in some cases stop isn't called during application showt down.
     */
    @Override
    public void onDestroy(ActivityTrackingStatus.STATUS status)
    {
        if(status == ActivityTrackingStatus.STATUS.SHUT_DOWNING) {
            stop();
        }
    }

    /**
     * this method gets called when application is going to be closed
     * it stores all requests to file and stop threads.
     * @hide
     */
    void stop() {
        mRequestFactory.stop();
    }

    /**
     * this method gets called when application is going to background
     * it stores all requests to file.
     */
    private void flush() {
        mRequestFactory.flush();
    }

    void setContext(Context context) {
        this.mContext = context;
    }

    RequestFactory getRequestFactory()
    {
        return mRequestFactory;
    }


    public boolean isOptout() {
        return mRequestFactory.isOptout();
    }


    /**
     * this method is for the opt out switch, when called it will set the shared preferences of opt out
     * and also stops tracking in case the user opts out
     * @param value boolean value indicating if the user opted out or not
     */
    public void setOptout(boolean value) {
        mRequestFactory.setIsOptout(value);
    }

    /**
     * Set url for tracking for each activity. This value is reset for each new activity
     * Value is override PAGE_URL parameter in activity configuration if any.
     * @param url
     * @return
     */

    public boolean setPageURL(String url)
    {
        ActivityConfiguration acConf = trackingConfiguration.getActivityConfigurations().get(mRequestFactory.getCurrentActivityName());

        if (!HelperFunctions.testIsValidURL(url)) {
            WebtrekkLogging.log("setPageURL. Invalid url format");
            return false;
        }else {

            if (acConf != null) {
                acConf.setOverridenPageURL(url);
                return true;
            } else {
                WebtrekkLogging.log("setPageURL. Activity configuration isn't defined.");
                return false;
            }
        }
    }

    //reset overrated URLTrack
    private void resetPageURLTrack()
    {
        ActivityConfiguration acConf = trackingConfiguration.getActivityConfigurations().get(mRequestFactory.getCurrentActivityName());

        if (acConf != null)
            acConf.resetOverridenPageURL();
    }

    /**
     * Retruns recommendation object that can be used to query recommendation(s)
     * Each time method returns new instance of recommendation object that is initialized accornding to
     * configuration xml. Using WebtrekkRecommendations object you can have independed several recommendation
     * request.
     * @return WebtrekkRecommendations object
     */
    public WebtrekkRecommendations getRecommendations()
    {
        return new WebtrekkRecommendations(trackingConfiguration, mContext);
    }


    public ProductListTracker getProductListTracker(){
        return mProductListTracker;
    }

    /**
     * Send manual tracks to server from tracks queue. Is done in separate thread and can be called from UI thread.
     * It must be called when <sendDelay> is zero, otherwise no message is sent to server.
     * @return true if sending is called and false if previous send procedure hasn't called or nothing to send
     *              or manual send mode is off (<sendDelay> not zero).
     */
    public boolean send() {
        if (mRequestFactory.getTrackingConfiguration().getSendDelay() == 0) {
            return mRequestFactory.onSendIntervalOver();
        }else {
            WebtrekkLogging.log("Custom url send mode isn't switched on. Send isn't available. For custom send mode set <sendDelay> to zero ");
            return false;
        }
    }

    /**
     * allows to set global tracking parameter which will be added to all requests
     * @return
     */
    public TrackingParameter getGlobalTrackingParameter() {
        return mRequestFactory.getGlobalTrackingParameter();
    }

    public void setGlobalTrackingParameter(TrackingParameter globalTrackingParameter) {
        mRequestFactory.setGlobalTrackingParameter(globalTrackingParameter);
    }

    public TrackingParameter getConstGlobalTrackingParameter() {
        return mRequestFactory.getConstGlobalTrackingParameter();
    }

    public void setConstGlobalTrackingParameter(TrackingParameter constGlobalTrackingParameter) {
        mRequestFactory.setConstGlobalTrackingParameter(constGlobalTrackingParameter);
    }

    /**
     * this method allows the customer to access the custom parameters which will be replaced by the mapping lter
     *
     * @return
     */
    public Map<String, String> getCustomParameter() {
        return mRequestFactory.getCustomParameter();
    }

    /**
     * this method alles the customer to set the custom parameters map
     *
     */
    public void setCustomParameter(Map<String, String> customParameter) {
        mRequestFactory.setCustomParameter(customParameter);
    }

    /**
     *Returns current EverId. EverId is generated automatically by SDK, but you can set is manual as well.
     * @return current EverId
     */
    public String getEverId() {
        return HelperFunctions.getEverId(mContext);
    }

    /**
     * Returns Tracking ID list defined in configuration xml. In most cased it is one item,
     * but theoretically it can be list divided by commas. null if Webtrekk is not initialized.
     * @return current Tracking ID
     */
    public List<String> getTrackingIDs() {
        if (trackingConfiguration == null){
            WebtrekkLogging.log("webtrekk has not been initialized");
            return null;
        }else {
            return Arrays.asList(trackingConfiguration.getTrackId().split(","));
        }
    }

    /**
     * set EverId. This ever ID will be used for all tracking request until application reinstall.
     * @param everId - 19 digits string value
     */
    public void setEverId(String everId)
    {
        if (mContext == null)
        {
            WebtrekkLogging.log("Can't set ever id. Please initialize SDK first.");
            return;
        }

        if (!everId.matches("\\d{19}"))
        {
            WebtrekkLogging.log("Incorrect everID should have 19 digits");
            return;
        }

        HelperFunctions.setEverId(mContext, everId);
        mRequestFactory.initEverID();
    }

    /**
     * Set deeplink attribution media code. This media code will be sent once with the next tracking request
     * @param mediaCode - media code
     */
    public void setMediaCode(String mediaCode)
    {
        if (mContext == null)
        {
            WebtrekkLogging.log("Can't set media code. Please initialize SDK first.");
            return;
        }

        HelperFunctions.setDeepLinkMediaCode(mContext, mediaCode);
    }

    /**
     * Use this function to establish user connection between App and Web tracking. In that case
     * Webtrekk use everId from App to track website that is opened by WebView. Please note that website
     * should have Webtrekk integrated as well with PIXEL minimum version 4.4.0.
     * This function should be called before first main WebView page loading and in UI thread.
     *
     * @param webView - webView instance can't be null
     */
    public void setupWebView(WebView webView){
        if (mContext == null){
            WebtrekkLogging.log("Can't setup WebView. Please initialize SDK first.");
            return;
        }

        if (webView == null){
            WebtrekkLogging.log("Can't setup WebView. WebView parameter is null.");
            return;
        }

        if (Looper.getMainLooper().getThread() != Thread.currentThread()){
            WebtrekkLogging.log("Can't setup WebView. Function should be called from UI thread.");
            return;
        }

        final String everId = HelperFunctions.getEverId(mContext);;

        webView.loadUrl("javascript: var webtrekkApplicationEverId = \"" + everId + "\";");
    }

    /**
     * @hide
     * @return
     */
    public TrackingConfiguration getTrackingConfiguration() {
        return trackingConfiguration;
    }

    /**
     * @hide
     * @param trackingConfiguration
     */
    public void setTrackingConfiguration(TrackingConfiguration trackingConfiguration) {
        this.trackingConfiguration = trackingConfiguration;
        mRequestFactory.setTrackingConfiguration(trackingConfiguration);
    }

    /**
     * @hide
     * @param context
     */
    private void initVersions(Context context)
    {
        mTrackingLibraryVersionUI = context.getResources().getString(R.string.version_name);
        mTrackingLibraryVersion = mTrackingLibraryVersionUI.replaceAll("\\D","");
    }

    /**
     * for unit testing in the application and debugging
     */
    public int getVersion() { return trackingConfiguration.getVersion(); }
    public String getTrackDomain() { return trackingConfiguration.getTrackDomain(); }
    public int getSampling() { return trackingConfiguration.getSampling(); }
    public void setIsSampling(boolean isSampling ) { mRequestFactory.setIsSampling(isSampling); }
    public int getSendDelay() { return trackingConfiguration.getSendDelay(); }
    public int getResendOnStartEventTime() { return trackingConfiguration.getResendOnStartEventTime(); }
    public int getMaxRequests() { return trackingConfiguration.getMaxRequests(); }
    public String getTrackingConfigurationUrl() { return trackingConfiguration.getTrackingConfigurationUrl(); }
    public boolean isAutoTracked() { return trackingConfiguration.isAutoTracked(); }
    public boolean isAutoTrackApiLevel() { return trackingConfiguration.isAutoTrackApiLevel(); }
    public boolean isEnableRemoteConfiguration() { return trackingConfiguration.isEnableRemoteConfiguration(); }

    /**
     * @deprecated use {@link #getTrackingIDs()} instead
     * @return
     */
    public String getTrackId(){
        return getTrackingIDs().get(0);
    }
}
