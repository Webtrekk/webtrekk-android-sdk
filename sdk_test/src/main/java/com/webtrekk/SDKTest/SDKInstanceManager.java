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
 * Created by Arsen Vartbaronov on 23.02.17.
 */

package com.webtrekk.SDKTest;

import android.app.Activity;
import android.app.Application;

import com.webtrekk.webtrekksdk.Request.RequestFactory;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledExecutorService;

public class SDKInstanceManager {
    private Thread.UncaughtExceptionHandler mOldHandler;

    public void setup(){
        mOldHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void release(Application application){
        Webtrekk webtrekk = Webtrekk.getInstance();
        if (webtrekk.isInitialized())
            webtrekk.stopTracking();
        Thread.setDefaultUncaughtExceptionHandler(mOldHandler);
        stopSendThread(application);
        unregisterActivityEventCallback(application);
        refreshWTInstance();
    }

    private void refreshWTInstance() {
        Class<Webtrekk> wtClass = Webtrekk.class;
        try {
            for (Class<?> classObj : wtClass.getDeclaredClasses()) {
                if (classObj.getName().contains("SingletonHolder")) {
                    Field field = null;
                    field = classObj.getDeclaredField("webtrekk");
                    field.setAccessible(true);
                    Constructor<Webtrekk> wtConstr = wtClass.getDeclaredConstructor();
                    wtConstr.setAccessible(true);
                    field.set(null, wtConstr.newInstance());
                }
            }
        } catch (NoSuchFieldException e) {
            WebtrekkLogging.log("Can't refresh Webtrekk instance");
        } catch (NoSuchMethodException e) {
            WebtrekkLogging.log("Can't refresh Webtrekk instance");
        } catch (InstantiationException e) {
            WebtrekkLogging.log("Can't refresh Webtrekk instance");
        } catch (IllegalAccessException e) {
            WebtrekkLogging.log("Can't refresh Webtrekk instance");
        } catch (InvocationTargetException e) {
            WebtrekkLogging.log("Can't refresh Webtrekk instance");
        }
    }

    private void stopSendThread(Application application)
    {
        if (application == null)
        {
            WebtrekkLogging.log("Error unregister callback. Application reference is null");
            return;
        }

        if (!Webtrekk.getInstance().isInitialized())
        {
            WebtrekkLogging.log("Error unregister callback. Webtrekk isn't initialized");
            return;
        }
        Webtrekk webtrekk = Webtrekk.getInstance();

        RequestFactory requestFactory = (RequestFactory)returnHiddenField(webtrekk, "mRequestFactory");
        ScheduledExecutorService threadService1 = (ScheduledExecutorService)returnHiddenField(requestFactory, "mURLSendTimerService");
        ScheduledExecutorService threadService2 = (ScheduledExecutorService)returnHiddenField(requestFactory, "mFlashTimerService");
        threadService1.shutdownNow();
        threadService2.shutdownNow();
    }

    private void unregisterActivityEventCallback(Application application)
    {
        Webtrekk webtrekk = Webtrekk.getInstance();

        Application.ActivityLifecycleCallbacks callbacks = (Application.ActivityLifecycleCallbacks)returnHiddenField(webtrekk, "mActivityStatus");
        if (callbacks != null)
        {
            application.unregisterActivityLifecycleCallbacks(callbacks);
        }
    }

    static private boolean isActivityStopped(Activity activity)
    {
        return (Boolean)returnHiddenField(activity, "mStopped");
    }

    static private boolean isActivityResumed(Activity activity)
    {
        return (Boolean)returnHiddenField(activity, "mResumed");
    }

    static private Object returnHiddenField(Object object, String fieldName)
    {
        Object result = null;
        try {
            Field callbackField = object.getClass().getDeclaredField(fieldName);
            callbackField.setAccessible(true);
            result = callbackField.get(object);
        } catch (NoSuchFieldException e) {
            WebtrekkLogging.log("Can't remove activity callback");
        } catch (IllegalAccessException e) {
            WebtrekkLogging.log("Can't remove activity callback");
        }

        return result;
    }
}
