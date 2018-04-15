package com.webtrekk.webtrekksdk;

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
 * Created by vartbaronov on 10.11.17.
 */

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;
import com.webtrekk.webtrekksdk.Modules.ProductListOrderSaver;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class ProductListTracker {

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener{

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(@NonNull Context context, final RecyclerView recyclerView){
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });

        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, MotionEvent e) {
            if (mGestureDetector.onTouchEvent(e)){
                mApplyPendingAction.run();
                clearPendingEvents();
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, MotionEvent e) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    }

    /**
     * Simple inteface for application to provide additional information about items in list.
     * It is mandatory at lease provide productId
     */
    public interface ProductListItemCallback{
        /**
         * return parameters for product with certain list position
         * @param position position of product in list
         * @return
         */
        @NonNull
        TrackingParameter getItem(int position);
    }

    final private SortedMap<Integer, TrackingParameter> mProductPositionItems = new TreeMap<>();
    final private SortedMap<Integer, TrackingParameter> mPendingProductPositionItems = new TreeMap<>();
    final private List<TrackingParameter> mProductItems = new ArrayList<>();
    private RecyclerView.OnScrollListener mScrollListener;
    private Handler mDelayHandler;
    final private Runnable mApplyPendingAction = new Runnable() {
        @Override
        public void run() {
            if (!mPendingProductPositionItems.isEmpty()) {
                mProductPositionItems.putAll(mPendingProductPositionItems);
                WebtrekkLogging.log(mPendingProductPositionItems.size() + " products add to tracking");
                mPendingProductPositionItems.clear();
            }
        }
    };

    private RecyclerView.OnItemTouchListener mOnTouсhEventListener;
    private final TrackingConfiguration mTrackingConfiguration;
    private final ProductListOrderSaver mOrderSaver;


    ProductListTracker(TrackingConfiguration configuration, Context context){
        mTrackingConfiguration = configuration;
        mOrderSaver = new ProductListOrderSaver(context);
        mOrderSaver.load();
    }

    /**
     * sends all product tracking list requests to server.
     */
    public void send(){
        send(null);
    }

    /**
     * sends all product tracking list requests to server.
     * @param commonParameters common parameters that is not merged with ";"
     */
    public void send(@Nullable TrackingParameter commonParameters){
        sendProductPositionItems(commonParameters);
        sendProducts(commonParameters);
    }

    /**
     * clear all data about position in list and adding order
     */
    public void clearAddPositionData(){
        mOrderSaver.clear();
    }


    private void sendProductPositionItems(@Nullable TrackingParameter commonParameters){
        //Check if there is anything to send
        if (mProductPositionItems.isEmpty()){
            return;
        }

        trackParameters(mProductPositionItems.values(), commonParameters, true);
        mOrderSaver.saveProductPositions(mProductPositionItems);

        mProductPositionItems.clear();
    }

    /**
     * track parameters with minimum number of requests
     * @param parametersToTrack
     */
    private void trackParameters(Collection<TrackingParameter> parametersToTrack, TrackingParameter commonParameters, boolean addIgnoreAction){

        if (parametersToTrack.isEmpty()){
            return;
        }

        Webtrekk webtrekk = Webtrekk.getInstance();
        TrackingParameter mergedParameters = constructTrackingParameter(addIgnoreAction);
        TrackingParameter mergedBaseParameters = constructTrackingParameter(false);

        //create Base TrackingParameters

        for (TrackingParameter itemToCollect: parametersToTrack){
            mergedBaseParameters.getDefaultParameter().putAll(itemToCollect.getDefaultParameter());
            mergedBaseParameters.getEcomParameter().putAll(itemToCollect.getEcomParameter());
            mergedBaseParameters.getProductCategories().putAll(itemToCollect.getProductCategories());
        }

        for (TrackingParameter item: parametersToTrack){
            TrackingParameter parameters = mergedParameters.mergeProducts(item,
                    mergedBaseParameters, mTrackingConfiguration);

            // parameters value is more then 255
            if (parameters == null){
                webtrekk.track(mergedParameters);
                mergedParameters = constructTrackingParameter(addIgnoreAction);
                // we assume that one product list won't have field with length more then 255
                mergedParameters = mergedParameters.mergeProducts(item,
                        mergedBaseParameters, mTrackingConfiguration);
            } else{
                mergedParameters = parameters;
            }
        }

        //merge common parameters
        if (commonParameters != null){
            mergedParameters.getDefaultParameter().putAll(commonParameters.getDefaultParameter());
            mergedParameters.getEcomParameter().putAll(commonParameters.getEcomParameter());
            mergedParameters.getProductCategories().putAll(commonParameters.getProductCategories());
        }

        webtrekk.track(mergedParameters);
    }

    private void sendProducts(@Nullable TrackingParameter commonParameters){
        if (mProductItems.isEmpty()){
            return;
        }

        //separate products to add view and conf
        List<TrackingParameter> productView = new ArrayList<>();
        List<TrackingParameter> productAdd = new ArrayList<>();
        List<TrackingParameter> productConf = new ArrayList<>();

        for (TrackingParameter parameter: mProductItems){
            final String type = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT_STATUS);
            if (type.equals(ProductParameterBuilder.ActionType.add.toString())){
                productAdd.add(parameter);
            }else if (type.equals(ProductParameterBuilder.ActionType.view.toString())){
                productView.add(parameter);
            }else if (type.equals(ProductParameterBuilder.ActionType.conf.toString())) {
                productConf.add(parameter);
            }
        }

        trackParametersWithType(productView, commonParameters, ProductParameterBuilder.ActionType.view);
        trackParametersWithType(productAdd, commonParameters, ProductParameterBuilder.ActionType.add);
        trackParametersWithType(productConf, commonParameters, ProductParameterBuilder.ActionType.conf);

        if (!productConf.isEmpty()) {
            clearAddPositionData();
        }

        mProductItems.clear();
    }

    private void trackParametersWithType(@NonNull List<TrackingParameter> parameters,
                                         @Nullable TrackingParameter commonParameters,
                                         ProductParameterBuilder.ActionType type){
        if (parameters.isEmpty()){
            return;
        }

        final Comparator<TrackingParameter> comparator = new Comparator<TrackingParameter>() {
            @Override
            public int compare(TrackingParameter parameter, TrackingParameter t1) {
                final String product1 = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT);
                final String product2 = t1.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT);

                final Integer product1Order = mOrderSaver.getProductAddOrderPosition(product1);
                final Integer product2Order = mOrderSaver.getProductAddOrderPosition(product2);

                return product1Order.compareTo(product2Order);
            }
        };

        Collections.sort(parameters, comparator);
        final ListIterator<TrackingParameter> iterator = parameters.listIterator();

        while (iterator.hasNext()){
            TrackingParameter parameter = iterator.next();
            final String productId = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT);

            final int position = type == ProductParameterBuilder.ActionType.view ?
                    mOrderSaver.getProductLastDefinedPosition(productId) :
                    mOrderSaver.getProductFirstDefinedPosition(productId);

            if (position != ProductListOrderSaver.NOT_DEFINED_ORDER){
                parameter.getDefaultParameter().put(TrackingParameter.Parameter.PRODUCT_POSITION,
                        String.valueOf(position));
            }

            if (type == ProductParameterBuilder.ActionType.add){
                mOrderSaver.trackAddedProducts(parameter);
            }
        }

        trackParameters(parameters, commonParameters, false);
    }

    @NonNull
    private TrackingParameter constructTrackingParameter(boolean addIgnoreAction){
        TrackingParameter parameter = new TrackingParameter();
        if (addIgnoreAction) {
            parameter.add(TrackingParameter.Parameter.ACTION_NAME, "webtrekk_ignore");
        }
        return parameter;
    }

    /**
     * Do position tracking. Due to acceptable performance function just remember position. Actual
     * tracking is done in {@link #send()} function that should be called after.
     * @param parameter
     */
    @NonNull
    public void trackProductPositionInList(@NonNull TrackingParameter parameter){

        String positionS = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT_POSITION);
        String typeTracking = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT_STATUS);
        String product = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT);

        if (typeTracking == null || (typeTracking != null && !typeTracking.equals(ProductParameterBuilder.ActionType.list.toString()))){
            WebtrekkLogging.log("Product isn't product list position tracking. Use another method for this");
            return;
        }

        if (positionS == null){
            WebtrekkLogging.log("Error: No position in parameters. Track won't be done");
            return;
        }

        if (product == null){
            WebtrekkLogging.log("Error: No product in parameters. Track won't be done");
            return;
        }

        Integer position = null;

        try {
            position = Integer.valueOf(positionS);
        }catch (NumberFormatException e){}

        if (position == null){
            WebtrekkLogging.log("Error: Incorrect position value in parameters. Track won't be done");
            return;
        }

        mProductPositionItems.put(position, parameter);
    }

    public void trackProduct(@NonNull TrackingParameter parameter) {
        trackProduct(parameter, false);
    }

    public void trackProduct(@NonNull TrackingParameter parameter, boolean sendImmediately){
        String product = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT);
        String typeTracking = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT_STATUS);

        String[] trackingTypes = {ProductParameterBuilder.ActionType.add.toString(),
                ProductParameterBuilder.ActionType.view.toString(),
                ProductParameterBuilder.ActionType.conf.toString()};
        Set<String> trackingTypesCollection = new HashSet<>(Arrays.asList(trackingTypes));


        if (typeTracking == null || (typeTracking != null
                && !trackingTypesCollection.contains(typeTracking))){
            WebtrekkLogging.log("Product isn't either add or view or conf product tracking. Track won't be done");
            return;
        }

        if (product == null){
            WebtrekkLogging.log("Error: No product in parameters. Track won't be done");
            return;
        }

        mProductItems.add(parameter);

        if (sendImmediately){
            send(null);
        }
    }

    /**
     * Register RecyclerView for product list tracking with timeout == 2 sec. Please call {@link #unregisterView(RecyclerView)}
     * after RecyclerView is hided
     * @param view RecyclerView instance
     * @param itemCallback callback that should be used to provide parameters for product
     */
    public void registerView(@NonNull RecyclerView view, @NonNull final ProductListItemCallback itemCallback){
        registerView(view, 2000, itemCallback);
    }

    /**
     * Register Recycler view for product list tracking. Please call {@link #unregisterView(RecyclerView)}
     * after RecyclerView is hided
     * @param view RecyclerView instance
     * @param timeoutMilliseconds timeout that is used when list is scrolled. Position of product
     * is tracked if list has this delay after scrolling or first show-up
     * @param itemCallback - callback to provide information about product
     */

    public void registerView(@NonNull RecyclerView view, @IntRange(from=0) final long timeoutMilliseconds, @NonNull final ProductListItemCallback itemCallback){

        final RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
        //we not support others layout managers then LinearLayoutManager
        if (!(layoutManager instanceof LinearLayoutManager)) {
            WebtrekkLogging.log("Error: not LinearLayouManager isn't supported");
            return;
        }

        mDelayHandler = new Handler();
        mScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    initPendingList(recyclerView, timeoutMilliseconds, itemCallback);
                }else{
                    clearPendingEvents();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //it equals zero on the first list start in that case start pending
                if (dy == 0){
                    initPendingList(recyclerView, timeoutMilliseconds, itemCallback);
                }
            }
        };

        view.addOnScrollListener(mScrollListener);

        mOnTouсhEventListener = new RecyclerItemClickListener(view.getContext(), view);
        view.addOnItemTouchListener(mOnTouсhEventListener);
        initPendingList(view, timeoutMilliseconds, itemCallback);
    }

    /**
     * unregister RecyclerView suggested to call in onStop.
     * @param view RecyclerView instance
     */
    public void unregisterView(@NonNull RecyclerView view){
        if (mScrollListener != null){
            view.removeOnScrollListener(mScrollListener);
        }

        if (mOnTouсhEventListener != null) {
            view.removeOnItemTouchListener(mOnTouсhEventListener);
        }

        clearPendingEvents();

        //send all items
        send();
        mDelayHandler = null;
        mOnTouсhEventListener = null;
        mScrollListener = null;
    }

    private void clearPendingEvents(){
        mPendingProductPositionItems.clear();
        if (mDelayHandler != null) {
            mDelayHandler.removeCallbacks(mApplyPendingAction);
        }
    }

    private void initPendingList(@NonNull RecyclerView view, long timeOut,
                                 ProductListItemCallback itemCallback){
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager)view.getLayoutManager();
        final int firstCompletelyItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        final int lastCompletelyItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();

        for (int position = firstCompletelyItem; position <= lastCompletelyItem; position++){
            if (!mProductPositionItems.containsKey(position) && position >= 0) {
                mPendingProductPositionItems.put(position, itemCallback.getItem(position));
            }
        }

        mDelayHandler.postDelayed(mApplyPendingAction, timeOut);
    }
}

