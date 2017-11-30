package com.webtrekk.webtrekksdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

/**
 * Class is used to generate TrackingParameter for product
 */
public class ProductParameterBuilder {

    public enum ActionType {
        list("list"),
        view("view"),
        add("add"),
        conf("conf"),
        common(""); //used for common parameters

        private final String value;

        ActionType(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

    }

    private final TrackingParameter mParameter = new TrackingParameter();
    private final ActionType mType;

    /**
     * Constructor for Product parameter builder
     *
     * @param productId
     */
    public ProductParameterBuilder(@NonNull String productId, ActionType type) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT, productId);
        mParameter.add(TrackingParameter.Parameter.PRODUCT_STATUS, type.toString());
        mType = type;
    }

    /**
     * Constructor for custom type. Can be used for common parameters
     */
    public ProductParameterBuilder(ActionType type){
        mType = type;
    }


    @NonNull
    public ProductParameterBuilder setPosition(int value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_POSITION, Integer.toString(value));
        return this;
    }

    @NonNull
    public ProductParameterBuilder setCost(float value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_COST, Float.toString(value));
        return this;
    }

    @NonNull
    public ProductParameterBuilder setEcommerce(int index, @NonNull String value) {
        mParameter.add(TrackingParameter.Parameter.ECOM, Integer.toString(index), value);
        return this;
    }

    @NonNull
    public ProductParameterBuilder setProductCategory(int index, @NonNull String value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_CAT, Integer.toString(index), value);
        return this;
    }

    @NonNull
    public ProductParameterBuilder setProductQuantity(int value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_COUNT, Integer.toString(value));
        return this;
    }

    @NonNull
    public ProductParameterBuilder setPaymentMethod(String value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_PAYMENT_METHOD, value);
        return this;
    }

    @NonNull
    public ProductParameterBuilder setShippingService(String value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_SHIPPING_SERVICE, value);
        return this;
    }

    @NonNull
    public ProductParameterBuilder setShippingSpeed(String value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_SHIPPING_SPEED, value);
        return this;
    }

    @NonNull
    public ProductParameterBuilder setShippingCost(float value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_SHIPPING_COST, Float.toString(value));
        return this;
    }

    @NonNull
    public ProductParameterBuilder setGrossMargin(float value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_GROSS_MARGIN, Float.toString(value));
        return this;
    }

    @NonNull
    public ProductParameterBuilder setOrderStatus(String value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_ORDER_STATUS, value);
        return this;
    }

    @NonNull
    public ProductParameterBuilder setProductVariant(String value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_VARIANT, value);
        return this;
    }

    @NonNull
    public ProductParameterBuilder setCouponValue(String value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_COUPON, value);
        return this;
    }

    @NonNull
    public ProductParameterBuilder setIsProductSoldOut(boolean value) {
        mParameter.add(TrackingParameter.Parameter.PRODUCT_SOLD_OUT, value ? "1" : "0");
        return this;
    }
    /**
     * return link to TrackingParameter or null if TrackingParameter for product is not correct
     * @return
     */
    @Nullable
    public TrackingParameter getResult() {
        return validate() ? mParameter : null;
    }

    private boolean validate(){
        final boolean isProduct = mParameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT) != null;
        switch (mType){
            case list:
                return isProduct && mParameter.getDefaultParameter().get(TrackingParameter.Parameter.PRODUCT_POSITION) != null;
            case add:
            case view:
            case conf:
                return isProduct;
            case common:
                return true;
        }
        return false;
    }
}
