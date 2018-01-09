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
 * Created by Thomas Dahlmann on 16.03.15.
 */

package com.Webtrekk.SDKTest;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.JsonReader;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.webtrekk.webtrekksdk.Request.RequestProcessor;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.WebtrekkApplication;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Created by user on 11/03/15.
 */

public class MyApplication extends WebtrekkApplication {
    private static  SDKInstanceManager mSDKManager = new SDKInstanceManager();

    public SDKInstanceManager getSDKManager(){
        return  mSDKManager;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mSDKManager.setup();

        AppsFlyerConversionListener conversionDataListener =
                new AppsFlyerConversionListener() {

                    @Override
                    public void onInstallConversionDataLoaded(Map<String, String> map) {
                        final String status = map.get("af_status");
                        final String media_source = map.get("media_source");
                        WebtrekkLogging.log("media_source: " + media_source);
                        WebtrekkLogging.log("status: " + status);
                        if (status != null && status.equals("Non-organic") && media_source != null) {
                            final PostInstallSender sender = new PostInstallSender(MyApplication.this);
                            sender.send(media_source);
                            //sender.send(media_source, getAdvID());
                        }
                        WebtrekkLogging.log("onInstallConversionDataLoaded. Value: " + getPlainString(map));
                    }

                    @Override
                    public void onInstallConversionFailure(String s) {
                        WebtrekkLogging.log("onInstallConversionFailure. Value: " + s);
                    }

                    @Override
                    public void onAppOpenAttribution(Map<String, String> map) {
                        WebtrekkLogging.log("onAppOpenAttribution. Value: " + getPlainString(map));
                    }

                    @Override
                    public void onAttributionFailure(String s) {
                        WebtrekkLogging.log("onAttributionFailure. Value: " + s);
                    }

                    @NonNull
                    private String getPlainString(@NonNull Map<String, String> map){
                        String mapPlainStr = "";
                        for (Map.Entry<String, String> entry : map.entrySet()){
                            mapPlainStr += " key:"+ entry.getKey()+" value:" + entry.getValue();
                        }

                        return mapPlainStr;
                    }
                };
        AppsFlyerLib.getInstance().init("SMBb7b3DFRJ2T5i8iETMcD", conversionDataListener, getApplicationContext());
        AppsFlyerLib.getInstance().startTracking(this);
        AppsFlyerLib.getInstance().setDebugLog(true);
    }


    private String getAdvID(){
        AdvertisingIdClient.Info adInfo = null;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());

        } catch (IOException e) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).

        } catch (GooglePlayServicesNotAvailableException e) {
            // Google Play services is not available entirely.
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return adInfo.getId();
    }
}
