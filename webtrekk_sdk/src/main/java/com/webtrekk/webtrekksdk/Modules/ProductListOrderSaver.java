package com.webtrekk.webtrekksdk.Modules;

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
 * Created by vartbaronov on 21.11.17.
 */


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.webtrekk.webtrekksdk.ProductParameterBuilder;
import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintain product position and product add order.
 * Database or products is based on the follow JSON:
 [
  {
 "id": "productId1",
 "add_order": 4
 "p_first": 1,
 "p_last": 3
 },
 .....
  ]
 */

public class ProductListOrderSaver {

    private final static String PRODUCTS_ORDER_LIST = "PRODUCTS_ORDER_LIST";
    private final static String JSON_ID_FIELD = "id";
    private final static String JSON_ADD_ORDER_FIELD = "add_order";
    private final static String JSON_PFIRST_FIELD = "p_first";
    private final static String JSON_PLAST_FIELD = "p_last";
    public final static int NOT_DEFINED_ORDER = Integer.MAX_VALUE;
    private int mProductCurrentAddPosition;
    final Context mContext;

    private Map<String, ProductOrderProperties> mProducts = new HashMap<>();

    private static class ProductOrderProperties {
        private int mAddOrder;
        private int mPFirst;
        private int mPLast;

        ProductOrderProperties(){
            mAddOrder = ProductListOrderSaver.NOT_DEFINED_ORDER;
            mPFirst = ProductListOrderSaver.NOT_DEFINED_ORDER;
            mPLast = ProductListOrderSaver.NOT_DEFINED_ORDER;
        }

        void setAddOrder(int addOrder){
            mAddOrder = addOrder;
        }

        void setPFirst(int pFirst){
            mPFirst = pFirst;
        }

        void setPLast(int pLast){
            mPLast = pLast;
        }

        int getAddOrder(){
            return mAddOrder;
        }

        int getPFirst(){
            return mPFirst;
        }

        int getPLast(){
            return mPLast;
        }

    }

    public ProductListOrderSaver(@NonNull Context context){
        mContext = context;
    }

    public void load(){
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(mContext);
        String json = preferences.getString(PRODUCTS_ORDER_LIST, null);

        //exit if there is no shared preferences
        if (json == null){
            return;
        }

        try {
            JSONArray array = new JSONArray(json);
            mProducts.clear();
            mProductCurrentAddPosition = 0;

            for (int i = 0; i < array.length(); i++){
                final JSONObject object = array.getJSONObject(i);
                final ProductOrderProperties properties = new ProductOrderProperties();

                final String id = object.getString(JSON_ID_FIELD);
                final int addOrder = object.getInt(JSON_ADD_ORDER_FIELD);

                properties.setAddOrder(addOrder);
                properties.setPFirst(object.getInt(JSON_PFIRST_FIELD));
                properties.setPLast(object.getInt(JSON_PLAST_FIELD));

                mProducts.put(id, properties);

                if (addOrder > mProductCurrentAddPosition && addOrder != NOT_DEFINED_ORDER) {
                    mProductCurrentAddPosition = addOrder;
                }
            }
        } catch (JSONException e) {
            WebtrekkLogging.log("Incorrect JSON for saved product list order information:" + e.getLocalizedMessage());
        }
    }

    public void savePermanent(){
        final SharedPreferences.Editor editor = HelperFunctions.getWebTrekkSharedPreference(mContext).edit();
        final JSONArray array = new JSONArray();

        try {
        for (Map.Entry<String, ProductOrderProperties> item: mProducts.entrySet()){
            final JSONObject object = new JSONObject();
            final ProductOrderProperties properties = item.getValue();

            object.put(JSON_ID_FIELD, item.getKey());
            object.put(JSON_ADD_ORDER_FIELD, properties.getAddOrder());
            object.put(JSON_PFIRST_FIELD, properties.getPFirst());
            object.put(JSON_PLAST_FIELD, properties.getPLast());
            array.put(object);
        }

            editor.putString(PRODUCTS_ORDER_LIST, array.toString());
            editor.apply();

        } catch (JSONException e) {
            WebtrekkLogging.log("Can't save JSON for saved product list order information:" + e.getLocalizedMessage());
        }
    }

    public void saveProductPositions(@NonNull Map<Integer, TrackingParameter> parametersToTrack){

        for (Map.Entry<Integer, TrackingParameter> parameterEntry: parametersToTrack.entrySet()){
            final String productId = parameterEntry.getValue().getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT);

            if (productId != null) {
                saveProductPosition(productId, parameterEntry.getKey());
            }
        }
        savePermanent();
    }

    private void saveProductPosition(@NonNull String productId, int position){
        ProductOrderProperties properties = getActualProperties(productId);
        if (properties.getPFirst() == NOT_DEFINED_ORDER){
            properties.setPFirst(position);
        } else {
            properties.setPLast(position);
        }
    }

    public void trackAddedProducts(@NonNull TrackingParameter parameter){
        final String productId = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT);
        final String trackType = parameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT_STATUS);
        if (productId != null && trackType != null &&
                trackType.equals(ProductParameterBuilder.ActionType.add.toString())){
            productAdded(productId);
            savePermanent();
        }
    }

    private void productAdded(@NonNull String productId){
        ProductOrderProperties properties = getActualProperties(productId);
        properties.setAddOrder(mProductCurrentAddPosition++);
    }

    public void clear(){
        mProducts.clear();
        final SharedPreferences.Editor editor = HelperFunctions.getWebTrekkSharedPreference(mContext).edit();
        editor.remove(PRODUCTS_ORDER_LIST);
        editor.apply();
        mProductCurrentAddPosition = 0;
    }

    public int getProductFirstDefinedPosition(@NonNull String productId){
        ProductOrderProperties properties = mProducts.get(productId);
        return properties == null ? NOT_DEFINED_ORDER : properties.getPFirst();
    }

    public int getProductLastDefinedPosition(@NonNull String productId){
        ProductOrderProperties properties = mProducts.get(productId);
        int position = NOT_DEFINED_ORDER;

        if (properties != null){
            position = properties.getPLast();
            if (position == NOT_DEFINED_ORDER){
                position = properties.getPFirst();
            }
        }

        return position;
    }

    public int getProductAddOrderPosition(@NonNull String productId){
        ProductOrderProperties properties = mProducts.get(productId);
        return properties == null ? NOT_DEFINED_ORDER : properties.getAddOrder();
    }

    @NonNull
    private ProductOrderProperties getActualProperties(String productId){
        ProductOrderProperties properties = mProducts.get(productId);
        if (properties == null){
            properties = new ProductOrderProperties();
            mProducts.put(productId, properties);
        }

        return properties;
    }
}
