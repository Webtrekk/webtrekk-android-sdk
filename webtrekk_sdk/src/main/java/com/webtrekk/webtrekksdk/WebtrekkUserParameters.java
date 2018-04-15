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
 * Created by Arsen Vartbaronov on 24.02.16.
 */

package com.webtrekk.webtrekksdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class manages user parameters as part of Cross Device Bridge implementation.
 * Class should be used for customer to set user parameter,
 */
public class WebtrekkUserParameters {

    private final Map<Parameter, String> mParameters = new HashMap<>();
    private final SortedMap<String, String> mCustomParameters  = new TreeMap<String, String>();
    static final Parameter ALL_CDB_PAR[] = {Parameter.CDB_EMAIL_MD5, Parameter.CDB_EMAIL_SHA,
            Parameter.CDB_PHONE_MD5, Parameter.CDB_PHONE_SHA, Parameter.CDB_ADDRESS_MD5, Parameter.CDB_ADDRESS_SHA,
            Parameter.CDB_ANDROID_ID, Parameter.CDB_IOS_ADD_ID, Parameter.CDB_WIN_AD_ID, Parameter.CDB_FACEBOOK_ID,
            Parameter.CDB_TWITTER_ID, Parameter.CDB_GOOGLE_PLUS_ID, Parameter.CDB_LINKEDIN_ID};
    static final int CUSTOM_PAR_BASE_INDEX = 50;
    private static String LAST_CBD_REQUEST_DATE = "LAST_CBD_REQUEST_DATE";
    private static final long DATE_DELIMETER = 1000*60*60*24;

    /**
     * Set Email. String will be normalized - lower case only and no whitespaces
     * MD5 and SHA256 will be sent for normalized string
     * @param email
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setEmail(String email) {
        putMd5ShaPair(Parameter.CDB_EMAIL_MD5, Parameter.CDB_EMAIL_SHA, normalizeEmail(email));
        return this;
    }

    /**
     * Set email as MD5
     * String will be normalized to lower case
     * @param emailMD5
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setEmailMD5(String emailMD5) {
        mParameters.put(Parameter.CDB_EMAIL_MD5, emailMD5.toLowerCase());
        return this;
    }

    /**
     * Set email as SHA256
     * String will be normalized to lower case
     * @param emailSHA256
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setEmailSHA256(String emailSHA256) {
        mParameters.put(Parameter.CDB_EMAIL_SHA, emailSHA256.toLowerCase());
        return this;
    }

    /**
     * Set Phone. It will be normalized - lower case only, no whitespaces, no non-number characters
     * MD5 and SHA256 will be sent for normalized string
     * @param phone phone number
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setPhone(String phone)
    {
        putMd5ShaPair(Parameter.CDB_PHONE_MD5, Parameter.CDB_PHONE_SHA, normalizePhone(phone));
       return this;
    }

    /**
     * Set phone as MD5
     * String will be normalized to lower case
     * @param phoneMD5
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setPhoneMD5(String phoneMD5) {
        mParameters.put(Parameter.CDB_PHONE_MD5, phoneMD5.toLowerCase());
        return this;
    }

    /**
     * Set phone as SHA256
     * String will be normalized to lower case
     * @param phoneSHA256
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setPhoneSHA256(String phoneSHA256) {
        mParameters.put(Parameter.CDB_PHONE_SHA, phoneSHA256.toLowerCase());
        return this;
    }

    /**
     * Set address should be in form prename|surename|zipcode|street|house number
     * address will be normalized
     * @param address
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setAddress(String address)
    {
        //Validate if address is incorrect

        if (address.matches("(.+\\|(.)+){4}")) {
            putMd5ShaPair(Parameter.CDB_ADDRESS_MD5, Parameter.CDB_ADDRESS_SHA, normalizeAddress(address));
        }else
          WebtrekkLogging.log("Address isn't added. Format is incorrect, use this one: prename|surename|zipcode|street|house number");
        return this;
    }

    /**
     * Set address as MD5
     * String will be normalized to lower case
     * @param addressMD5
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setAddressMD5(String addressMD5) {
        mParameters.put(Parameter.CDB_ADDRESS_MD5, addressMD5.toLowerCase());
        return this;
    }

    /**
     * Set address as SHA256
     * String will be normalized to lower case
     * @param addressSHA256
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setAddressSHA256(String addressSHA256) {
        mParameters.put(Parameter.CDB_ADDRESS_SHA, addressSHA256.toLowerCase());
        return this;
    }

    /**
     * Set android ID
     * String will be normalized to lower case
     * @param androidId
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setAndroidId(String androidId) {
        mParameters.put(Parameter.CDB_ANDROID_ID, androidId.toLowerCase());
        return this;
    }

    /**
     * Set iOS id
     * String will be normalized to lower case
     * @param iOSId
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setiOSId(String iOSId) {
        mParameters.put(Parameter.CDB_IOS_ADD_ID, iOSId.toLowerCase());
        return this;
    }

    /**
     * Set WindowsID
     * String will be normalized to lower case
     * @param windowsId
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setWindowsId(String windowsId) {
        mParameters.put(Parameter.CDB_WIN_AD_ID, windowsId.toLowerCase());
        return this;
    }

    /**
     * Set Facebook ID
     * String will be normalized to lower case
     * @param facebookID
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setFacebookID(String facebookID) {
        mParameters.put(Parameter.CDB_FACEBOOK_ID, HelperFunctions.makeSha256(facebookID.toLowerCase()));
        return this;
    }

    /**
     * Set Twitter ID
     * String will be normalized to lower case
     * @param twitterID
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setTwitterID(String twitterID)
    {
        mParameters.put(Parameter.CDB_TWITTER_ID, HelperFunctions.makeSha256(twitterID.toLowerCase()));
        return this;
    }

    /**
     * Set Google Plus ID
     * String will be normalized to lower case
     * @param googlePlusID
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setGooglePlusID(String googlePlusID)
    {
        mParameters.put(Parameter.CDB_GOOGLE_PLUS_ID, HelperFunctions.makeSha256(googlePlusID.toLowerCase()));
        return this;
    }

    /**
     * Set LinkedIn ID
     * String will be normalized to lower case
     * @param linkedInID
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setLinkedInID(String linkedInID)
    {
        mParameters.put(Parameter.CDB_LINKEDIN_ID, HelperFunctions.makeSha256(linkedInID.toLowerCase()));
        return this;
    }

    /**
     * Set Custom User Parameters
     * String will be normalized to lower case
     * @param id id of custom parameter should be id > 0 && id < 30
     * @param value custom user value
     * @return instance of to WebtrekkUserParameters
     */
    public WebtrekkUserParameters setCustom(int id, String value)
    {
        if (id > 0 && id < 30)
        {
            mCustomParameters.put(new Integer(CUSTOM_PAR_BASE_INDEX+id).toString(), value.toLowerCase());
        }else
        {
            WebtrekkLogging.log("Custom parameter isn't set as id is incorrect please use id > 0 && id < 30 ");
        }

        return this;
    }


