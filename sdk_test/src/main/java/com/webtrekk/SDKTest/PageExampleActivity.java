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
 * Created by Thomas Dahlmann on 23.04.15.
 */

package com.webtrekk.SDKTest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Webtrekk;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;


public class PageExampleActivity extends Activity {
    private Webtrekk webtrekk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_example_activity);
        webtrekk = Webtrekk.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        //webtrekk.startActivity("PageExampleActivity");
        webtrekk.getCustomParameter();
    }

    @Override
    public void onStop() {
        //webtrekk.stopActivity();
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_page_example_activity, menu);
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

    public void onButtonActionClicked(View view) {
        TrackingParameter tp = new TrackingParameter();
        tp.add(Parameter.ACTION_NAME, "Action Button clicked");
        webtrekk.track(tp);
    }

    public void onCheckboxActionClicked(View view) {
        TrackingParameter tp = new TrackingParameter();
        tp.add(Parameter.PAGE_CAT, "1", "Herren")
                .add(Parameter.ACTION, "2", "Schuhe")
                .add(Parameter.ACTION, "3", "Sportschuhe");
        webtrekk.track(tp);
    }

    public void onButtonActionParamsClicked(View view) {
        TrackingParameter tp = new TrackingParameter();
        tp.add(Parameter.ACTION, "Action Button clicked")
                .add(Parameter.ACTION,"1", "grey")
                .add(Parameter.ACTION,"2", "pos3");
        webtrekk.track(tp);

        TrackingParameter tp_pageparams = new TrackingParameter();
        tp_pageparams.add(Parameter.PAGE, "1", "green")
                .add(Parameter.PAGE, "2", "4")
                .add(Parameter.PAGE, "3", "234");
        webtrekk.track(tp_pageparams);
    }

    public void onButtonNextPage(View view){
        Intent intent = new Intent(this, NextPageExampleActivity.class);
        startActivity(intent);
    }
}
