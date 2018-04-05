package com.webtrekk.webtrekksdk.Utils;

import android.app.Activity;
import android.app.Application;

/**
 * Created by vartbaronov on 26.09.17.
 */

public class ActivityListener extends ActivityTrackingStatus {

    public interface Callback{
        void onStart(boolean isRecreationProcess, STATUS status, long inactivityTime,
                     String activityName);
        void onStop(STATUS status);
        void onDestroy(STATUS status);
    }

    final Callback mCallback;

    public ActivityListener(Callback callback){
        super();
        mCallback = callback;
    }

    public void init(Application application){
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        super.onActivityStarted(activity);
        mCallback.onStart(mIsConfigurationChanged, mCurrentStatus, getInactivityApplicaitonTime(), mCurrentActivityName);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        super.onActivityStopped(activity);
        mCallback.onStop(mCurrentStatus);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        super.onActivityDestroyed(activity);
        mCallback.onDestroy(mCurrentStatus);
    }

}
