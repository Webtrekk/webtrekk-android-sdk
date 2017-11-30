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
 * Created by Arsen Vartbaronov on 23.06.17.
 */

package com.Webtrekk.SDKTest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.test.InstrumentationRegistry;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import junit.framework.Assert;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by vartbaronov on 22.06.17.
 */

public class WebtrekkBaseSDKTest extends Assert implements WebtrekkTestRule.TestAdapter {
    protected Application mApplication;
    private SDKInstanceManager mSDKManager = new SDKInstanceManager();
    protected boolean mIsErrorHandlerTest;
    protected boolean mIsExternalCall;
    static private String IS_EXTERNAL = "external";
    protected boolean mIsCDBTestRequest;
    public static String mTestName;
    private PowerManager.WakeLock mWakeLock;

    public void before() throws Exception {

        //super.setUp();
        //refresh webtrekk instance
        setupWTInstance();
        mApplication = getApplication();
        if (!mIsErrorHandlerTest)
            deleteErrorHandlerFile(mApplication);
        if (!mIsCDBTestRequest)
            deleteCDBRepeatRequestInfo();

        Bundle arguments = null;
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            arguments = InstrumentationRegistry.getArguments();
        }else {
            arguments = (Bundle)getFieldValue(instrumentation, "mArguments");
        }
        if (arguments.size() > 0)
            WebtrekkLogging.log("Receive arguments for test:"+arguments);
        mIsExternalCall = arguments.getString(IS_EXTERNAL) != null;

        setupWakeUp();
    }

    public void after() throws Exception {
        releaseWTInstance();
        releaseWakeUp();
    }

    protected void setupWTInstance(){
        mSDKManager.setup();
    }

    protected void releaseWTInstance(){
        mSDKManager.release(mApplication);
    }

    private void setupWakeUp(){
        PowerManager powerManager = (PowerManager)mApplication.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        mWakeLock.acquire();
    }

    private void releaseWakeUp(){
        if (mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    protected void deleteErrorHandlerFile(Context context)
    {
        File loadFile = new File(context.getFilesDir().getPath() + File.separator+"exception.txt");
        loadFile.delete();
    }

    static public void finishActivitySync(Activity activity, boolean doFinish)
    {
        finishActivitySync(activity, InstrumentationRegistry.getInstrumentation(), doFinish);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    static public void finishActivitySync(Activity activity, Instrumentation instrumentation, boolean doFinish)
    {
        if (doFinish)
            activity.finish();
        //give activity one minute to finish
        long currentTime = System.currentTimeMillis();
        boolean finishTimeout = false;
        int activityHash = activity.hashCode();
        boolean isDestroyed = false;

        while (!isDestroyed && !finishTimeout) {
            instrumentation.waitForIdleSync();
            finishTimeout = (System.currentTimeMillis() - currentTime) > 140000;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isDestroyed = activity.isDestroyed();
            }else {
                isDestroyed = (Boolean)callMethod(null, activity.getWindow(), "isDestroyed", null);
            }
        }

        if (finishTimeout) {
            WebtrekkLogging.log("finishActivitySync: finished by timeout. Hash:" + activityHash);
        }
    }

    private void deleteCDBRepeatRequestInfo()
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(mApplication);

        if (preferences.contains("LAST_CBD_REQUEST_DATE"))
            preferences.edit().remove("LAST_CBD_REQUEST_DATE").apply();
    }

    static private Object getFieldValue(Object instance, String valueName) {
        Field field = null;
        try {
            field = instance.getClass().getDeclaredField(valueName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    static private <T extends Object> T callMethod(String className, Object classInstance, String methodName, Class[] argumentsTypes){
        try {
            Class classObj = classInstance == null ? Class.forName(className) : classInstance.getClass();

            Method method = classObj.getMethod(methodName, argumentsTypes);

            return (T) method.invoke(classInstance);
        }catch (InvocationTargetException e) {
            return null;
        } catch (Exception e){
            return null;
        }
    }

    protected boolean isRestrictedMode(){
        Context context = mApplication;

        return context.getResources().getBoolean(R.bool.is_restricted_mode);
    }


    protected void cleanConfigPreference()
    {
        SharedPreferences sharedPrefs = HelperFunctions.getWebTrekkSharedPreference(mApplication);
        sharedPrefs.edit().remove(Webtrekk.PREFERENCE_KEY_CONFIGURATION).apply();
    }

    protected Instrumentation getInstrumentation(){
        return InstrumentationRegistry.getInstrumentation();
    }

    protected Application getApplication(){
        return (Application)InstrumentationRegistry.getTargetContext().getApplicationContext();
    }

}
