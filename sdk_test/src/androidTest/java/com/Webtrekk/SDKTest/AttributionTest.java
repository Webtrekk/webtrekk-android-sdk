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
 * Created by Arsen Vartbaronov on 01.04.16.
 */

package com.Webtrekk.SDKTest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class AttributionTest extends WebtrekkBaseSDKTest {

    volatile String mAdvID;
    private Context mContext;
    volatile boolean mNotifierDone;
    volatile boolean postInstallSendResult;
    final Object mWaiter = new Object();
    final String MEDIA_CODE = "MEDIA_CODE";


    @Rule
    public final WebtrekkTestRule<MainActivity> mActivityRule =
            new WebtrekkTestRule<>(MainActivity.class, null, false, false);

    @Override
    @Before
    public void before() throws Exception {
        super.before();
    }

    @Override
    @After
    public void after() throws Exception {
        //add sleep to wait until all messages are sent.
        super.after();
    }


    @Test
    public void testAttributionRunLinkWithAdID()
    {
        launchClickID("http://appinstall.webtrekk.net/appinstall/v1/redirect?mc="+MEDIA_CODE+"&trackid=&as1=market%3A//details%3Fid%3Dcom.Webtrekk.SDKTest&aid=", true);
    }

    @Test
    public void testAttributionRunLinkWithoutAdID()
    {
        launchClickID("http://appinstall.webtrekk.net/appinstall/v1/redirect?mc="+MEDIA_CODE+"&trackid=&as1=market%3A//details%3Fid%3Dcom.Webtrekk.SDKTest", false);
    }

    private String[] getFileList(final String contains)
    {
        return getInstrumentation().getTargetContext().getFilesDir().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(contains);
            }
        });

    }

    private String getFileNameOnly(String fullName)
    {
        return fullName.substring(0, fullName.indexOf("."));
    }

    private void launchClickID(String url, boolean useAdvID)
    {
        if (!mIsExternalCall)
            return;

        mActivityRule.launchActivity(null);
        String trackID = Webtrekk.getInstance().getTrackingIDs().get(0);
        url = url.replace("&trackid=", "&trackid="+trackID);

        if (useAdvID) {
            String[] fileList = getFileList(".adv");

            assertTrue(fileList.length == 1);


            String advID = getFileNameOnly(fileList[0]);

            assertNotNull(advID);
            url = url.replace("&aid=", "&aid="+advID);
        }

        final String URLFinal = url;


        mActivityRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                WebView webView = (WebView) mActivityRule.getActivity().findViewById(R.id.main_web_view);
                webView.setVisibility(View.VISIBLE);


                webView.setWebViewClient(new WebViewClient() {

                                             @Override
                                             public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                 URLParsel parcel = new URLParsel();

                                                 parcel.parseURL(url);
                                                 String clickID = parcel.getValue("referrer").split("%3D")[1];
                                                 File file = new File(mActivityRule.getActivity().getFilesDir(), clickID + ".clk");

                                                 try {
                                                     file.createNewFile();
                                                 } catch (IOException e) {
                                                     e.printStackTrace();
                                                 }

                                                 synchronized (mWaiter) {
                                                     mNotifierDone = true;
                                                     mWaiter.notifyAll();
                                                 }
                                                 return true;
                                             }
                                         }
                );

                webView.loadUrl(URLFinal);
            }
        });


        try {
            synchronized (mWaiter) {
                while (!mNotifierDone)
                   mWaiter.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAdID()
    {
        if (!mIsExternalCall)
            return;
        Object notifier = new Object();
        mContext = getInstrumentation().getTargetContext();

        new Thread(new AdvIDReader(notifier)).start();

        synchronized (notifier)
        {
            try {
                while (!mNotifierDone)
                   notifier.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        assertNotNull(mAdvID);


        File file = new File(getInstrumentation().getTargetContext().getFilesDir(), mAdvID+".adv");

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(getClass().getName(), "Create advID file:" + file.getAbsolutePath());
    }

    @Test
    public void testNoCampaignMode(){

        if (!mIsExternalCall)
            return;

        LocalBroadcastManager.getInstance(getInstrumentation().getTargetContext()).registerReceiver(mSDKNoCampaignTestReceiver,
                new IntentFilter("com.Webtrekk.CampainMediaMessage"));

        Webtrekk.getInstance().initWebtrekk(mApplication, R.raw.webtrekk_config_no_campaign_test);

        synchronized (mWaiter) {
            while (!mNotifierDone)
                try {
                    mWaiter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        LocalBroadcastManager.getInstance(getInstrumentation().getTargetContext()).unregisterReceiver(mSDKNoCampaignTestReceiver);
    }

    class AdvIDReader implements Runnable {
        private final Object mNotifier;

        public AdvIDReader(Object notifier)
        {
            mNotifier = notifier;
        }

        @Override
        public void run() {

            AdvertisingIdClient.Info adInfo = null;
            //get adv ID

            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            }

            mAdvID = adInfo.getId();

            synchronized (mNotifier) {
                mNotifierDone = true;
                mNotifier.notifyAll();
            }

        }
    }

    @Test
    public void sendPostBack(){
        if (!mIsExternalCall)
            return;

        Webtrekk.getInstance().initWebtrekk(mApplication);
        final PostInstallSender sender = new PostInstallSender(getApplication().getApplicationContext());

        sender.send(MEDIA_CODE, new PostInstallSender.CompleteNotifier() {
            @Override
            public void complete(boolean isSuccessful) {
                mNotifierDone = true;
                postInstallSendResult = isSuccessful;

                synchronized (mWaiter)
                {
                    mWaiter.notifyAll();
                }

            }
        });

        synchronized (mWaiter) {
            while (!mNotifierDone)
                try {
                    mWaiter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

        assertTrue(postInstallSendResult);

    }

    @Test
    public void testFirstStart()
    {
        if (!mIsExternalCall)
            return;

        Webtrekk.getInstance().initWebtrekk(mApplication);

        LocalBroadcastManager.getInstance(mApplication).registerReceiver(mSDKCampaignTestReceiver,
                new IntentFilter("com.Webtrekk.CampainMediaMessage"));

        synchronized (mWaiter) {
            while (!mNotifierDone)
                try {
                    mWaiter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        LocalBroadcastManager.getInstance(mApplication).unregisterReceiver(mSDKCampaignTestReceiver);
    }

    private BroadcastReceiver mSDKCampaignTestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String mediaCode = intent.getStringExtra("INSTALL_SETTINGS_MEDIA_CODE");
            String advID = intent.getStringExtra("INSTALL_SETTINGS_ADV_ID");

            Log.d(getClass().getName(), "Broad cast message from SDK is received");

            String[] advFileList = getFileList(".adv");
            if (advFileList.length == 1)
            {
                assertEquals(advID, getFileNameOnly(advFileList[0]));
            }

            assertEquals(mediaCode, MEDIA_CODE);

            mNotifierDone = true;

            synchronized (mWaiter)
            {
                mWaiter.notifyAll();
            }
        };
    };

    private BroadcastReceiver mSDKNoCampaignTestReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String mediaCode = intent.getStringExtra("INSTALL_SETTINGS_MEDIA_CODE");

            assertEquals("NoCampaignMode", mediaCode);

            Log.d(getClass().getName(), "No campaign mode is ON.");

            mNotifierDone = true;

            synchronized (mWaiter)
            {
                mWaiter.notifyAll();
            }
        };
    };

}
