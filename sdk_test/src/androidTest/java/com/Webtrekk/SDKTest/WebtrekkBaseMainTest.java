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

import android.support.test.InstrumentationRegistry;

import com.Webtrekk.SDKTest.SimpleHTTPServer.HttpServer;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.Subject;

/**
 * Created by vartbaronov on 22.06.17.
 */
public class WebtrekkBaseMainTest extends WebtrekkBaseSDKTest {

    private List<String> mSentURLArray = new Vector<>();
    protected long mWaitMilliseconds = 12000;
    protected HttpServer mHttpServer;
    volatile long mStringNumbersToWait = 1;
    private long mStartMessageReceiveNumber;
    private Subject<String> mSubject;
    private Iterator<String> mIterator;
    private boolean mIsNoTrackCheck;
    static final private String TIMEOUT = "TIMEOUT";

    interface StopListenForURLCallback{
        boolean stop(String url);
    }


    @Override
    public void before() throws Exception {
        super.before();
        //refresh webtrekk instance
        if (mHttpServer == null) {
            mHttpServer = new HttpServer();
            mHttpServer.setContext(mApplication);
            mHttpServer.start();
            WebtrekkLogging.log("subject created");
            mSubject = mHttpServer.getSubject();
        }
    }

    @Override
    public void after() throws Exception {
        mIterator = null;
        mSubject.onComplete();
        mHttpServer.stop();
        super.after();
    }

    protected void initWaitingForTrack(Runnable process)
    {
        initWaitingForTrack(process, 1, false);
    }

    protected void initWaitingForTrack(Runnable process, boolean isNoTrackCheck)
    {
        initWaitingForTrack(process, 1, isNoTrackCheck);
    }

    protected void initWaitingForTrack(Runnable process, long UrlCount){
        initWaitingForTrack(process, UrlCount, false);
    }

    protected void initWaitingForTrack(Runnable process, long UrlCount, final boolean isNoTrackCheck)
    {
        mStringNumbersToWait = UrlCount;
        mSentURLArray.clear();
        mIsNoTrackCheck = isNoTrackCheck;

        updateIterator();

        if (process != null) {
            synchronized (Webtrekk.getInstance()) {

                new Thread(process).start();
            }
        }
    }

    final protected String waitForTrackedURL()
    {
        processWaitForURL(null);
        return mIsNoTrackCheck ? null : mSentURLArray.get(0);
    }

    final protected List<String> waitForTrackedURLs(){
        return waitForTrackedURLs(null);
    }

    final protected List<String> waitForTrackedURLs(StopListenForURLCallback callback)
    {
        processWaitForURL(callback);
        return mSentURLArray;
    }

    private void processWaitForURL(StopListenForURLCallback callback)
    {
        WebtrekkLogging.log("start wait process with iterator/hasNext" + (mIterator == null ? "null" : "mIterator.hasNext()"));
        while (mIterator != null && mIterator.hasNext()){
            final String url = mIterator.next();
            if (!url.equals(TIMEOUT)) {
                if (mIsNoTrackCheck){
                    assertEquals(false, "url received in no track mode");
                } else {
                    mSentURLArray.add(url);
                }
            } else { // timeout
                mIterator = null;
            }
            if (callback != null)
            {
                if (callback.stop(url)) {
                    break;
                }
            }else  if (mStringNumbersToWait == mSentURLArray.size()){
                break;
            }
        }
    }

    private void updateIterator(){
        if (mIterator != null){
            return;
        }

        mIterator = mSubject.timeout(mWaitMilliseconds, TimeUnit.MILLISECONDS)
                .onErrorReturn(new Function<Throwable, String>() {
                    @Override
                    public String apply(@NonNull Throwable throwable) throws Exception {
                        assertEquals("track receive error",
                                mIsNoTrackCheck ? 0 : mStringNumbersToWait, mSentURLArray.size());
                        return TIMEOUT;
                    }
                }).blockingIterable().iterator();
    }

    protected void setStartMessageNumber()
    {
        mStartMessageReceiveNumber = mHttpServer.getCurrentRequestNumber();
    }

    protected void waitForMessages(long messageCount)
    {
        while((mHttpServer.getCurrentRequestNumber() - mStartMessageReceiveNumber) != messageCount)
        {
            Thread.yield();
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        }
    }
}
