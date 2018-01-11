package com.webtrekk.SDKTest;

import android.content.SharedPreferences;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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
 * Created by Arsen Vartbaronov on 16.10.17.
 */

public class EverIDTransferTest extends WebtrekkBaseSDKTest  {

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, null, false, false);

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
    public void testEverIDFromV2Transfer(){

        final String everIdKey = "everId";
        //clear everID
        SharedPreferences sharedPrefs = HelperFunctions.getWebTrekkSharedPreference(mApplication);
        sharedPrefs.edit().remove(everIdKey).commit();

        //setupEverID as in V2
        final Core coreV2 = new Core();
        coreV2.setContext(getApplication());
        final String everIdV2 = coreV2.getEverId();

        // do Webtrekk initialization.
        Webtrekk.getInstance().initWebtrekk(getApplication());
        final String everIdLastVersion = Webtrekk.getInstance().getEverId();

        assertEquals(everIdV2, everIdLastVersion);

    }
}
