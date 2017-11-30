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
 * Created by Thomas Dahlmann 19.04.15.
 */

package com.Webtrekk.SDKTest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.Webtrekk.SDKTest.ProductList.ProductListActivity;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;
import com.mixpanel.android.mpmetrics.MixpanelAPI;


public class MainActivity extends Activity {
    private Webtrekk webtrekk;
    private boolean mAdClearOn;
    private String ADCLEAR_SIGN = "ADCLEAR_SIGN";
    volatile private LoadWebViewResource mLoadResourceCallback;

    interface LoadWebViewResource {
        void load(String url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        if (savedInstanceState != null){
            mAdClearOn = savedInstanceState.getBoolean(ADCLEAR_SIGN);
        }

        mediaCodeReceiverRegister();

        webtrekk = initWithNormalParameter();

        webtrekk.getCustomParameter().put("own_para", "my-value");

        ((TextView)findViewById(R.id.main_version)).setText(getString(R.string.hello_world) + "\nLibrary Version:" + Webtrekk.mTrackingLibraryVersionUI);
        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, "9e956a2e5169ddb44eb87b6acb0eee95");
        updateAdClearCaption();
    }

    private Webtrekk initWithNormalParameter(){
        Webtrekk.getInstance().initWebtrekk(getApplication(), R.raw.webtrekk_config_normal_track);
        return Webtrekk.getInstance();
    }

    private void updateAdClearCaption(){
        Button button = (Button)findViewById(R.id.adclear_button_id);
        button.setText("AdClear test " + (mAdClearOn ? "on" : "off"));
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ADCLEAR_SIGN, mAdClearOn);
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        mediaCodeReceiverUnRegister();
        super.onDestroy();
    }


    private BroadcastReceiver mSDKReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String mediaCode = intent.getStringExtra("INSTALL_SETTINGS_MEDIA_CODE");
            String advID = intent.getStringExtra("INSTALL_SETTINGS_ADV_ID");

            Log.d(getClass().getName(),"Broad cast message from SDK is received");

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Media Code")
                    .setMessage("Media code is received: " + mediaCode + "\nAdv id is: " + advID)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();

        }
    };

    /**
     * This is just for testing. To receive Campain installation data
     */
    private void mediaCodeReceiverRegister()
    {
        LocalBroadcastManager.getInstance(this).registerReceiver(mSDKReceiver,
                new IntentFilter("com.Webtrekk.CampainMediaMessage"));
    }

    /**
     * This is just for testing. To receive Campain installation data
     */
    private void mediaCodeReceiverUnRegister()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSDKReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Called when the user clicks the Page Example Activity Button button */
    public void showPageExampleActivity(View view) {
        Intent intent = new Intent(this, PageExampleActivity.class);
        startActivity(intent);
    }

    public void showShopExampleActivity(View view) {
        Intent intent = new Intent(this, ShopExampleActivity.class);
        startActivity(intent);
    }

    public void showMediaExampleActivity(View view) {
        Intent intent = new Intent(this, MediaExampleActivity.class);
        startActivity(intent);
    }

    public void sendCDBRequest(View view)
    {
        Intent intent = new Intent(this, CDBActivityTest.class);
        startActivity(intent);
    }

    public void recommendationTest(View view)
    {
        Intent intent = new Intent(this, RecommendationActivity.class);
        intent.putExtra(RecommendationActivity.RECOMMENDATION_NAME, "complexReco");
        intent.putExtra(RecommendationActivity.RECOMMENDATION_PRODUCT_ID, "085cc2g007");
        startActivity(intent);
    }

    public void productList(View view)
    {
        Intent intent = new Intent(this, ProductListActivity.class);
        startActivity(intent);
    }

    public void adClearTest(View view)
    {
        SDKInstanceManager sdkManager = ((MyApplication)getApplication()).getSDKManager();
        webtrekk = null;
        sdkManager.release(getApplication());
        sdkManager.setup();

        if (mAdClearOn){
            webtrekk = initWithNormalParameter();
            mAdClearOn = false;
        } else {
            webtrekk = Webtrekk.getInstance();
            webtrekk.initWebtrekk(getApplication(), R.raw.webtrekk_config_adclear_integration_test);
            mAdClearOn = true;
        }
        updateAdClearCaption();
    }

    public void appToWebConnection(View view){

        final WebView webView = (WebView)findViewById(R.id.main_web_view);
        webView.setVisibility(View.VISIBLE);

        webView.getSettings().setJavaScriptEnabled(true);

        Webtrekk.getInstance().setupWebView(webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                if (mLoadResourceCallback != null){
                    mLoadResourceCallback.load(url);
                }
                super.onLoadResource(view, url);
            }
        });

        webView.loadUrl("http://jenkins-yat-dev-01.webtrekk.com/web/hello.html");
    }

    public void setLoadResourceCallback(LoadWebViewResource mLoadResourceCallback) {
        this.mLoadResourceCallback = mLoadResourceCallback;
    }

    public Webtrekk getWebtrekk() {
        return webtrekk;
    }
}
