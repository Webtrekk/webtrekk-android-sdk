package com.Webtrekk.SDKTest;

import android.support.test.filters.LargeTest;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.Webtrekk.SDKTest.ProductList.ProductItem;
import com.Webtrekk.SDKTest.ProductList.ProductListActivity;
import com.webtrekk.webtrekksdk.ProductListTracker;
import com.webtrekk.webtrekksdk.ProductParameterBuilder;
import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.Espresso.pressBack;
import static java.lang.Thread.sleep;


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
 * Created by Arsen Vartbaronov on 16.11.17.
 */
@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class ProductListTrackingTest extends WebtrekkBaseMainTest {
    private Webtrekk mWebtrekk;

    @Rule
    public final WebtrekkTestRule<ProductListActivity> mActivityRule =
            new WebtrekkTestRule<>(ProductListActivity.class, null,false, true);

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mEmptyActivity =
            new WebtrekkTestRule<>(EmptyActivity.class, null,false, true);

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        mWaitMilliseconds = 5000;
        initWT();
    }

    @Override
    @After
    public void after() throws Exception {
        super.after();
    }

    private void initWT(){
        mWebtrekk = Webtrekk.getInstance();
        mWebtrekk.initWebtrekk(mApplication, R.raw.webtrekk_config_no_auto_track);
    }
    @Test
    public void manualTrackingTest(){
        mEmptyActivity.launchActivity(null);
        final ProductListTracker productListTracker = Webtrekk.getInstance().getProductListTracker();

        //Tracking position some items
        ProductItem item1 = ProductItem.Handler.getProductItemByIndex(1);
        TrackingParameter parameter = getFromProductItem(item1, ProductParameterBuilder.ActionType.list);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_COST);
        productListTracker.trackProductPositionInList(parameter);

        ProductItem item2 = ProductItem.Handler.getProductItemByIndex(2);
        TrackingParameter parameter2 = getFromProductItem(item2, ProductParameterBuilder.ActionType.list);
        productListTracker.trackProductPositionInList(parameter2);

        TrackingParameter parameter3 = new ProductParameterBuilder("productId3",
                ProductParameterBuilder.ActionType.list).setPosition(3).getResult();
        productListTracker.trackProductPositionInList(parameter3);

        ProductItem item4 = ProductItem.Handler.getProductItemByIndex(4);
        parameter = getFromProductItem(item4, ProductParameterBuilder.ActionType.list);
        productListTracker.trackProductPositionInList(parameter);

        initWaitingForTrack(null);
        productListTracker.send();

        String URL = waitForTrackedURL();
        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);


        assertEquals("list", parcel.getDecodedValue("st") );
        assertEquals("webtrekk_ignore", parcel.getDecodedValue("ct"));
        assertEquals(item1.getEcomParameters()[0]+";"+item2.getEcomParameters()[0] + ";;" +
                        item4.getEcomParameters()[0], parcel.getDecodedValue("cb1"));
        assertEquals(
                item1.getEcomParameters()[1]+";"+item2.getEcomParameters()[1] + ";;" +
                        item4.getEcomParameters()[1], parcel.getDecodedValue("cb2"));
        assertEquals(
                item1.getCategories()[0]+";"+item2.getCategories()[0] + ";;" +
                        item4.getCategories()[0], parcel.getDecodedValue("ca1"));
        assertEquals(item1.getCategories()[1]+";"+item2.getCategories()[1] + ";;" +
                        item4.getCategories()[1], parcel.getDecodedValue("ca2"));

        assertEquals(item1.getId()+";"+item2.getId()+";productId3;"+ item4.getId(), parcel.getDecodedValue("ba"));
        assertEquals(item1.getInd()+";"+item2.getInd()+";3;"+ item4.getInd(), parcel.getDecodedValue("plp"));
        assertEquals(";"+item2.getCost()+";;"+ item4.getCost(), parcel.getDecodedValue("co"));
        assertEquals(item1.getPaymentMethod()+";"+item2.getPaymentMethod()+";;"+ item4.getPaymentMethod(),
                parcel.getDecodedValue("cb761"));
        assertEquals(item1.getShippingService()+";"+item2.getShippingService()+";;"+ item4.getShippingService(),
                parcel.getDecodedValue("cb762"));
        assertEquals(item1.getShippingSpeed()+";"+item2.getShippingSpeed()+";;"+ item4.getShippingSpeed(),
                parcel.getDecodedValue("cb763"));
        assertEquals(item1.getShippingCost()+";"+item2.getShippingCost()+";;"+ item4.getShippingCost(),
                parcel.getDecodedValue("cb764"));
        assertEquals(item1.getGrossMargin()+";"+item2.getGrossMargin()+";;"+ item4.getGrossMargin(),
                parcel.getDecodedValue("cb765"));
        assertEquals(item1.getProductVariant()+";"+item2.getProductVariant()+";;"+ item4.getProductVariant(),
                parcel.getDecodedValue("cb767"));
        assertEquals(getSoldOutString(item1.getProductSoldOut())+";"+
                getSoldOutString(item2.getProductSoldOut())+";;"+
                getSoldOutString(item4.getProductSoldOut()),
                parcel.getDecodedValue("cb760"));

        // Add the same product with different position so product2 has 2 and 5 positions
        parameter2.add(TrackingParameter.Parameter.PRODUCT_POSITION, "5");
        productListTracker.trackProductPositionInList(parameter2);

        initWaitingForTrack(null);
        productListTracker.send();

        //just wait no check is required.
        waitForTrackedURL();

        //View product1 test
        parameter = getFromProductItem(item1, ProductParameterBuilder.ActionType.view);
        productListTracker.trackProduct(parameter);

        initWaitingForTrack(null);
        productListTracker.send();
        URL = waitForTrackedURL();

        parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("view", parcel.getDecodedValue("st") );
        assertEquals(item1.getEcomParameters()[0], parcel.getDecodedValue("cb1"));
        assertEquals(item1.getEcomParameters()[1], parcel.getDecodedValue("cb2"));
        assertEquals(item1.getCategories()[0], parcel.getDecodedValue("ca1"));
        assertEquals(item1.getCategories()[1], parcel.getDecodedValue("ca2"));

        assertEquals(item1.getId(), parcel.getDecodedValue("ba"));
        assertEquals(String.valueOf(item1.getInd()), parcel.getDecodedValue("plp"));
        assertEquals(String.valueOf(item1.getCost()), parcel.getDecodedValue("co"));
        assertEquals(item1.getPaymentMethod(), parcel.getDecodedValue("cb761"));
        assertEquals(item1.getShippingService(), parcel.getDecodedValue("cb762"));
        assertEquals(item1.getShippingSpeed(), parcel.getDecodedValue("cb763"));
        assertEquals(String.valueOf(item1.getShippingCost()), parcel.getDecodedValue("cb764"));
        assertEquals(String.valueOf(item1.getGrossMargin()), parcel.getDecodedValue("cb765"));
        assertEquals(item1.getProductVariant(), parcel.getDecodedValue("cb767"));
        assertEquals(getSoldOutString(item1.getProductSoldOut()),
                parcel.getDecodedValue("cb760"));

        //View product2 test
        parameter = getFromProductItem(item2, ProductParameterBuilder.ActionType.view);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        initWaitingForTrack(null);
        productListTracker.send();
        URL = waitForTrackedURL();

        parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("view", parcel.getDecodedValue("st") );
        assertEquals(item2.getEcomParameters()[0], parcel.getDecodedValue("cb1"));
        assertEquals(item2.getEcomParameters()[1], parcel.getDecodedValue("cb2"));
        assertEquals(item2.getCategories()[0], parcel.getDecodedValue("ca1"));
        assertEquals(item2.getCategories()[1], parcel.getDecodedValue("ca2"));

        assertEquals(item2.getId(), parcel.getDecodedValue("ba"));
        assertEquals("5", parcel.getDecodedValue("plp"));
        assertEquals(String.valueOf(item2.getCost()), parcel.getDecodedValue("co"));
        assertEquals(item2.getPaymentMethod(), parcel.getDecodedValue("cb761"));
        assertEquals(item2.getShippingService(), parcel.getDecodedValue("cb762"));
        assertEquals(item2.getShippingSpeed(), parcel.getDecodedValue("cb763"));
        assertEquals(String.valueOf(item2.getShippingCost()), parcel.getDecodedValue("cb764"));
        assertEquals(String.valueOf(item2.getGrossMargin()), parcel.getDecodedValue("cb765"));
        assertEquals(item2.getProductVariant(), parcel.getDecodedValue("cb767"));
        assertEquals(getSoldOutString(item2.getProductSoldOut()),
                parcel.getDecodedValue("cb760"));

        //Add product2 test
        parameter = getFromProductItem(item2, ProductParameterBuilder.ActionType.add);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        initWaitingForTrack(null);
        productListTracker.send();
        URL = waitForTrackedURL();

        parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("add", parcel.getDecodedValue("st") );
        assertEquals(item2.getEcomParameters()[0], parcel.getDecodedValue("cb1"));
        assertEquals(item2.getEcomParameters()[1], parcel.getDecodedValue("cb2"));
        assertEquals(item2.getCategories()[0], parcel.getDecodedValue("ca1"));
        assertEquals(item2.getCategories()[1], parcel.getDecodedValue("ca2"));

        assertEquals(item2.getId(), parcel.getDecodedValue("ba"));
        assertEquals(String.valueOf(item2.getInd()), parcel.getDecodedValue("plp"));
        assertEquals(String.valueOf(item2.getCost()), parcel.getDecodedValue("co"));
        assertEquals(item2.getPaymentMethod(), parcel.getDecodedValue("cb761"));
        assertEquals(item2.getShippingService(), parcel.getDecodedValue("cb762"));
        assertEquals(item2.getShippingSpeed(), parcel.getDecodedValue("cb763"));
        assertEquals(String.valueOf(item2.getShippingCost()), parcel.getDecodedValue("cb764"));
        assertEquals(String.valueOf(item2.getGrossMargin()), parcel.getDecodedValue("cb765"));
        assertEquals(item2.getProductVariant(), parcel.getDecodedValue("cb767"));
        assertEquals(getSoldOutString(item2.getProductSoldOut()),
                parcel.getDecodedValue("cb760"));

        //Add product1 test
        parameter = getFromProductItem(item1, ProductParameterBuilder.ActionType.add);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        initWaitingForTrack(null);
        productListTracker.send();
        URL = waitForTrackedURL();

        parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("add", parcel.getDecodedValue("st") );
        assertEquals(item1.getEcomParameters()[0], parcel.getDecodedValue("cb1"));
        assertEquals(item1.getEcomParameters()[1], parcel.getDecodedValue("cb2"));
        assertEquals(item1.getCategories()[0], parcel.getDecodedValue("ca1"));
        assertEquals(item1.getCategories()[1], parcel.getDecodedValue("ca2"));

        assertEquals(item1.getId(), parcel.getDecodedValue("ba"));
        assertEquals(String.valueOf(item1.getInd()), parcel.getDecodedValue("plp"));
        assertEquals(String.valueOf(item1.getCost()), parcel.getDecodedValue("co"));
        assertEquals(item1.getPaymentMethod(), parcel.getDecodedValue("cb761"));
        assertEquals(item1.getShippingService(), parcel.getDecodedValue("cb762"));
        assertEquals(item1.getShippingSpeed(), parcel.getDecodedValue("cb763"));
        assertEquals(String.valueOf(item1.getShippingCost()), parcel.getDecodedValue("cb764"));
        assertEquals(String.valueOf(item1.getGrossMargin()), parcel.getDecodedValue("cb765"));
        assertEquals(item1.getProductVariant(), parcel.getDecodedValue("cb767"));
        assertEquals(getSoldOutString(item1.getProductSoldOut()),
                parcel.getDecodedValue("cb760"));

        //Add product7 test (no position)
        parameter = new ProductParameterBuilder("productId7",
                ProductParameterBuilder.ActionType.add).getResult();
        productListTracker.trackProduct(parameter);

        initWaitingForTrack(null);
        productListTracker.send();
        URL = waitForTrackedURL();

        parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("add", parcel.getDecodedValue("st") );
        assertEquals("productId7", parcel.getDecodedValue("ba"));
        assertEquals(null, parcel.getDecodedValue("co"));
        assertEquals(null, parcel.getDecodedValue("cb761"));
        assertEquals(null, parcel.getDecodedValue("plp"));

        //Add product4 no test is required
        parameter = getFromProductItem(item4, ProductParameterBuilder.ActionType.add);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        initWaitingForTrack(null);
        productListTracker.send();
        waitForTrackedURL();

        //call conf request
        parameter = getFromProductItem(item1, ProductParameterBuilder.ActionType.conf);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        parameter = getFromProductItem(item2, ProductParameterBuilder.ActionType.conf);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        parameter = new ProductParameterBuilder("productId7",
                ProductParameterBuilder.ActionType.conf).getResult();
        productListTracker.trackProduct(parameter);

        parameter = getFromProductItem(item4, ProductParameterBuilder.ActionType.conf);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        parameter.add(TrackingParameter.Parameter.PRODUCT_COUNT, "2");
        productListTracker.trackProduct(parameter);

        TrackingParameter commonParameter = new ProductParameterBuilder(ProductParameterBuilder.ActionType.common)
                .setCouponValue("Coupon")
                .setOrderStatus("OrderStatus").getResult();

        initWaitingForTrack(null);
        productListTracker.send(commonParameter);

        URL = waitForTrackedURL();
        parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("conf", parcel.getDecodedValue("st") );
        assertEquals(item2.getEcomParameters()[0]+";"+item1.getEcomParameters()[0] + ";;" +
                item4.getEcomParameters()[0], parcel.getDecodedValue("cb1"));
        assertEquals(
                item2.getEcomParameters()[1]+";"+item1.getEcomParameters()[1] + ";;" +
                        item4.getEcomParameters()[1], parcel.getDecodedValue("cb2"));
        assertEquals(item2.getCategories()[0]+";"+item1.getCategories()[0] + ";;" +
                        item4.getCategories()[0], parcel.getDecodedValue("ca1"));
        assertEquals(item2.getCategories()[1]+";"+item1.getCategories()[1] + ";;" +
                item4.getCategories()[1], parcel.getDecodedValue("ca2"));

        assertEquals(item2.getId()+";"+item1.getId()+";productId7;"+ item4.getId(), parcel.getDecodedValue("ba"));
        assertEquals(item2.getInd()+";"+item1.getInd()+";;"+ item4.getInd(), parcel.getDecodedValue("plp"));
        assertEquals(item2.getCost()+";"+item1.getCost()+";;"+ item4.getCost(), parcel.getDecodedValue("co"));
        assertEquals(item2.getPaymentMethod()+";"+item1.getPaymentMethod()+";;"+ item4.getPaymentMethod(),
                parcel.getDecodedValue("cb761"));
        assertEquals(item2.getShippingService()+";"+item1.getShippingService()+";;"+ item4.getShippingService(),
                parcel.getDecodedValue("cb762"));
        assertEquals(item2.getShippingSpeed()+";"+item1.getShippingSpeed()+";;"+ item4.getShippingSpeed(),
                parcel.getDecodedValue("cb763"));
        assertEquals(item2.getShippingCost()+";"+item1.getShippingCost()+";;"+ item4.getShippingCost(),
                parcel.getDecodedValue("cb764"));
        assertEquals(item2.getGrossMargin()+";"+item1.getGrossMargin()+";;"+ item4.getGrossMargin(),
                parcel.getDecodedValue("cb765"));
        assertEquals(item2.getProductVariant()+";"+item1.getProductVariant()+";;"+ item4.getProductVariant(),
                parcel.getDecodedValue("cb767"));
        assertEquals(getSoldOutString(item2.getProductSoldOut())+";"+
                        getSoldOutString(item1.getProductSoldOut())+";;"+
                        getSoldOutString(item4.getProductSoldOut()),
                parcel.getDecodedValue("cb760"));
        assertEquals(";;;2", parcel.getDecodedValue("qn"));
        assertEquals("Coupon", parcel.getDecodedValue("cb563"));
        assertEquals("OrderStatus", parcel.getDecodedValue("cb766"));
    }

    private TrackingParameter getFromProductItem(ProductItem item, ProductParameterBuilder.ActionType type){
        return new ProductParameterBuilder(item.getId(), type).
                setPosition(item.getInd()).
                setEcommerce(1, item.getEcomParameters()[0]).
                setEcommerce(2, item.getEcomParameters()[1]).
                setProductCategory(1, item.getCategories()[0]).
                setProductCategory(2, item.getCategories()[1]).
                setCost(item.getCost()).
                setPaymentMethod(item.getPaymentMethod()).
                setShippingService(item.getShippingService()).
                setShippingSpeed(item.getShippingSpeed()).
                setShippingCost(item.getShippingCost()).
                setGrossMargin(item.getGrossMargin()).
                setProductVariant(item.getProductVariant()).
                setIsProductSoldOut(item.getProductSoldOut()).getResult();

    }

    private String getSoldOutString(boolean value){
        return value ? "1" : "0";
    }

    @Test
    public void integrationWithRecyclingViewNothingSendTest(){
        mActivityRule.launchActivity(null);
        try {
            onView(withId(R.id.productListRecyclerView)).perform(swipeUp());
            initWaitingForTrack(null, true);
            mActivityRule.getActivity().unregisterTracking();

            //check for nothing to send
            waitForTrackedURL();

            mWebtrekk.getProductListTracker().clearAddPositionData();
        } finally {
            mWebtrekk.getProductListTracker().clearAddPositionData();
        }
    }

    @Test
    public void integrationWithRecyclingViewSendOnFirstPageTest(){
        mActivityRule.launchActivity(null);
        try {
            sleep(2100);
            initWaitingForTrack(null);
            mActivityRule.getActivity().unregisterTracking();

            //check that there request sent
            waitForTrackedURL();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mWebtrekk.getProductListTracker().clearAddPositionData();
        }
    }

    @Test
    public void validateCorrectDataToSend(){
        mActivityRule.launchActivity(null);
        try {
            RecyclerView recyclerView = mActivityRule.getActivity().getRecyclerView();
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
            Set<Integer> positionsTracked = new HashSet<>();
            onView(withId(R.id.productListRecyclerView)).perform(swipeUp());
            sleep(4000);
            addToHash(linearLayoutManager.findFirstCompletelyVisibleItemPosition(),
                    linearLayoutManager.findLastCompletelyVisibleItemPosition(), positionsTracked);

            onView(withId(R.id.productListRecyclerView)).perform(swipeUp());
            sleep(1000);

            onView(withId(R.id.productListRecyclerView)).perform(swipeUp());
            sleep(4000);

            final int lastItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
            addToHash(linearLayoutManager.findFirstCompletelyVisibleItemPosition(),
                    lastItem, positionsTracked);

            initWaitingForTrack(null);
            mActivityRule.getActivity().unregisterTracking();

            //check that there request sent
            List<String> urls = waitForTrackedURLs(new StopListenForURLCallback() {
                @Override
                public boolean stop(String url) {
                    URLParsel parcel = new URLParsel();

                    parcel.parseURL(url);
                    final String[] positionsToCheck =
                            parcel.getDecodedValue(TrackingParameter.Parameter.PRODUCT_POSITION.toString()).split(";");

                    return Integer.valueOf(positionsToCheck[positionsToCheck.length - 1]) == lastItem;
                }
            });

            Set<Integer> positionsResult = new HashSet<>();
            Set<Float> costResult = new HashSet<>();
            Set<String> ecom1Result = new HashSet<>();
            Set<String> ecom2Result = new HashSet<>();

            //fill what should be tracked
            Set<Float> costTracked = new HashSet<>();
            Set<String> ecom1Tracked = new HashSet<>();
            Set<String> ecom2Tracked = new HashSet<>();

            for (int index: positionsTracked) {
                final ProductItem item = ProductItem.Handler.getProductItemByIndex(index);
                costTracked.add(item.getCost());
                ecom1Tracked.add(item.getEcomParameters()[0]);
                ecom2Tracked.add(item.getEcomParameters()[1]);
            }



            for (int i = 0; i < urls.size(); i++){
                URLParsel parcel = new URLParsel();

                parcel.parseURL(urls.get(i));

                positionsResult.addAll(fromStringToInt(Arrays.asList(parcel.getDecodedValue("plp").split(";"))));
                costResult.addAll(fromStringToFloat(Arrays.asList(parcel.getDecodedValue("co").split(";"))));
                ecom1Result.addAll(Arrays.asList(parcel.getDecodedValue("cb1").split(";")));
                ecom2Result.addAll(Arrays.asList(parcel.getDecodedValue("cb2").split(";")));
            }

            //positions
            assertTrue("tracked by SDK:"+ getFlatString(positionsResult) + "should be"+getFlatString(positionsTracked),
                    positionsResult.containsAll(positionsTracked) && positionsTracked.containsAll(positionsResult));
            //cost
            assertTrue("tracked by SDK:"+ getFlatString(costResult) + "should be"+getFlatString(costTracked),
                    costResult.containsAll(costTracked) && costTracked.containsAll(costResult));
            //ecom
            assertTrue("tracked by SDK:"+ getFlatString(ecom1Result) + "should be"+getFlatString(ecom1Tracked),
                    ecom1Result.containsAll(ecom1Tracked) && ecom1Tracked.containsAll(ecom1Result));
            assertTrue("tracked by SDK:"+ getFlatString(ecom2Result) + "should be"+getFlatString(ecom2Tracked),
                    ecom2Result.containsAll(ecom2Tracked) && ecom2Tracked.containsAll(ecom2Result));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mWebtrekk.getProductListTracker().clearAddPositionData();
        }
    }

    @Test
    public void savingPositionToMemory(){
        mEmptyActivity.launchActivity(null);
        ProductListTracker productListTracker = Webtrekk.getInstance().getProductListTracker();

        //Tracking position some items
        ProductItem item6 = ProductItem.Handler.getProductItemByIndex(6);
        TrackingParameter parameter = getFromProductItem(item6, ProductParameterBuilder.ActionType.list);
        productListTracker.trackProductPositionInList(parameter);

        ProductItem item5 = ProductItem.Handler.getProductItemByIndex(5);
        TrackingParameter parameter2 = getFromProductItem(item5, ProductParameterBuilder.ActionType.list);
        productListTracker.trackProductPositionInList(parameter2);

        productListTracker.trackProductPositionInList(parameter);

        initWaitingForTrack(null);
        productListTracker.send();

        waitForTrackedURL();

        //Add product6 and  product5 no test is required
        parameter = getFromProductItem(item6, ProductParameterBuilder.ActionType.add);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        parameter = getFromProductItem(item5, ProductParameterBuilder.ActionType.add);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        initWaitingForTrack(null);
        productListTracker.send();
        waitForTrackedURL();

        //restart WT instance like application restart
        mEmptyActivity.finishActivity();
        releaseWTInstance();
        setupWTInstance();
        initWT();

        mEmptyActivity.launchActivity(null);

        productListTracker = Webtrekk.getInstance().getProductListTracker();

        parameter = getFromProductItem(item5, ProductParameterBuilder.ActionType.conf);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        parameter = getFromProductItem(item6, ProductParameterBuilder.ActionType.conf);
        parameter.getDefaultParameter().remove(TrackingParameter.Parameter.PRODUCT_POSITION);
        productListTracker.trackProduct(parameter);

        initWaitingForTrack(null);
        productListTracker.send();

        String URL = waitForTrackedURL();
        URLParsel parcel = new URLParsel();

        parcel.parseURL(URL);

        assertEquals("conf", parcel.getDecodedValue("st") );
        assertEquals(item6.getId()+";"+item5.getId(), parcel.getDecodedValue("ba"));
        assertEquals(item6.getInd()+";"+item5.getInd(), parcel.getDecodedValue("plp"));

    }

    private void addToHash(int min, int max, Set<Integer> set){
        for (int nummer = min; nummer <= max; nummer++){
            set.add(nummer);
        }
    }

    private Set<Integer> fromStringToInt(Collection<String> set){
        Set<Integer> toSet = new HashSet<>();
        for (String value: set){
            toSet.add(Integer.valueOf(value));
        }
        return toSet;
    }

    private Set<Float> fromStringToFloat(Collection<String> set){
        Set<Float> toSet = new HashSet<>();
        for (String value: set){
            toSet.add(Float.valueOf(value));
        }
        return toSet;
    }

    private <T> String getFlatString(Collection<T> collection){
       return Arrays.toString(collection.toArray());
    }
}
