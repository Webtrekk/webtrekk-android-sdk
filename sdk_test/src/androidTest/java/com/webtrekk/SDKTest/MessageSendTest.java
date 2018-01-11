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
 * Created by Arsen Vartbaronov on 23.06.16.
 */

package com.webtrekk.SDKTest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.filters.LargeTest;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class MessageSendTest extends WebtrekkBaseMainTest {

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, null, false, false);

    @Override
    @Before
    public void before() throws Exception{
        super.before();
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testManualFlash()
    {
        this.initWebtrekk(R.raw.webtrekk_config_manual_flush);
        mActivityRule.launchActivity(null);
        mWaitMilliseconds = 2000;
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        }, true);

        waitForTrackedURL();

        initWaitingForTrack(null);

        mWebtrekk.send();

        waitForTrackedURL();
    }

    @Test
    public void testURLCashFileMigration(){
        final String FILE_NAME = "wt-tracking-requests";
        final String URL_STORE_CURRENT_SIZE = "URL_STORE_CURRENT_SIZE";
        final String URL_STORE_SENDED_URL_OFSSET = "URL_STORE_SENDED_URL_OFSSET";

        Context applicationContext = getInstrumentation().getTargetContext().getApplicationContext();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = applicationContext.getAssets().open(FILE_NAME);
            File outFile = new File(applicationContext.getFilesDir(), FILE_NAME);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            out.flush();
        } catch (IOException e) {
            WebtrekkLogging.log("error:", e);
            assertTrue(false);
        } finally {
            try {
                if (in != null){
                    in.close();
                }
                if (out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        SharedPreferences.Editor prefEdit = HelperFunctions.getWebTrekkSharedPreference(applicationContext).edit();

        prefEdit.putLong(URL_STORE_SENDED_URL_OFSSET, 0);
        prefEdit.putInt(URL_STORE_CURRENT_SIZE, 1000).apply();

        this.initWebtrekk(R.raw.webtrekk_config_manual_flush);

        mWaitMilliseconds = 70000;
        initWaitingForTrack(null, 1000);
        mWebtrekk.send();


        waitForTrackedURLs();

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
