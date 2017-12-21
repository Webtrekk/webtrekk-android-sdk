package com.Webtrekk.SDKTest;

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
 * Created by vartbaronov on 20.12.17.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import java.io.EOFException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class PostInstallSender {

    final Context applicationContext;
    private CompleteNotifier notifier;

    interface CompleteNotifier{
        void complete(boolean isSuccessful);
    }

    PostInstallSender(Context applicationContext){
        this.applicationContext = applicationContext;
    }

    void send(@NonNull String mediaCode, @NonNull CompleteNotifier completeCallback){
        send (mediaCode, completeCallback, null);
    }

    void send(@NonNull String mediaCode){
        send (mediaCode, null, null);
    }

    void send(@NonNull final String mediaCode, final @Nullable CompleteNotifier completeCallback, @Nullable final String advId){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String request = getRequest(mediaCode, advId);
                        WebtrekkLogging.log("send request:"+ request);
                        notifier = completeCallback;
                        sendRequest(new URL(request));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                };
            }).start();
    }

    @NonNull
    private String getRequest(@NonNull String mediaCode, @Nullable String advId){
        Webtrekk wt = Webtrekk.getInstance();
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(applicationContext);
        return "http://appinstall.webtrekk.net/appinstall/v1/postback?trackid="+
                wt.getTrackingIDs().get(0)+"&mc="+mediaCode+"&app_name=null" +
                (advId == null ? "" : "&aid=" + advId);
    }


    private boolean sendRequest(URL url) throws InterruptedException {
        HttpURLConnection connection = null;
        boolean result = false;
        try {
            connection = (HttpURLConnection) url.openConnection();

            if (Thread.interrupted())
                throw new InterruptedException();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.setUseCaches(false);
            connection.connect();
            int statusCode = connection.getResponseCode();

            if (statusCode != 200){
                WebtrekkLogging.log("Post install request failed with status code:" + statusCode);
            }
            result = true;
        } catch (EOFException e) {
            WebtrekkLogging.log("RequestProcessor: EOF > Will retry later.", e);
        } catch (SocketTimeoutException e) {
            WebtrekkLogging.log("RequestProcessor: SocketTimeout > Will retry later.", e);
        } catch (SocketException e) {
            WebtrekkLogging.log("RequestProcessor: Socket Exception.", e);
        }  catch (UnknownHostException e) {
            WebtrekkLogging.log("RequestProcessor: UnknownHost > Will retry later.", e);
        } catch (IOException e) {
            WebtrekkLogging.log("io exception: can not connect to host", e);
            WebtrekkLogging.log("RequestProcessor: IO > Removing URL from queue because exception cannot be handled.", e);
        } catch (InterruptedException e) {
        } catch (Exception e) {
            // we don't know how to resolve these - cannot retry
            WebtrekkLogging.log("RequestProcessor: Removing URL from queue because exception cannot be handled.", e);
            // IllegalStateException by setrequestproperty in case the connectin is already established
            // NPE
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        if (notifier != null){
            notifier.complete(result);
        }

        return result;
    }
}
