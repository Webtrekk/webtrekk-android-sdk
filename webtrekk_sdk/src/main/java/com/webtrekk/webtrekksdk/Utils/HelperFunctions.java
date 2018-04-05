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
 * Created by Thomas Dahlmann on 01.03.15.
 */

package com.webtrekk.webtrekksdk.Utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.WindowManager;

import com.webtrekk.webtrekksdk.Request.RequestProcessor;
import com.webtrekk.webtrekksdk.Webtrekk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * this class contains various static helper functions to get device details
 */
final public class HelperFunctions {

    private static final String MEDIA_CODE_DEEP_LINK = "DEEP_LINK_SETTINGS_MEDIA_CODE";
    /**
     * private constructor as this is a utility class
     */
    private HelperFunctions() {

    }

    /**
     * returns a string of the display resolution, like 400x300
     *
     * @param context
     * @return
     */
    public static String getResolution(Context context) {
        String resolution = "";
        try {
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(dm);

            resolution = String.format("%sx%s", dm.widthPixels, dm.heightPixels);
        } catch (Exception e) {
            Log.w("WTrack", "Cannot grab resolution", e);
        }
        return resolution;
    }

    /**
     * gets the screendepth, as its 32 default, return this value
     *
     * @param context
     * @return
     */
    public static String getDepth(Context context) {
        // since android 2.3
        return "32";
        //WindowManager wm = (WindowManager) app.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        //int depth = wm.getDefaultDisplay().getPixelFormat();
        //return String.format("%s", depth);
    }

    /**
     * returns the default language of the device as human readable String
     *
     * @return
     */
    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * returns the UTC Offset of the current timezone at the current time, in hours
     *
     * @return
     */
    public static String getTimezone() {
        return "" + (TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60);
    }

    /**
     * returns the name of the os, defualt android is its an android lib
     *
     * @return
     */
    public static String getOSName() {
        return "Android";
    }

    /**
     * returns the release version of the os, as human readable String like : Lollipop
     *
     * @return
     */
    public static String getOSVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * returns the API level, like 22 for Lollipop
     *
     * @return
     */
    public static String getAPILevel() {
        return String.format("%s", Build.VERSION.SDK_INT);
    }

    /**
     * returns the manufactures device string
     *
     * @return
     */
    public static String getDevice() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    /**
     * returns the current country, based on the default locale of the device as country code
     * returns empty string when no matching default is found
     *
     * @return
     */
    public static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    /**
     * returns the user agent string of the default browser
     *
     * @return
     */
    public static String getHttpUserAgent() {
        return System.getProperty("http.agent");
    }

    /**
     * reads the contact information of the user like email, name, sname
     * important: you need to set read_profile/read_contacts permissions for this to work
     *
     * @param context
     * @return
     */
    public static Map<String, String> getUserProfile(Context context) {
        // TODO: this method needs special permissions, so i would at least add an opt out possibility here
        // this method only works on android > 4.0, for lower versions use accountmanager

        String[] profilequery = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.Contacts.Data.MIMETYPE
        };
        final ContentResolver content = context.getContentResolver();
        final Cursor cursor = content.query(
                // Retrieves data rows for the device user's 'profile' contact
                Uri.withAppendedPath(
                        ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                profilequery,

                // Selects only email addresses or names
                ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                        + ContactsContract.Contacts.Data.MIMETYPE + "=?",
                new String[]{
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                },
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
        );

        HashMap<String, String> result = new HashMap<>();
        String mime_type;
        while (cursor.moveToNext()) {
            // get the typ, 4.column in the query result
            mime_type = cursor.getString(4);
            if (mime_type.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                // this automaticly stores the first found email in the result, but its sorted by primary, so if a user has a primary one, its the default
                result.put("email", cursor.getString(0));
            } else if (mime_type.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                result.put("gname", cursor.getString(2));
                result.put("sname", cursor.getString(3));
            }
        }
        cursor.close();
        return result;
    }

