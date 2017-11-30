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
 * Created by Thomas Dahlmann on 11.03.15.
 */
package com.webtrekk.webtrekksdk;

import android.support.annotation.NonNull;

import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;
import com.webtrekk.webtrekksdk.Request.TrackingRequest;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * this class deals with all the possible tracking parameters, it offers a static enum for all the valid ones
 * it also allows to return the trackingParameter as url, and maybe later as json as well
 * it offers the user some helper functions for easy of use, and will be created before a tracking request is send
 * the manual information has to be added by the user, the automatic information will be added by the sdk
 */

public class TrackingParameter {

    //Separator for product list merge
    final public static String PRODUCT_LIST_SEPARATOR = ";";
    final private static int MAX_PARAMETER_LENGTH = 255;
    //~8 KB because some parameter don't exist during size validation add 200 symbols, just in case
    final private static int MAX_QUERY_LENGTH = 8*1024 - 200;
    // general tracking trackingParameter
    private SortedMap<Parameter, String> defaultParameter;
    // customer trackingparams, defined by the app
    private SortedMap<String, String> pageParameter;
    private SortedMap<String, String> sessionParameter;
    private SortedMap<String, String> ecomParameter;
    private SortedMap<String, String> userCategories;
    private SortedMap<String, String> pageCategories;
    private SortedMap<String, String> adParameter;
    private SortedMap<String, String> actionParameter;
    private SortedMap<String, String> productCategories;
    private SortedMap<String, String> mediaCategories;
    private SortedMap<String, String> mCustomUserParameters;


    public TrackingParameter() {
        this.defaultParameter = new TreeMap<Parameter, String>();
        this.pageParameter = new TreeMap<String, String>();
        this.sessionParameter = new TreeMap<String, String>();
        this.ecomParameter = new TreeMap<String, String>();
        this.userCategories = new TreeMap<String, String>();
        this.pageCategories = new TreeMap<String, String>();
        this.adParameter = new TreeMap<String, String>();
        this.actionParameter = new TreeMap<String, String>();
        this.productCategories = new TreeMap<String, String>();
        this.mediaCategories = new TreeMap<String, String>();
        //Used for CDB feature;
        mCustomUserParameters = new TreeMap<String, String>();
    }

    /*
     * this method adds a tracking param to the HashMap
     * if the key already exists it will be updated
     */
    public TrackingParameter add(Parameter key, String value) {
        defaultParameter.put(key, value);
        return this;
    }

    /**
     * this method allows to merge two trackingparameter objects in single objects
     * @param tp
     * @return
     */
    public TrackingParameter add(TrackingParameter tp) {
        if(tp == null) {
            WebtrekkLogging.log("Error: TrackingParameter object passed to add method is null");
            return this;
        }
        this.defaultParameter.putAll(tp.getDefaultParameter());
        this.pageParameter.putAll(tp.getPageParameter());
        this.sessionParameter.putAll(tp.getSessionParameter());
        this.ecomParameter.putAll(tp.getEcomParameter());
        this.userCategories.putAll(tp.getUserCategories());
        this.pageCategories.putAll(tp.getPageCategories());
        this.adParameter.putAll(tp.getAdParameter());
        this.actionParameter.putAll(tp.getActionParameter());
        this.productCategories.putAll(tp.getProductCategories());
        this.mediaCategories.putAll(tp.getMediaCategories());
        mCustomUserParameters.putAll(tp.getCustomUserParameters());
        return this;
    }

    public boolean containsKey(Parameter key) {
        return defaultParameter.containsKey(key);
    }

    //TODO: noch mehr add Methoden für int/long/double usw, dann muss der nutzer nicht mehr manuell String.valueOf nehmen beispiel mediatracking positionen

    //TODO: maybe make the index to int, in case this is really always a number

