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
 * Created by Arsen Vartbaronov on 14.04.16.
 */

package com.webtrekk.SDKTest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

public class EmptyActivity extends Activity {

    public boolean isStopped() {
        return mIsStopped;
    }

    public boolean isStartedToStopping() {
        return mIsStartedToStopping;
    }

    volatile boolean mIsStopped;
    volatile boolean mIsStartedToStopping;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsStopped = false;
        mIsStartedToStopping = false;
    }

    @Override
    protected void onStop() {
        mIsStartedToStopping = true;
        super.onStop();
        mIsStopped = true;
    }

    public void onTransparentActivity(View view){
        startActivity(new Intent(this, TransparentActivity.class));
    }

    public void onPageExampleActivity(View view){
        startActivity(new Intent(this, PageExampleActivity.class));
    }
}
