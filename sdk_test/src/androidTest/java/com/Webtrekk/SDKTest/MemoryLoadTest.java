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
 * Created by Arsen Vartbaronov on 22.04.16.
 */

package com.Webtrekk.SDKTest;

import android.content.Intent;
import android.support.test.filters.LargeTest;
import android.test.suitebuilder.annotation.Suppress;

import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class MemoryLoadTest extends WebtrekkBaseMainTest  {
    private final long oneMeg = 1024*1024;

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, this);

    @Override
    public void before() throws Exception {
        super.before();
        initWebtrekk(R.raw.webtrekk_config_no_auto_track);
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    public void testLoadMemory()
    {
        Runnable runnableTrackOnly = new Runnable(){

            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Webtrekk.getInstance().track();
            }
        };

        initWaitingForTrack(runnableTrackOnly);
        waitForTrackedURL();
        initWaitingForTrack(runnableTrackOnly);
        waitForTrackedURL();
    }

    @Test
    public void testLoadCPU()
    {
        Intent intent = new Intent(getInstrumentation().getTargetContext(), LoadOSService.class);
        intent.putExtra(LoadOSService.MODE, LoadOSService.Mode.LOAD_CPU.ordinal());

        getInstrumentation().getTargetContext().startService(intent);

        Runnable runnableTrackOnly = new Runnable(){

            @Override
            public void run() {
                Webtrekk.getInstance().track();
            }
        };

        for (int i = 0 ; i < 10; i++) {
            initWaitingForTrack(runnableTrackOnly, 1);
            waitForTrackedURLs();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        getInstrumentation().getTargetContext().stopService(intent);
    }
}