    public TrackingParameter add(Parameter key, String index, String value) {

        String valueToAdd = null;
        if (value != null && value.length() > TrackingParameter.MAX_PARAMETER_LENGTH)
        {
            WebtrekkLogging.log("Parameter is more then "+TrackingParameter.MAX_PARAMETER_LENGTH+" length. Truncate");
            valueToAdd = value.substring(0, TrackingParameter.MAX_PARAMETER_LENGTH);
        }else
            valueToAdd = value;

        switch(key) {
            case ACTION:
                this.actionParameter.put(index, valueToAdd);
                break;
            case PAGE:
                this.pageParameter.put(index, valueToAdd);
                break;
            case SESSION:
                this.sessionParameter.put(index, valueToAdd);
                break;
            case ECOM:
                this.ecomParameter.put(index, valueToAdd);
                break;
            case AD:
                this.adParameter.put(index, valueToAdd);
                break;
            case USER_CAT:
                this.userCategories.put(index, valueToAdd);
                break;
            case PAGE_CAT:
                this.pageCategories.put(index, valueToAdd);
                break;
            case PRODUCT_CAT:
                this.productCategories.put(index, valueToAdd);
                break;
            case MEDIA_CAT:
                this.mediaCategories.put(index, valueToAdd);
                break;
            default:
                WebtrekkLogging.log( "invalid trackingparam type");
                throw new IllegalArgumentException("invalid TrackingParameter type");
        }
        return this;
    }

