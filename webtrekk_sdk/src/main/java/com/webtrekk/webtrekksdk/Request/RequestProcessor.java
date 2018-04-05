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
 * Created by Thomas Dahlmann on 17.09.16.
 */

package com.webtrekk.webtrekksdk.Request;

import android.util.Log;

import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import java.io.EOFException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.observers.DisposableObserver;

/**
 * this class sends the requests to the server
 * it handles just the networking tasks
 * @hide
 */
public class RequestProcessor {

    public static final int NETWORK_CONNECTION_TIMEOUT = 60 * 1000;  // 1 minute
    static final int NETWORK_READ_TIMEOUT = 60 * 1000;  // 1 minute

    private final RequestUrlStore mRequestUrlStore;


    public interface ProcessOutputCallback
    {
        public void process(int statusCode, HttpURLConnection connection);
    }

    public RequestProcessor(RequestUrlStore requestUrlStore) {
        mRequestUrlStore = requestUrlStore;
    }

    /**
     * gets the URL for a string, returns null for invalid urls
     *
     * @param urlString
     * @return
     */
    public URL getUrl(String urlString) {
        try {
            return new URL(urlString);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * returns the http url connection for the given url
     *
     * @param url
     * @return
     * @throws IOException
     */

    public HttpURLConnection getUrlConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }

    /**
     * sends the request to the server and returns the status code
     *
     * @param url
     * @return statusCode, 0 for retry, -1 for remove, 200 for success
     */
    public int sendRequest(URL url, ProcessOutputCallback processOutput) throws InterruptedException {
        HttpURLConnection connection = null;
        try {
            connection = getUrlConnection(url);


            connection.setRequestMethod("GET");
            connection.setConnectTimeout(NETWORK_CONNECTION_TIMEOUT);
            connection.setReadTimeout(NETWORK_READ_TIMEOUT);
            connection.setUseCaches(false);
            connection.connect();
            int statusCode = connection.getResponseCode();

            if (processOutput != null)
                processOutput.process(statusCode, connection);

            return statusCode;

        } catch (EOFException e) {
            WebtrekkLogging.log("RequestProcessor: EOF > Will retry later.", e);
            return 500;
        } catch (SocketTimeoutException e) {
            WebtrekkLogging.log("RequestProcessor: SocketTimeout > Will retry later.", e);
            return 500;
        } catch (SocketException e) {
            WebtrekkLogging.log("RequestProcessor: Socket Exception.", e);
            return 500;
        }  catch (UnknownHostException e) {
            WebtrekkLogging.log("RequestProcessor: UnknownHost > Will retry later.", e);
            return 500;
        } catch (IOException e) {
            WebtrekkLogging.log("io exception: can not connect to host", e);
            WebtrekkLogging.log("RequestProcessor: IO > Removing URL from queue because exception cannot be handled.", e);
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
        return -1;
    }

    public Observable<Integer> getResponseCode(){

        final String urlString = mRequestUrlStore.peek();
        final URL url = getUrl(urlString);
        if (url == null) {
            WebtrekkLogging.log("Removing invalid URL '" + urlString + "' from queue. remaining: " + mRequestUrlStore.size());
            mRequestUrlStore.removeLastURL();

        }
       return Observable.create(new ObservableOnSubscribe<Integer>() {
           @Override
           public void subscribe(ObservableEmitter<Integer> e) throws Exception {

               e.onNext(sendRequest(url,null));
           }
       });
    }

}
