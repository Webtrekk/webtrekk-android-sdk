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
 * Created by Arsen Vartbaronov on 30.05.16.
 */

package com.Webtrekk.SDKTest;

import android.content.Intent;
import android.os.Looper;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Webtrekk;
import com.webtrekk.webtrekksdk.WebtrekkRecommendations;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class RecommendationsTest extends WebtrekkBaseMainTest {

    @Rule
    public final WebtrekkTestRule<RecommendationActivity> mActivityRule =
            new WebtrekkTestRule<>(RecommendationActivity.class, null, false, false);

    @Override
    @Before
    public void before() throws Exception{
        super.before();
        this.initWebtrekk(R.raw.webtrekk_config_recomendations);
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testComplexRecommendations()
    {
        recommendTest("complexReco", "085cc2g007", null);
    }

    @Test
    public void testSimpleRecommendations()
    {
        recommendTest("simpleReco", null, null);
    }

    @Test
    public void testEmptyRecommendationList()
    {
        recommendTest("emptyTest", null, null, 0);
    }

    private void recommendTest(String recName, String productID, String productCat)
    {
        recommendTest(recName, productID, productCat, 1);
    }

    private void recommendTest(String recName, String productID, String productCat, int countCheck)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(RecommendationActivity.RECOMMENDATION_NAME, recName);
        intent.putExtra(RecommendationActivity.RECOMMENDATION_PRODUCT_ID, productID);
        intent.putExtra(RecommendationActivity.RECOMMENDATION_PRODUCT_CAT, productCat);
        mActivityRule.launchActivity(intent);
        //Wait for recommendation request done
        while (!mActivityRule.getActivity().isRequestFinished()) {
            getInstrumentation().waitForIdleSync();
        }

        //Check results
        assertEquals(WebtrekkRecommendations.QueryRecommendationResult.RECEIVED_OK, mActivityRule.getActivity().getLastResult());

        if (countCheck > 0)
          assertTrue(mActivityRule.getActivity().getRecommendationCount() >= countCheck);
        else
          assertEquals(0, mActivityRule.getActivity().getRecommendationCount());
        assertTrue(mActivityRule.getActivity().isUsedUIThread());
    }

    @Test
    public void testRecoRequest()
    {
        WebtrekkRecommendations recommendations = mWebtrekk.getRecommendations();

        initWaitingForTrack(null);
        final long currentThreadID = Thread.currentThread().getId();

        assertFalse(Looper.getMainLooper().getThread() == Thread.currentThread());

        recommendations.queryRecommendation(new WebtrekkRecommendations.RecommendationCallback() {
            @Override
            public void onReceiveRecommendations(List<WebtrekkRecommendations.RecommendationProduct> products, WebtrekkRecommendations.QueryRecommendationResult result) {
                assertFalse(currentThreadID == Thread.currentThread().getId());
            }
        }, "paramTest").setProductId("productIDTest")/*.setProductCat("productCatTest")*/.call();

        String url = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(url);

        assertEquals("productIDTest", parcel.getValue("product"));
        //assertEquals("productCatTest", parcel.getValue("productCat"));
        assertEquals(HelperFunctions.getEverId(getInstrumentation().getTargetContext()), parcel.getValue("userId"));
    }
}
