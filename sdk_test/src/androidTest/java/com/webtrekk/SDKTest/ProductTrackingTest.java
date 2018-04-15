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
 * Created by Arsen Vartbaronov on 26.05.16.
 */

package com.webtrekk.SDKTest;

import android.support.test.filters.LargeTest;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class ProductTrackingTest extends WebtrekkBaseMainTest {

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, this);

    @Override
    public void before() throws Exception{
        super.before();
        this.initWebtrekk(R.raw.webtrekk_config_no_auto_track);
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testProductTracking()
    {
        final String productsName = "product1;product2";
        final String productQuantity = "3;1";
        final String productCost = "104.44;34.12";
        final String productCurrency = "EUR";
        final String productStatus = "view";
        final String productVoucher = "voucher";
        final String productCat1 = "ProdCat1";
        final String productCat2 = "ProdCat2";


        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                TrackingParameter par = new TrackingParameter();
                par.add(TrackingParameter.Parameter.PRODUCT, productsName);
                par.add(TrackingParameter.Parameter.PRODUCT_COUNT, productQuantity);
                par.add(TrackingParameter.Parameter.PRODUCT_COST, productCost);
                par.add(TrackingParameter.Parameter.CURRENCY, productCurrency);
                par.add(TrackingParameter.Parameter.PRODUCT_STATUS, productStatus);
                par.add(TrackingParameter.Parameter.VOUCHER_VALUE, productVoucher);
                par.add(TrackingParameter.Parameter.PRODUCT_CAT, "1", productCat1);
                par.add(TrackingParameter.Parameter.PRODUCT_CAT, "2", productCat2);
                mWebtrekk.track(par);
            }
        });

        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals(productsName, HelperFunctions.urlDecode(parcel.getValue("ba")));
        assertEquals(productQuantity, HelperFunctions.urlDecode(parcel.getValue("qn")));
        assertEquals(productCost, HelperFunctions.urlDecode(parcel.getValue("co")));
        assertEquals(productCurrency, HelperFunctions.urlDecode(parcel.getValue("cr")));
        assertEquals(productStatus, HelperFunctions.urlDecode(parcel.getValue("st")));
        assertEquals(productVoucher, HelperFunctions.urlDecode(parcel.getValue("cb563")));
        assertEquals(productCat1, HelperFunctions.urlDecode(parcel.getValue("ca1")));
        assertEquals(productCat2, HelperFunctions.urlDecode(parcel.getValue("ca2")));
    }
}