    public static String getMailByAccountManager(Context context) {
        AccountManager accManager = AccountManager.get(context);
        Pattern gmailPattern = Patterns.EMAIL_ADDRESS;
        Account acc[] = accManager.getAccountsByType("com.google");
        for (Account account : acc) {
            if (gmailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        WebtrekkLogging.log("could not get valid Account Email, check Permissions. Account:"+ Arrays.toString(accManager.getAccounts()));
        return "";
    }

    /**
     * generates the everid, a unique identifier for the tracking
     *
     * @return
     */
    public static String generateEverid() {
        String everId = "";
        long date = System.currentTimeMillis() / 1000;
        Random random = new Random();

        everId = "6" + String.format(
                "%010d%08d",
                date,
                Long.valueOf(random.nextInt(100000000))
        );

        return everId;
    }

    public static String getEverId(Context context) {
        SharedPreferences preferences = getWebTrekkSharedPreference(context);
        if (!preferences.contains(Webtrekk.PREFERENCE_KEY_EVER_ID)) {
            preferences.edit().putString(Webtrekk.PREFERENCE_KEY_EVER_ID, HelperFunctions.generateEverid()).apply();
            // for compatibility reasons put the key here for new installation
            //preferences.edit().putString(Webtrekk.PREFERENCE_KEY_INSTALLATION_FLAG, "1");
        }
        return preferences.getString(Webtrekk.PREFERENCE_KEY_EVER_ID, "");
    }

    public static void setEverId(Context context, String value) {
        SharedPreferences preferences = getWebTrekkSharedPreference(context);
        preferences.edit().putString(Webtrekk.PREFERENCE_KEY_EVER_ID, value).apply();
    }


    public static long getAdClearId(Context context) {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);
        if (!preferences.contains(AdClearIdUtil.PREFERENCE_KEY_ADCLEAR_ID)) {
            preferences.edit().putLong(AdClearIdUtil.PREFERENCE_KEY_ADCLEAR_ID, new AdClearIdUtil().generateAdClearId()).apply();
        }
        return preferences.getLong(AdClearIdUtil.PREFERENCE_KEY_ADCLEAR_ID, 0);
    }


    /**
     * returns the version of the application
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        // android studio has this as static var in buildconfig, later switch
        // return BuildConfig.VERSION_NAME
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            WebtrekkLogging.log(e.getMessage());
            return "";
        }
    }

    /**
     * returns the version code of the playstore from the app
     * each version code of an uploaded app in the playstore must be higher than the previous one
     *
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        // android studio has this as static var in buildconfig, later switch
        // return BuildConfig.VERSION_CODE
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            WebtrekkLogging.log(e.getMessage());
            return -1;
        }
    }

    public static void setAppVersionCode(int versionCode, Context context) {
        SharedPreferences preferences = getWebTrekkSharedPreference(context);
        preferences.edit().putInt(Webtrekk.PREFERENCE_APP_VERSIONCODE, versionCode).apply();
    }

    /**
     * this method checks if the application was updated, therefore it writes its version_code in a shared preference
     * and in case its a higher number on the next start, it was updated
     *
     * @param context
     * @return
     */
    public static boolean updated(Context context, int currentVersion) {
        //process old style of version as well. It has string value. If it is string - consider it as old one.
        Object storedVersionObj = getWebTrekkSharedPreference(context).getAll().get(Webtrekk.PREFERENCE_APP_VERSIONCODE);
        boolean isOldSettingVersion = storedVersionObj == null || storedVersionObj instanceof String;
        boolean isUpdated;

        if (!isOldSettingVersion) {
            int stored_version = (Integer) storedVersionObj;
            isUpdated = currentVersion > stored_version;
        }else
           isUpdated = true;

        if (isUpdated)
            setAppVersionCode(currentVersion, context);

        return isUpdated;
    }

    /**
     * returns the user agent string for the device
     *
     * @return
     */
    public static String getUserAgent() {
        return "Tracking Library " + Webtrekk.mTrackingLibraryVersionUI + " ("+HelperFunctions.getOSName() + " " + HelperFunctions.getOSVersion() + "; " + HelperFunctions.getDevice() + "; " + Locale.getDefault() + ")";
    }

    /**
     * returns true when the app is started for the first time,
     * it detects the first start when there is no everid set by the application
     *
     * @param context
     * @return
     */
    public static boolean firstStart(Context context) {
        // if no everid is set, this is the first start
        SharedPreferences preferences = getWebTrekkSharedPreference(context);
        return !preferences.contains(Webtrekk.PREFERENCE_KEY_EVER_ID);
    }


    public static SharedPreferences getWebTrekkSharedPreference(Context context) {
        return context.getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * this is true when its a system app or was preinstalled on the phone
     *
     * @param context
     * @return
     */
    public static boolean isAppPreinstalled(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        if (info != null && info.flags != 0) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNewInstallation(Context context) {
        SharedPreferences preferences = getWebTrekkSharedPreference(context);
        String installationFlag = preferences.getString(Webtrekk.PREFERENCE_KEY_INSTALLATION_FLAG, null);
        return (installationFlag != null && installationFlag.equals("1"));

    }

    /**
     * this function checks if the user has auto screen rotation disabled
     *
     * @return
     */
    public static boolean isSysAutoRotate(Context context) {
        int sysAutoRotate = 0;
        try {
            sysAutoRotate = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
        } catch (Settings.SettingNotFoundException ex) {
            WebtrekkLogging.log("error getting sysAutoRotate settings", ex);
        }
        if (sysAutoRotate > 0) {
            return true;
        }
        return false;
    }

    /**
     * this function returns the current device orientation ignoring the rotation as string
     */
    public static String getOrientation(Context context) {
        String orientationString = "undefined";
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == 2) {
                orientationString = "landscape";
            } else if (orientation == 1) {
                orientationString = "portrait";
            } else {
                orientationString = "undefined";
            }
        } catch (Exception e) {
            WebtrekkLogging.log("error getting orientation settings", e);
        }
        return orientationString;
    }

    /**
     * returns the networkInfo
     *
     * @param context
     * @return
     */

    private static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo;
    }

