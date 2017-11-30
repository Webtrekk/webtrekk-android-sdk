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
 * Created by Arsen Vartbaronov on 27.05.16.
 */

package com.Webtrekk.SDKTest;

import android.support.test.filters.LargeTest;

import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class TransparentActivityTest extends WebtrekkBaseMainTest {

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, null, false, false);

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        Webtrekk.getInstance().initWebtrekk(mApplication);
    }

    @After
    @Override
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testTransparentActivity()
    {
        initWaitingForTrack(null);
        mActivityRule.launchActivity(null);

        waitForTrackedURL();

        initWaitingForTrack(null);
        onView(withId(R.id.trasparent_activity_start)).perform(click());

        waitForTrackedURL();

        initWaitingForTrack(null, true);

        pressBack();

        waitForTrackedURL();
    }
}