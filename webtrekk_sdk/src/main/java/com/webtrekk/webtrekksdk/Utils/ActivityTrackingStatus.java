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
 * Created by Arsen Vartbaronov on 06.05.16.
 */

package com.webtrekk.webtrekksdk.Utils;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Need to calculate status of application
 * @hide
 */
public class ActivityTrackingStatus implements Application.ActivityLifecycleCallbacks
{
    public enum STATUS
    {
        NO_ACTIVITY_IS_RUNNING,
        FIRST_ACTIVITY_STARTED,
        ACTIVITY_IS_SHOWN,
        RETURNINIG_FROM_BACKGROUND,
        GOING_TO_BACKGROUND,
        SHUT_DOWNING
    }

    STATUS mCurrentStatus = STATUS.NO_ACTIVITY_IS_RUNNING;
    String mCurrentActivityName;
    private WeakReference<Activity> mCurrentActivityInstance;
    private Configuration mLatestConfiguration;

    volatile private int mCurrentActivitiesCount;
    final private Deque<String> mPreviousActivitiesQueue = new LinkedList<>();
    private String mFirstActivityName;
    private long mLastActivityVisibleTime;
    private long mReturnFromBackgroundTime;

    private boolean mIsActivityRestored;
    boolean mIsConfigurationChanged;

    /**
     * @hide
     * for unit test only
     * @return number of showed activities.
     */
    public int getCurrentActivitiesCount() {
        return mCurrentActivitiesCount;
    }

