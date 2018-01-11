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
 * Created by Thomas Dahlmann on 19.04.15.
 */

package com.webtrekk.SDKTest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.Webtrekk;

import java.io.OutputStream;
import java.net.URLDecoder;


public class ShopExampleActivity extends Activity {

    private Webtrekk webtrekk;
    TrackingParameter tp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_example);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String everID = bundle.getString("everID"); // "everID"
            String mediaCode = bundle.getString("mediaCode"); // "MediaCode"

            webtrekk = Webtrekk.getInstance();
            webtrekk.setEverId(everID);
            webtrekk.setMediaCode(mediaCode);
        }
        tp = new TrackingParameter();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shop_example, menu);
        return true;
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

    public void onButtonOrderClicked(View view) {
//        TrackingParameter buttonParameter = new TrackingParameter();
//        buttonParameter.add(Parameter.PRODUCT_STATUS, "add")
//        .add(Parameter.ACTION_NAME, "orderButton");
//        webtrekk.track(buttonParameter);

        try {
            OutputStream output = this.getApplication().getApplicationContext().openFileOutput("webtrekk-referrer-store", Context.MODE_PRIVATE);
            output.write(URLDecoder.decode("utm_source%3Dgoogle%26utm_medium%3Dcpc%26utm_term%3Drunning%252Bshoes%26utm_content%3DdisplayAd1%26utm_campaign%3Dshoe%252Bcampaign", "UTF-8").getBytes());
            output.close();
        }
        catch (Exception e){}

        tp.add(Parameter.ACTION, "action");
        tp.add(Parameter.ACTION_NAME, "action-name");

        webtrekk.track();
    }
}
