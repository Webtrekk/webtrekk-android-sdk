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

package com.webtrekk.SDKTest;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.webtrekk.SDKTest.ProductList.ProductListActivity;
import com.webtrekk.webtrekksdk.Webtrekk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {
    private Webtrekk webtrekk;
    private boolean mAdClearOn;
    private String ADCLEAR_SIGN = "ADCLEAR_SIGN";
    volatile private LoadWebViewResource mLoadResourceCallback;
    private final PermissionRequest permissionRequest = new PermissionRequest();

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mediaCodeReceiverRegister();

        final String permissions[] = {"android.permission.READ_CONTACTS", "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"};

        final String permissionsUI[] = {"Permission Read Contact Granted", "Permission Read External Storage Granted",
                "Permission Write External Storage Granted"};

        final Runnable runnables[] = {new Runnable() {
            @Override
            public void run() {
                webtrekk = initWithNormalParameter();

                webtrekk.getCustomParameter().put("own_para", "my-value");
            }
        }, null, null};

        permissionRequest(permissions, permissionsUI, runnables);

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

        if (webtrekk == null){
            return;
        }

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

    private void permissionRequest(String permissions[], final String permissionUINames[], @Nullable  final Runnable onCompletes[]){
        List<String> permissionsNotGranted = new ArrayList<>(Arrays.asList(permissions));

        for (int i = 0; i < permissionsNotGranted.size(); i++) {
            if (ContextCompat.checkSelfPermission(this, permissionsNotGranted.get(i))
                    == PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsNotGranted.remove(i);
                i--;
            }
        }

        if (permissionsNotGranted.size() > 0) {
            final List<Completable> completes = permissionRequest.
                    requestPermission(this, permissionsNotGranted.toArray(permissions));

            for (int i = 0; i < completes.size(); i++) {
                final Completable complete = completes.get(i);
                final String permissionUIName = permissionUINames[i];
                final Runnable onComplete = onCompletes == null ? null : onCompletes[i];

                complete.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                Log.d("Permission", permissionUIName);
                                if (onCompletes != null && onComplete != null) {
                                    onComplete.run();
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.d("Permission", "Permission Error: " + throwable.getLocalizedMessage());
                            }
                        });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                    @NonNull int[] grantResults) {
        permissionRequest.processResponse(requestCode, permissions, grantResults);
    }

}