    /**
     * this function adds the auto tracked values to the trackingparams map
     * it will be called during before the request is send by the sdk
     * @param auto_tracked_values
     * @return
     *
     */
    public TrackingParameter add(Map<Parameter, String> auto_tracked_values) {
        for(Map.Entry<Parameter, String> entry : auto_tracked_values.entrySet()) {
            defaultParameter.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public SortedMap<Parameter, String> getDefaultParameter() {
        return defaultParameter;
    }

    public SortedMap<String, String> getPageParameter() {
        return pageParameter;
    }

    public SortedMap<String, String> getSessionParameter() {
        return sessionParameter;
    }

    public SortedMap<String, String> getEcomParameter() {
        return ecomParameter;
    }

    public SortedMap<String, String> getCustomUserParameters() {
        return mCustomUserParameters;
    }

    public SortedMap<String, String> getUserCategories() {
        return userCategories;
    }

    public SortedMap<String, String> getPageCategories() {
        return pageCategories;
    }

    public SortedMap<String, String> getAdParameter() {
        return adParameter;
    }

    public SortedMap<String, String> getActionParameter() {
        return actionParameter;
    }

    public SortedMap<String, String> getProductCategories() {
        return productCategories;
    }

    private void setDefaultParameter(SortedMap<Parameter, String> defaultParameter) {
        this.defaultParameter = defaultParameter;
    }

    public SortedMap<String, String> getMediaCategories() {
        return mediaCategories;
    }

    private void setPageParameter(SortedMap<String, String> pageParameter) {
        this.pageParameter = pageParameter;
    }

    private void setSessionParameter(SortedMap<String, String> sessionParameter) {
        this.sessionParameter = sessionParameter;
    }

    private void setEcomParameter(SortedMap<String, String> ecomParameter) {
        this.ecomParameter = ecomParameter;
    }

    private void setUserCategories(SortedMap<String, String> userCategories) {
        this.userCategories = userCategories;
    }

    private void setPageCategories(SortedMap<String, String> pageCategories) {
        this.pageCategories = pageCategories;
    }

    private void setAdParameter(SortedMap<String, String> adParameter) {
        this.adParameter = adParameter;
    }

    private void setActionParameter(SortedMap<String, String> actionParameter) {
        this.actionParameter = actionParameter;
    }

    private void setProductCategories(SortedMap<String, String> productCategories) {
        this.productCategories = productCategories;
    }

    private void setMediaCategories(SortedMap<String, String> mediaCategories) {
        this.mediaCategories = mediaCategories;
    }

    public void setCustomUserParameters(SortedMap<String, String> customUserParameters) {
        mCustomUserParameters = customUserParameters;
    }

    /**
     * this enum contains all valid tracking parameters and their url string identifier
     */
    public enum Parameter {
        /**
         * single value parameter
         */
        SCREEN_RESOLUTION("res"),
        SCREEN_DEPTH("depth"),

        SAMPLING("ps"), // to submit the sampling value with each request which is an integer like 10
        TIMESTAMP("ts"),
        CURRENT_TIME("mts"),
        IP_ADDRESS("X_WT_IP"), //TODO: no internal way to get the external address, discuss which is really useful here
        USERAGENT("X-WT-UA"),
        TIMEZONE("tz"),
        DEV_LANG("la"),
        EVERID("eid"),
        APP_FIRST_START("one"),
        ACTION_NAME("ct"),
        VOUCHER_VALUE("cb563"), //Gutscheinwert
        ORDER_TOTAL("ov"), // Gesamtbestellwert
        ORDER_NUMBER("oi"), // Bestellnummer
        PRODUCT("ba"), //produkt
        PRODUCT_COST("co"), // produktkosten
        CURRENCY("cr"),
        PRODUCT_COUNT("qn"), // produkt anzahl
        PRODUCT_STATUS("st"), // produkt status ( ad, view, conf, list)
        PRODUCT_POSITION("plp"),
        PRODUCT_PAYMENT_METHOD("cb761"),
        PRODUCT_SHIPPING_SERVICE("cb762"),
        PRODUCT_SHIPPING_SPEED("cb763"),
        PRODUCT_SHIPPING_COST("cb764"),
        PRODUCT_GROSS_MARGIN("cb765"),
        PRODUCT_ORDER_STATUS("cb766"),
        PRODUCT_VARIANT("cb767"),
        PRODUCT_COUPON("cb563"),
        PRODUCT_SOLD_OUT("cb760"),
        CUSTOMER_ID("cd"), // kundennnummer
        EMAIL("uc700"), // email
        EMAIL_RID("uc701"), // email rid
        NEWSLETTER("uc702"),
        GNAME("uc703"), // vorname
        SNAME("uc704"), // nachname
        PHONE("uc705"), // telefonnummer
        GENDER("uc706"), //geschlecht
        BIRTHDAY("uc707"), // geburtstag
        CITY("uc708"),
        COUNTRY("uc709"),
        ZIP("uc710"), // postleitzahl
        STREET("uc711"),
        STREETNUMBER("uc712"),
        INTERN_SEARCH("is"),
        ADVERTISEMENT("mc"), // Werbemittel
        ADVERTISEMENT_ACTION("mca"), // Werbemittel
        ADVERTISER_ID("geid"),
        PAGE_URL("pu"),

        /**
         * Media tracking parameter
         */

        MEDIA_FILE("mi"), // media datei
        MEDIA_ACTION("mk"), //play,pause,stop,pos,seek,eof
        MEDIA_POS("mt1"), //aktuelle position
        MEDIA_LENGTH("mt2"), // laenge der aktuellen media datei
        MEDIA_BANDWITH("bw"), // bandbreite der mediendatei
        MEDIA_VOLUME("vol"), // lautstaerke der mediendatei
        MEDIA_MUTED("mut"),
        MEDIA_TIMESTAMP("x"),// timestamp um caching zu umgehen

        /**
         * tracking Cross Device Bridge parameters
         */
        CDB_EMAIL_MD5("cdb1"),
        CDB_EMAIL_SHA("cdb2"),
        CDB_PHONE_MD5("cdb3"),
        CDB_PHONE_SHA("cdb4"),
        CDB_ADDRESS_MD5("cdb5"),
        CDB_ADDRESS_SHA("cdb6"),
        CDB_ANDROID_ID("cdb7"),
        CDB_IOS_ADD_ID("cdb8"),
        CDB_WIN_AD_ID("cdb9"),
        CDB_FACEBOOK_ID("cdb10"),
        CDB_TWITTER_ID("cdb11"),
        CDB_GOOGLE_PLUS_ID("cdb12"),
        CDB_LINKEDIN_ID("cdb13"),

        /**
         * Install request
         */

        INST_TRACK_ID("trackid"),
        INST_AD_ID("aid"),
        INST_CLICK_ID("clickid"),

        /**
         * multiple value trackingParameter and customer trackingParameter
         */

        PAGE("cp"),
        SESSION("cs"),
        ECOM("cb"),
        AD("cc"),
        ACTION("ck"),
        USER_CAT("uc"),
        PAGE_CAT("cg"),
        PRODUCT_CAT("ca"),
        MEDIA_CAT("mg"),
        ACTIVITY_CAT(""),


        /**
         * unclear / TODO: remove and use as custom trackingParameter
         */
        /**
        @deprecated
         you should use {@link com.webtrekk.webtrekksdk.Webtrekk#setCustomPageName(String)} instead
         */
        ACTIVITY_NAME("aname"),
        INSTALL_REFERRER_PARAMS_MC("wt_mc"), // for the referrer tracking
        INSTALL_REFERRER_KEYWORD("wt_kw"), // for the referrer tracking
        FORCE_NEW_SESSION("fns");

        private final String value;

        Parameter(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static Parameter getParameterByName(String name) {
            for(Parameter p: Parameter.values()) {
                if(name.equals(p.name())) {
                    return p;
                }
            }
            return null;
        }
    }

    /**
     * this method maps any values from mapping values to all the stored parameters
     * it matches by the key of the stored parameter and the mapping value, and replaces
     * the value of the stored parameter with the value from the mappingValues map
     * this method must not modify the original object as its the configuration and key may change dynamicly
     * so the original configuration must contain the placeholders
     *
     * @param mappingValues
     */
    public TrackingParameter applyMapping(Map<String, String> mappingValues) {
        // create a new mapped trackingparameter object
        TrackingParameter mappedTrackingParameter = new TrackingParameter();
        mappedTrackingParameter.setDefaultParameter(applySingleMapping(defaultParameter, mappingValues));
        mappedTrackingParameter.setActionParameter(applySingleMapping(actionParameter, mappingValues));
        mappedTrackingParameter.setAdParameter(applySingleMapping(adParameter, mappingValues));
        mappedTrackingParameter.setEcomParameter(applySingleMapping(ecomParameter, mappingValues));
        mappedTrackingParameter.setPageParameter(applySingleMapping(pageParameter, mappingValues));
        mappedTrackingParameter.setProductCategories(applySingleMapping(productCategories, mappingValues));
        mappedTrackingParameter.setSessionParameter(applySingleMapping(sessionParameter, mappingValues));
        mappedTrackingParameter.setUserCategories(applySingleMapping(userCategories, mappingValues));
        mappedTrackingParameter.setPageCategories(applySingleMapping(pageCategories, mappingValues));
        mappedTrackingParameter.setMediaCategories(applySingleMapping(mediaCategories, mappingValues));
        return mappedTrackingParameter;
    }

    private <T> SortedMap<T, String> applySingleMapping(SortedMap<T, String> original, Map<String, String> mappingValues) {
        SortedMap<T, String> mappedValues = new TreeMap<T, String>();
        for (Map.Entry<T, String> entry : original.entrySet()) {
            String key = entry.getValue();
            if(mappingValues.containsKey(key)) {
                //entry.setValue(mappingValues.get(entry.getValue()));
                mappedValues.put(entry.getKey(), mappingValues.get(key));
            } else {
                // pass empty string if no mapping value is found in the custom parameter, could also use get with default here
                mappedValues.put(entry.getKey(), "");
            }
        }
        return mappedValues;
    }

    /**merge products in two Tracking parameters with product.
     *
     * @param mergedFrom parameters that have additional product to merge
     * @return TrackingParameter instance with merged results or null if size of any parameter more then {@link #MAX_PARAMETER_LENGTH}
     */

    public TrackingParameter mergeProducts(TrackingParameter mergedFrom, TrackingParameter baseParameter,
                                           TrackingConfiguration configuration){

        // merge default parameters
        boolean more255 = false;

        final TrackingParameter mergedResult = new TrackingParameter();
        final SortedMap<Parameter, String> mergedDefaults =
                mergeMaps(defaultParameter, mergedFrom.getDefaultParameter(),
                        baseParameter.getDefaultParameter().keySet());

        // can't merge this parameters query is too long
        if (mergedDefaults == null){
            return null;
        }

        mergedResult.setDefaultParameter(mergedDefaults);

        final SortedMap<String, String> eComParameters = mergedFrom.getEcomParameter();

        if (ecomParameter != null){
            SortedMap<String, String> mergedEcomParameters =
                    mergeMaps(ecomParameter, mergedFrom.getEcomParameter(),
                            baseParameter.getEcomParameter().keySet());

            // can't merge this parameters query is too long
            if (mergedEcomParameters == null){
                return null;
            }
            mergedResult.setEcomParameter(mergedEcomParameters);
        }

        final SortedMap<String, String> eProductCategory = mergedFrom.getProductCategories();

        if (productCategories != null){
            SortedMap<String, String> mergedProductCategories =
                    mergeMaps(productCategories, mergedFrom.getProductCategories(),
                            baseParameter.getProductCategories().keySet());

            // can't merge this parameters query is too long
            if (mergedProductCategories == null){
                return null;
            }
            mergedResult.setProductCategories(mergedProductCategories);
        }

        if (validateQuerySize(mergedResult, configuration)){
            return mergedResult;
        } else{
            WebtrekkLogging.log("size more than 8 KB");
            return null;
        }
    }

    /**
     * merge two maps and concatenate values with {@link #PRODUCT_LIST_SEPARATOR} character
     * @param mergeTo base map
     * @param mergeFrom map that have additional items to merge
     * @param <T> type of key
     * @return result
     */
    @NonNull
    private <T> SortedMap<T, String> mergeMaps(SortedMap<T, String> mergeTo,
                                               SortedMap<T, String> mergeFrom,
                                               Set<T> baseKey){

        final SortedMap<T, String> mergedResult = new TreeMap<>(mergeTo);
        final Set<String> exceptionsKey =
                new HashSet<>(Arrays.asList(new String[] {Parameter.PRODUCT_STATUS.toString(),
                        Parameter.ACTION_NAME.toString()}));
        boolean more255 = false;

        for (T key: baseKey){
            String valueMergeFrom = mergeFrom.get(key);;
            String valueMergeTo = mergeTo.get(key);
            valueMergeFrom = valueMergeFrom == null ? "" : valueMergeFrom;

            final String keyAsString = key.toString();

            if (valueMergeFrom == null && valueMergeTo == null){
                WebtrekkLogging.log("incorrect baseKey list");
                continue;
            }
            if (exceptionsKey.contains(keyAsString)){
                    valueMergeTo = valueMergeFrom;
            }else {
                if (valueMergeTo == null){
                    valueMergeTo = valueMergeFrom;
                } else {
                    valueMergeTo += PRODUCT_LIST_SEPARATOR + valueMergeFrom;
                }
            }

            if (valueMergeTo.length() <=  TrackingParameter.MAX_PARAMETER_LENGTH){
                mergedResult.put(key, valueMergeTo);
            }else{
                more255 = true;
                break;
            }
        }

        return more255 ? null : mergedResult;
    }

    private boolean validateQuerySize(TrackingParameter parameter, TrackingConfiguration configuration){
        TrackingRequest request = new TrackingRequest(parameter, configuration);
        return request.getRequestSize() <= TrackingParameter.MAX_QUERY_LENGTH;
    }
}
