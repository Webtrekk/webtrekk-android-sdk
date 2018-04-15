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

import android.test.AndroidTestCase;

import com.webtrekk.webtrekksdk.Request.RequestProcessor;
import com.webtrekk.webtrekksdk.Request.RequestUrlStore;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.mockito.Mockito.*;


public class RequestProcessorTest extends AndroidTestCase {
    private RequestUrlStore requestUrlStore;
    private RequestProcessor requestProcessor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // android bug workaround: 308
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().toString());
        requestUrlStore = mock(RequestUrlStore.class);
        requestProcessor = new RequestProcessor(requestUrlStore);

    }

    public void testEmptyStore() {
        RequestUrlStore requestUrlStore = mock(RequestUrlStore.class);
        when(requestUrlStore.size()).thenReturn(0);
        requestProcessor = spy(requestProcessor);
        when(requestUrlStore.size()).thenReturn(0);
        requestProcessor.run();
        // make sure that the request processor returns without further actions
        verify(requestProcessor, times(0)).getUrl(anyString());
        try {
            verify(requestProcessor, times(0)).sendRequest((URL)any(), (RequestProcessor.ProcessOutputCallback)isNull());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //verifyNoMoreInteractions(requestUrlStore);
    }

    public void testSingleValidUrlInStore() throws IOException {
        requestProcessor = spy(requestProcessor);
        when(requestUrlStore.size()).thenReturn(1).thenReturn(0);
        // test valid url first
        when(requestUrlStore.peek()).thenReturn("http://nglab.org");
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        when(mockHttpURLConnection.getResponseCode()).thenReturn(200);
        doReturn(mockHttpURLConnection).when(requestProcessor).getUrlConnection((URL) any());
        requestProcessor.run();
        // two times, first one returns the urlstring, second one is empty
        verify(requestUrlStore, times(1)).peek();
        verify(mockHttpURLConnection, times(1)).connect();
        verify(mockHttpURLConnection, times(1)).getResponseCode();
        verify(requestUrlStore, times(1)).removeLastURL();
        verify(mockHttpURLConnection, times(1)).disconnect();

    }

    public void testMoreValidUrlInStore() throws IOException {
        requestProcessor = spy(requestProcessor);
        when(requestUrlStore.size()).thenReturn(2).thenReturn(1).thenReturn(0);
        // test valid url first
        when(requestUrlStore.peek()).thenReturn("http://nglab.org").thenReturn("http://nglab.org");
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        when(mockHttpURLConnection.getResponseCode()).thenReturn(200).thenReturn(200);
        doReturn(mockHttpURLConnection).when(requestProcessor).getUrlConnection((URL) any());
        requestProcessor.run();
        // two times, first one returns the urlstring, second one is empty
        verify(requestUrlStore, times(2)).peek();
        verify(mockHttpURLConnection, times(2)).connect();
        verify(mockHttpURLConnection, times(2)).getResponseCode();
        verify(requestUrlStore, times(2)).removeLastURL();
        verify(mockHttpURLConnection, times(2)).disconnect();
    }

    public void testInvalidUrl() throws IOException {
        requestProcessor = spy(requestProcessor);
        when(requestUrlStore.size()).thenReturn(1).thenReturn(0);
        // test valid url first
        when(requestUrlStore.peek()).thenReturn("invalidurl");
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        when(mockHttpURLConnection.getResponseCode()).thenReturn(200);
        doReturn(mockHttpURLConnection).when(requestProcessor).getUrlConnection((URL) any());
        requestProcessor.run();
        // two times, first one returns the urlstring, second one is empty
        verify(requestUrlStore, times(1)).peek();
        verify(mockHttpURLConnection, times(0)).connect();
        verify(mockHttpURLConnection, times(0)).getResponseCode();
        verify(requestUrlStore, times(1)).removeLastURL();
        verify(mockHttpURLConnection, times(0)).disconnect();
    }

    public void testClientSideError() throws IOException {
        requestProcessor = spy(requestProcessor);
        when(requestUrlStore.size()).thenReturn(1).thenReturn(0);
        // test valid url first
        when(requestUrlStore.peek()).thenReturn("http://nglab.org");
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        when(mockHttpURLConnection.getResponseCode()).thenReturn(500);
        doReturn(mockHttpURLConnection).when(requestProcessor).getUrlConnection((URL)any());
        requestProcessor.run();
        // two times, first one returns the urlstring, second one is empty
        verify(requestUrlStore, times(1)).peek();
        verify(mockHttpURLConnection, times(1)).connect();
        verify(mockHttpURLConnection, times(1)).getResponseCode();
        verify(requestUrlStore, times(0)).removeLastURL();
        verify(mockHttpURLConnection, times(1)).disconnect();
    }

    public void testServerSideError() throws IOException {
        requestProcessor = spy(requestProcessor);
        when(requestUrlStore.size()).thenReturn(1).thenReturn(0);
        // test valid url first
        when(requestUrlStore.peek()).thenReturn("http://nglab.org");
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        when(mockHttpURLConnection.getResponseCode()).thenReturn(-1);
        doReturn(mockHttpURLConnection).when(requestProcessor).getUrlConnection((URL) any());
        requestProcessor.run();
        // two times, first one returns the urlstring, second one is empty
        verify(requestUrlStore, times(1)).peek();
        verify(mockHttpURLConnection, times(1)).connect();
        verify(mockHttpURLConnection, times(1)).getResponseCode();
        verify(requestUrlStore, times(1)).removeLastURL();
        verify(mockHttpURLConnection, times(1)).disconnect();
    }

}