    private String normalizeEmail(String email)
    {

        return email.toLowerCase().replace(" ", "");
    }

    private String normalizePhone(String phone)
    {
        String ret = phone.toLowerCase().replaceAll("[\\D\\s]", "");

        WebtrekkLogging.log("normalized phone: "+ ret);

        return ret;
    }

    private String normalizeAddress(String address)
    {
        String ret = address.toLowerCase().
                replace("ä", "ae").
                replace("ö", "oe").
                replace("ü", "ue").
                replace("ß", "ss").
                replaceAll("[\\s_-]", "").
                replaceAll("str(\\.)?(\\s|\\|)+", "strasse|");

        WebtrekkLogging.log("normalized address: " + ret);
        return ret;
    }

    private void putMd5ShaPair(Parameter md5Key, Parameter sha256Key, String value)
    {
          mParameters.put(md5Key, HelperFunctions.makeMd5(value));
          mParameters.put(sha256Key, HelperFunctions.makeSha256(value));
    }

    /**
     * Save to settings and clear map from null values  return false if nothing to send
     * @param context
     */
    boolean saveToSettings(Context context)
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);
        SharedPreferences.Editor editor = preferences.edit();

        //Save standard parameters and remove nulls
        saveToSettingAndRemoveNullItemsProcess(context, editor, mParameters, "");

        //Save custom parameters and remove nulls
        saveToSettingAndRemoveNullItemsProcess(context, editor, mCustomParameters, "cdb");

        editor.apply();

        return !mCustomParameters.isEmpty() || !mParameters.isEmpty();
    }

    /**
     * save parameters and remove nulls
     * @param context
     * @param editor
     * @param map
     * @param keyPrefix
     * @param <T>
     */
    private <T> void saveToSettingAndRemoveNullItemsProcess(Context context, SharedPreferences.Editor editor, Map<T, String> map, String keyPrefix)
    {
        for (Iterator<Map.Entry<T, String>> iterator = map.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry<T, String> entry = iterator.next();
            String key = keyPrefix + entry.getKey();

            if (entry.getValue() != null)
                editor.putString(key, entry.getValue());
            else
            {
                editor.remove(key);
                iterator.remove();
            }
        }
    }

    /**
     * restore instace from settings returns false if no item in setting
     * @param context
     * @return
     */
    boolean restoreFromSettings(Context context)
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);

        for (Parameter par: ALL_CDB_PAR)
        {
            if (preferences.contains(par.toString()))
              mParameters.put(par, preferences.getString(par.toString(), ""));
        }

        for (int i = 1; i < 30; i++ )
        {
            String key = new Integer(CUSTOM_PAR_BASE_INDEX+i).toString();
            String prefKey = "cdb"+key;

            if (preferences.contains(prefKey))
              mCustomParameters.put(key,preferences.getString(prefKey, ""));
        }
        return !mCustomParameters.isEmpty() || !mParameters.isEmpty();
    }

    Map<Parameter, String> getParameters()
    {
        return  mParameters;
    }

    SortedMap<String, String> getCustomParameters()
    {
        return  mCustomParameters;
    }

    boolean isAnyNotNullValue()
    {
        for(String value:mParameters.values())
        {
            if (value != null)
                return true;
        }

        for(String value:mCustomParameters.values())
        {
            if (value != null)
                return true;
        }

        return false;
    }


    /**
     * define if CDB request need repeat
     * @param context
     * @return
     */

    static boolean needUpdateCDBRequest(Context context)
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);

        if (preferences.contains(LAST_CBD_REQUEST_DATE)) {
            long dates = preferences.getLong(LAST_CBD_REQUEST_DATE, 0);

            return dates < getCurrentDateCounter();
        }else
            return false;
    }

    /**
     * update date of CDB request
     * @param context
     */
    static void updateCDBRequestDate(Context context)
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);

        preferences.edit().putLong(LAST_CBD_REQUEST_DATE, getCurrentDateCounter()).apply();
    }

    static long getCurrentDateCounter()
    {
        return (long)System.currentTimeMillis()/DATE_DELIMETER;
    }
}
