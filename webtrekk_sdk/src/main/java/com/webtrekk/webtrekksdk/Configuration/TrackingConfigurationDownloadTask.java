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

package com.webtrekk.webtrekksdk.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.webtrekk.webtrekksdk.Request.RequestProcessor;
import com.webtrekk.webtrekksdk.Utils.AsyncTest;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * This classe downloads the xml configuration from the configured remote url,
 * it runs asynchronous in the background during application start if enabled
 */
public class TrackingConfigurationDownloadTask extends AsyncTask<String, Void, TrackingConfiguration> {
    private Context context;
    private Webtrekk webtrekk;
    private TrackingConfiguration trackingConfiguration;
    private String trackingConfigurationString;
    // this interface is for testing asynchronous calls, only used during unit tests to notify that the task is done
    private AsyncTest asyncTest;


    public TrackingConfigurationDownloadTask(Webtrekk webtrekk, AsyncTest asyncTest) {
        this.webtrekk = webtrekk;
        this.context = webtrekk.getContext();
        this.asyncTest = asyncTest;
    }

    /**
     * runs in the background and downloads and parses the xmlconfigration from the configured remoteurl
     *
     * @param urls
     * @return
     */
    @Override
    protected TrackingConfiguration doInBackground(String... urls) {
        WebtrekkLogging.log("trying to get remote configuration url: " + urls[0]);
        // Instantiate the parser
        TrackingConfigurationXmlParser trackingConfigurationParser = new TrackingConfigurationXmlParser();
        //stream = downloadUrl(urls[0]);

        try {
            trackingConfigurationString = getXmlFromUrl(urls[0]);
            WebtrekkLogging.log("remote configuration string: " + trackingConfigurationString);
            if (trackingConfigurationString != null) {
                trackingConfiguration = trackingConfigurationParser.parse(trackingConfigurationString);
                return trackingConfiguration;
            } else {
                WebtrekkLogging.log("error getting the xml configuration string from url: " + urls[0]);
            }

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (IOException e) {
            WebtrekkLogging.log("xml parser error, ioexception", e);
        } catch (XmlPullParserException e) {
            WebtrekkLogging.log("xml parser error, no validate xml configuration file", e);
        } catch (Exception e) {
            WebtrekkLogging.log("error getting remove configuration", e);
        }

        return null;
    }

    /**
     * this method gets called when the doInBackground method is finished it replaces the current
     * TrackingConfiguration with a newer one if one was found online, and also store it in the shared prefs
     *
     * @param config
     */
    @Override
    protected void onPostExecute(TrackingConfiguration config) {
        if (config == null) {
            WebtrekkLogging.log("error getting a new valid configuration from remote url, tracking with the old config");
        } else {
            WebtrekkLogging.log("successful downloaded remote configuration");

            if (config.getVersion() > webtrekk.getTrackingConfiguration().getVersion()) {
                if(config.validateConfiguration()) {
                    WebtrekkLogging.log("found a new version online, updating current version");
                    // either store it as xml on the internal storage or save it as xml string in the shared prefs
                    WebtrekkLogging.log("saving new trackingConfiguration to preferences");
                    SharedPreferences sharedPrefs = HelperFunctions.getWebTrekkSharedPreference(context);
                    sharedPrefs.edit().putString(Webtrekk.PREFERENCE_KEY_CONFIGURATION, trackingConfigurationString).apply();

                    //TODO: update the current configuration only if valid and newer
                    WebtrekkLogging.log("updating current trackingConfiguration");
                    webtrekk.setTrackingConfiguration(trackingConfiguration);
                } else {
                    WebtrekkLogging.log("new remote version is invalid");
                }

            } else {
                WebtrekkLogging.log("local config is already up to date, doing nothing");
            }
        }

        if (asyncTest != null) {
            asyncTest.workDone();
            WebtrekkLogging.log("asyncTest: workdDone()");
        }

    }

    /**
     * Reads a stream and writes it into a string. Closes inputStream when done.
     *
     * @param inputStream The stream to read
     * @return A string, containing stream data
     * @throws java.io.IOException
     */
    String stringFromStream(InputStream inputStream) throws java.io.IOException {
        final String encoding = "UTF-8";
        final long maxSize = 1024*1024;
        final StringBuilder builder = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);

                if (builder.length() > maxSize) {
                    WebtrekkLogging.log("Error load remote configuration xml. Exceeded size (>=" + maxSize + ")");
                    return null;
                }
            }
        }finally {
            reader.close();
        }

        return builder.toString();
    }

    public String getXmlFromUrl(String url) throws IOException {
        // defaultHttpClient
        HttpURLConnection urlConnection;
        InputStream is = null;
        try {
            URL trackingConfigurationUrl = new URL(url);
            urlConnection = (HttpURLConnection) trackingConfigurationUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(RequestProcessor.NETWORK_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(RequestProcessor.NETWORK_CONNECTION_TIMEOUT);
            urlConnection.setRequestProperty("Content-Type", "application/xml");
            urlConnection.connect();
            int response = urlConnection.getResponseCode();
            is = urlConnection.getInputStream();
            String xmlConfiguration = stringFromStream(is);
            return xmlConfiguration;
        } catch (MalformedURLException e) {
            WebtrekkLogging.log("getXmlFromUrl: invalid URL", e);
        } catch (ProtocolException e) {
            WebtrekkLogging.log("getXmlFromUrl: invalid URL", e);
        } catch (IOException e) {
            WebtrekkLogging.log("getXmlFromUrl: invalid URL", e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return null;
    }
}