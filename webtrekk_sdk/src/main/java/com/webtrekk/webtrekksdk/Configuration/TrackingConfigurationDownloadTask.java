package com.webtrekk.webtrekksdk.Configuration;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.webtrekk.webtrekksdk.Request.RequestProcessor;
import com.webtrekk.webtrekksdk.Utils.ActivityTrackingStatus;
import com.webtrekk.webtrekksdk.Utils.AsyncTest;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func0;

/**
 * Created by eabdelra on 3/24/2018.
 */

///// create TrackingConfigurationDownloadTask observer instead on AsyncTack and instead
/// of calling doInbackground we will call the observable .fromCallable
/// and instead of onPostExecute we will call onNext Observer

public class TrackingConfigurationDownloadTask implements Observer<TrackingConfiguration> {

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


    public Observable<TrackingConfiguration> parseConfiguration(final String urls){

        return Observable.fromCallable(
                new Callable<TrackingConfiguration>() {
                    @Override
                    public TrackingConfiguration call() throws Exception {
                        WebtrekkLogging.log("trying to get remote configuration url: " + urls);
                        // Instantiate the parser
                        TrackingConfigurationXmlParser trackingConfigurationParser = new TrackingConfigurationXmlParser();
                        //stream = downloadUrl(urls[0]);

                        trackingConfigurationString = getXmlFromUrl(urls);
                        WebtrekkLogging.log("remote configuration string: " + trackingConfigurationString);
                        if (trackingConfigurationString != null) {
                            trackingConfiguration = trackingConfigurationParser.parse(trackingConfigurationString);
                            return trackingConfiguration;
                        } else {

                            WebtrekkLogging.log("error getting the xml configuration string from url: " + urls);
                        }

                        return null;
                    }

                });
        }
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

        Log.i("onError","Throwable ",e);

    }

    @Override
    public void onNext(TrackingConfiguration config) {

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