    /**
     *
     * @return time in seconds when application was in background. Should ba called when status is RETURNINIG_FROM_BACKGROUND
     */
    protected long getInactivityApplicaitonTime()
    {
        return (mReturnFromBackgroundTime - mLastActivityVisibleTime)/1000;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        WebtrekkLogging.log("Tracking Activity Created: "+getActivityName(activity) + " instance hash:" + activity.hashCode() + (savedInstanceState != null ? " as recreation":""));
        WebtrekkLogging.log("CurrentStatus before:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());

        Configuration currentConfiguration = getCurrentConfiguration(activity);
        mIsConfigurationChanged = mLatestConfiguration != null && !currentConfiguration.equals(mLatestConfiguration);

        if (mIsConfigurationChanged) {
            mLatestConfiguration = new Configuration(currentConfiguration);
        }

        mIsActivityRestored = savedInstanceState != null;
        if (!mIsActivityRestored) {
            if (mCurrentActivityName != null) {
                mPreviousActivitiesQueue.offerFirst(mCurrentActivityName);
            }
        }

        if (!mIsConfigurationChanged){
            mCurrentActivityName = getActivityName(activity);
        }

        mCurrentActivityInstance = new WeakReference<>(activity);

        if (!mIsActivityRestored)
            mCurrentActivitiesCount++;

        WebtrekkLogging.log("Configuration is changed:"+mIsConfigurationChanged);
        WebtrekkLogging.log("CurrentStatus after:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        WebtrekkLogging.log("Tracking Activity started: " + getActivityName(activity) + " instance hash:" + activity.hashCode());
        WebtrekkLogging.log("CurrentStatus before:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());

        //this is first start, but initialization was done in onCreate of MainActivity
        if (!mIsActivityRestored && mCurrentActivitiesCount == 0)
            mCurrentActivitiesCount = 1;

        if (mCurrentActivitiesCount == 1 && mCurrentStatus == STATUS.NO_ACTIVITY_IS_RUNNING) {
            mCurrentStatus = STATUS.FIRST_ACTIVITY_STARTED;
            if (mCurrentActivityName == null) {
                mCurrentActivityName = getActivityName(activity);
                mCurrentActivityInstance = new WeakReference<>(activity);
            }
            mFirstActivityName = mCurrentActivityName;
            if (mLatestConfiguration == null) {
                mLatestConfiguration = new Configuration(getCurrentConfiguration(activity));
            }
        }else if (mCurrentStatus == STATUS.GOING_TO_BACKGROUND && getActivityName(activity).equals(mCurrentActivityName))
        {
            mCurrentStatus = STATUS.RETURNINIG_FROM_BACKGROUND;
            mReturnFromBackgroundTime = System.currentTimeMillis();
        }

        WebtrekkLogging.log("CurrentStatus after:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        WebtrekkLogging.log("Tracking Activity Resumed: " + getActivityName(activity) + " instance hash:" + activity.hashCode() + (mIsActivityRestored ? " as recreation":""));
        WebtrekkLogging.log("CurrentStatus before:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
        mIsActivityRestored = false;
        mIsConfigurationChanged = false;
        mCurrentStatus = STATUS.ACTIVITY_IS_SHOWN;
        WebtrekkLogging.log("CurrentStatus after:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
    }

    @Override
    public void onActivityPaused(Activity activity) {
      WebtrekkLogging.log("Tracking Activity Paused: " + getActivityName(activity));

        WebtrekkLogging.log("CurrentStatus before:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " instance hash:" + activity.hashCode() + " Previous Activity:"+mPreviousActivitiesQueue.peek());
        if (activity.isFinishing() && mCurrentActivityName != null && mCurrentActivityName.equals(getActivityName(activity))) {
            mCurrentActivityName = mPreviousActivitiesQueue.pollFirst();
            mCurrentActivityInstance = null;
        }

        WebtrekkLogging.log("CurrentStatus after:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        WebtrekkLogging.log("Tracking Activity stopped: " + getActivityName(activity) + " instance hash:" + activity.hashCode() + (activity.isFinishing() ? " as finishing" : " as sleeping"));
        WebtrekkLogging.log("CurrentStatus before:"+mCurrentStatus+ " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
        if (activity.isFinishing()) {
            //activity is finishing increase activity counter and determine applicaiton shut down
            mCurrentActivitiesCount--;
            if (mCurrentActivitiesCount == 0 || (mFirstActivityName != null && mFirstActivityName.equals(getActivityName(activity))))
                mCurrentStatus = STATUS.SHUT_DOWNING;
        }else
        {
            // if this is not finishing activity and there is no any new activity started that is go to background process
            String stoppedActivity = getActivityName(activity);
            if (stoppedActivity.equals(mCurrentActivityName) && !activity.isChangingConfigurations() && (mCurrentActivityInstance == null || activity == mCurrentActivityInstance.get()))
            {
                mCurrentStatus = STATUS.GOING_TO_BACKGROUND;
                mLastActivityVisibleTime = System.currentTimeMillis();
            }
        }
        WebtrekkLogging.log("CurrentStatus after:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        WebtrekkLogging.log("Tracking Activity SaveInstance: " + getActivityName(activity) + (outState != null ? " as recreation":""));
        WebtrekkLogging.log("CurrentStatus:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        WebtrekkLogging.log("Tracking Activity Destroyed: " + getActivityName(activity) + " instance hash:" + activity.hashCode() + (activity.isFinishing() ? " as finishing" : " as sleeping"));
        WebtrekkLogging.log("CurrentStatus before:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());

        if (activity.isFinishing() && mCurrentStatus != STATUS.SHUT_DOWNING &&
                    (mCurrentActivitiesCount == 0 || (mFirstActivityName != null && mFirstActivityName.equals(getActivityName(activity))))) {
                mCurrentStatus = STATUS.SHUT_DOWNING;
        }
        WebtrekkLogging.log("CurrentStatus after:"+mCurrentStatus + " Current Activity:"+mCurrentActivityName + " Previous Activity:"+mPreviousActivitiesQueue.peek());
    }

    private String getActivityName(Activity activity)
    {
        return activity.getClass().getName();
    }

    private Configuration getCurrentConfiguration(Activity activity){
        return activity.getResources().getConfiguration();
    }
}