    /**
     * returns the type of the internet connection as string
     *
     * @param context
     * @return
     */
    public static String getConnectionString(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo == null || !networkInfo.isConnected()) {
            return "offline";
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return "WIFI";
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = networkInfo.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return "4G";
                default:
                    return "unknown";
            }
        } else {
            return "unknown";
        }
    }

    /**
     * returns true if the device uses roaming
     *
     * @param context
     * @return
     */
    public static boolean isRoaming(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo != null) {
            return networkInfo.isRoaming();
        }
        return false;

    }

    /**
     * url encodes the given string as utf8
     * replaces special strings like " " with %20
     *
     * @param string
     * @return
     */
    public static String urlEncode(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String urlDecode(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }

        try {
            return URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if there is wifi or mobile connection available
     *
     * @param context The application context
     * @return true if there is network connection available
     */
    public static boolean isNetworkConnection(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Reads a stream and writes it into a string. Closes inputStream when done.
     *
     * @param inputStream The stream to read
     * @return A string, containing stream data
     * @throws java.io.IOException
     */
    public static String stringFromStream(InputStream inputStream) throws java.io.IOException {
        String encoding = "UTF-8";
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }

    public String getXmlFromUrl(String url) throws IOException {
        String xml = null;
        // defaultHttpClient
        HttpURLConnection urlConnection;
        InputStream is = null;
        try {
            URL trackingConfigurationUrl = new URL(url);
            urlConnection = (HttpURLConnection) trackingConfigurationUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(RequestProcessor.NETWORK_CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(RequestProcessor.NETWORK_CONNECTION_TIMEOUT);
            urlConnection.setRequestProperty("Content-Type", "application/xml");
            urlConnection.connect();
            int response = urlConnection.getResponseCode();
            is = urlConnection.getInputStream();
            String xmlConfiguration = stringFromStream(is);
            return xmlConfiguration;
        } catch (MalformedURLException e) {
            WebtrekkLogging.log("getXmlFromUrl: invalid URL", e);
            return null;
        } catch (ProtocolException e) {
            WebtrekkLogging.log("getXmlFromUrl: invalid URL", e);
        } catch (IOException e) {
            WebtrekkLogging.log("getXmlFromUrl: invalid URL", e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        // return XML
        return xml;
    }

    // Given a string representation of a URL, sets up a connection and gets
// an input stream.
    InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    /**
     * return sha256 coded string
     *
     * @param value
     * @return
     */
    public static String makeSha256(String value) {
        return getHash(value, "SHA-256");
    }

    /**
     * return md5 code based on string
     *
     * @param value
     * @return
     */
    public static String makeMd5(String value) {
        if (value == null)
            return null;

        return getHash(value, "MD5");
    }

    private static String getHash(String value, String hasType) {
        if (value == null)
            return null;

        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance(hasType);
            digest = md.digest(value.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            WebtrekkLogging.log(hasType + " isn't found");
            return null;
        } catch (UnsupportedEncodingException e) {
            WebtrekkLogging.log("encoding error for UTF-8");
            return null;
        }

        String retStr = new BigInteger(1, digest).toString(16);

        //add leading zero value if value less then 16
        if (digest.length > 0 && digest[0] >= 0 && digest[0] < 16) {
            retStr = "0" + retStr;
        }

        return retStr;
    }

    public static boolean testIsValidURL(String url)
    {
        try {
            URL urlObject = new URL(url);
        }catch(MalformedURLException e)
        {
            return false;
        }
        return true;
    }

    public static void setDeepLinkMediaCode(Context context, String value)
    {
        SharedPreferences.Editor editor = HelperFunctions.getWebTrekkSharedPreference(context).edit();

        editor.putString(MEDIA_CODE_DEEP_LINK, value).apply();
    }

    public static String getDeepLinkMediaCode(Context context, boolean deleteFlag)
    {
        SharedPreferences preferences = HelperFunctions.getWebTrekkSharedPreference(context);

        String result = preferences.getString(MEDIA_CODE_DEEP_LINK, null);

        if (deleteFlag)
            preferences.edit().remove(MEDIA_CODE_DEEP_LINK).apply();

        return result;
    }
}
