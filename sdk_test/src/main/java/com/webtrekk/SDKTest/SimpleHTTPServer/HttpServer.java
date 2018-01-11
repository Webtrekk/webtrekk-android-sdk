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
 * Created by Arsen Vartbaronov on 09.05.16.
 */

package com.webtrekk.SDKTest.SimpleHTTPServer;

import android.content.Context;
import android.content.SharedPreferences;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import java.io.IOException;

import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

public class HttpServer extends NanoHTTPD {
    private final static int PORT = 8080;
    private Context mContext;
    static private String REQUEST_COUNT_VALUE = "com.webtrekk.webtrekksdk.Test.RequestCount";
    volatile private long mDelayAfterReceive;
    volatile private long mDelayBeforeReceive;
    final private Object mDelayMonitor = new Object();
    private boolean mIsDelay;
    private Subject<String> mSubject;

    public HttpServer() throws IOException {
        super(PORT);
    }

    public void setContext(Context context)
    {
        mContext = context;
    }

    public void setDelay(int delay)
    {
        mDelayAfterReceive = delay;
    }

    public void setBeforeDelay(int delay)
    {
        mDelayBeforeReceive = delay;
    }

    public Subject<String> getSubject(){
        mSubject =  ReplaySubject.create();
        return mSubject;
    }

    @Override
    public Response serve(IHTTPSession session) {

        try {
            if (mDelayBeforeReceive > 0)
            {
                mIsDelay = true;
                synchronized (mDelayMonitor) {
                    long currentStartTime = System.currentTimeMillis();
                    while (mIsDelay && (currentStartTime + mDelayBeforeReceive) > System.currentTimeMillis()) {
                        mDelayMonitor.wait(mDelayBeforeReceive);
                    }
                }
            }
        } catch (InterruptedException e) {
        }

        String requestURL = "http://"+session.getRemoteHostName()+session.getUri()+"?"+session.getQueryParameterString();
        WebtrekkLogging.log("receive request("+getCurrentRequestNumber()+"):" + requestURL);
        mSubject.onNext(requestURL);
        Response response = new Response(Response.Status.OK, "image/gif;charset=UTF-8", null, 0);
        response.closeConnection(true);

        incrementRequestNumber();

        try {
            if (mDelayAfterReceive > 0)
            {
                Thread.sleep(mDelayAfterReceive);
            }
        } catch (InterruptedException e) {
        }

        return response;
    }

    synchronized public long getCurrentRequestNumber()
    {
        if (mContext == null)
        {
            WebtrekkLogging.log("Error. Context is null. No operation with context for HTTP serer");
            return -1;
        }

        SharedPreferences pref = HelperFunctions.getWebTrekkSharedPreference(mContext);

        return pref.getLong(REQUEST_COUNT_VALUE, 0);
    }

    synchronized public void incrementRequestNumber()
    {
        if (mContext == null)
        {
            WebtrekkLogging.log("Error. Context is null. No operation with context for HTTP serer");
            return;
        }

        SharedPreferences pref = HelperFunctions.getWebTrekkSharedPreference(mContext);

        pref.edit().putLong(REQUEST_COUNT_VALUE, pref.getLong(REQUEST_COUNT_VALUE, 0)+1).apply();
    }

    public void resetRequestNumber()
    {
        if (mContext == null)
        {
            WebtrekkLogging.log("Error. Context is null. No operation with context for HTTP serer");
            return;
        }

        SharedPreferences pref = HelperFunctions.getWebTrekkSharedPreference(mContext);

        pref.edit().putLong(REQUEST_COUNT_VALUE, 0).apply();
    }

    public void stopBeforeDelay(){

        synchronized (mDelayMonitor) {
            mIsDelay = false;
            mDelayMonitor.notifyAll();
        }
    }
}