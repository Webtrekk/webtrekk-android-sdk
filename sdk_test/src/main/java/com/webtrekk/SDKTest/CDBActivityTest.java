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
 * Created by Arsen Vartbaronov on 29.02.16.
 */

package com.webtrekk.SDKTest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.webtrekk.webtrekksdk.Webtrekk;
import com.webtrekk.webtrekksdk.WebtrekkUserParameters;

public class CDBActivityTest extends Activity {
    private TextView mTextConsole;
    volatile private String mSendedURL;
    volatile private int mTestCycleID;
    private boolean mIsWaitForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cdb_test);

        findViewById(R.id.cdb_send_request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Webtrekk webtrekk = Webtrekk.getInstance();

                webtrekk.track(new WebtrekkUserParameters().
                        setAddress(getTextFromID(R.id.cdb_address)).
                        setEmail(getTextFromID(R.id.cdb_email)).
                        setPhone(getTextFromID(R.id.cdb_phone)).
                        setAndroidId(getTextFromID(R.id.cdb_android_id)).
                        setiOSId(getTextFromID(R.id.cdb_ios_id)).
                        setWindowsId(getTextFromID(R.id.cdb_windows_id)).
                        setFacebookID(getTextFromID(R.id.cdb_facebook_id)).
                        setTwitterID(getTextFromID(R.id.cdb_twitter_id)).
                        setGooglePlusID(getTextFromID(R.id.cdb_google_plus_id)).
                        setLinkedInID(getTextFromID(R.id.cdb_linkin_id)).
                        setCustom(Integer.valueOf(getTextFromID(R.id.cdb_custom_ind1)), getTextFromID(R.id.cdb_custom_value1)).
                        setCustom(Integer.valueOf(getTextFromID(R.id.cdb_custom_ind2)), getTextFromID(R.id.cdb_custom_value2)));
            }
        });
        findViewById(R.id.cdb_clear_fields).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int ids[] = {R.id.cdb_address, R.id.cdb_email, R.id.cdb_phone, R.id.cdb_android_id, R.id.cdb_ios_id,
                        R.id.cdb_windows_id, R.id.cdb_facebook_id, R.id.cdb_twitter_id, R.id.cdb_google_plus_id,
                        R.id.cdb_linkin_id, R.id.cdb_custom_ind1, R.id.cdb_custom_ind2, R.id.cdb_custom_value1,
                        R.id.cdb_custom_value2};

                for (int id: ids)
                {
                    setTextByID(id, "");
                }
            }
        });

        findViewById(R.id.cdb_unit_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTestCycleID = 0;
                doCDBTest();
            }
        });

        mTextConsole = (TextView)findViewById(R.id.cdb_unit_test_console);
        URLReceiverRegister();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        URLReceiverUnRegister();
    }

    private String getTextFromID(int id) {
        String value = ((TextView) findViewById(id)).getText().toString();

        return value.equals("null") ? null : value;
    }

    private void setTextByID(int id, String text)
    {
        ((TextView) findViewById(id)).setText(text);
    }


    private BroadcastReceiver mURLReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mIsWaitForResult) {
                mSendedURL = intent.getStringExtra("URL");
                processResult();
                if (++mTestCycleID < mCycleTestArr.length) {
                    doCDBTest();
                } else
                    addTextToConsole("End of test\n");
            }
        }
    };

    /**
     * This is just for testing. To receive Campain installation data
     */
    private void URLReceiverRegister()
    {
        LocalBroadcastManager.getInstance(this).registerReceiver(mURLReceiver,
                new IntentFilter("com.webtrekk.webtrekksdk.TEST_URL"));
    }

    /**
     * This is just for testing. To receive Campain installation data
     */
    private void URLReceiverUnRegister()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mURLReceiver);
    }

    private void addTextToConsole(String text)
    {
        mTextConsole.append(text);
        Log.d(getLocalClassName(), text);
    }

    String[] mParametersName = {
            //0, 1, 2, 3, 4, 5
            "email1", "email2", "email3", "email4", "emailmd", "emailsha",
            //6, 7, 8, 9, 10, 11, 12
            "phone1", "phone2", "phone3", "phone4","phone5", "phonemd", "phonesha",
            //13, 14, 15, 16
            "address1", "address2", "address3", "address4",
            //17, 18, 19, 20
            "address5", "address6", "address7", "address8",
            //21, 22
            "addressmd", "addresssha",
            //23, 24, 25, 26
            "androidID", "iosID", "WinID", "facebookID",
            //27, 28, 29, 30
            "TwitterID","GooglePludID", "LinkedID", "Custom"
    };

    String[] mParametersValue = {
            "test@tester.com", "TEST@TESTER.COM",  "Test@Tester.com", " Test@Tester.com ", "EF8CA1C0FF7D2E34DC0953D4222655B8", "1F9E575AD4234C30A81D30C70AFFD4BBA7B0D57D8E8607AD255496863D72C8BB",
            "01799586148", "+49179 9586148", "+49 179/9586148", "00 179/9586148", "0179 95 86 148", "6AF3CC537AB15FFB500167AF24D2B9D6", "629D99E8350B704511F8FE6506C38888C0749DACC0F091D7F8914CDD6B5B7862",
            "stephan|guenther|10115|teststrasse|7", "Stephan|Guenther|10115|Teststrasse|7", "Stephan|Günther|10115|Teststraße|7", "Stephan|Günther|10115|Teststr|7",
            "Stephan|Günther|10115|Teststr.|7", "Stephan|Günther|10115|Teststr. |7", " Stephan | Günther | 10 115 | Teststr. | 7 ", " Ste-phan | Günt_her | 10 1 15 | Test - str. | 7 ",
            "756E1FD66E46D930707ACD1B2D2DCC14", "6E65555EA5D3C707EF3E7BBC6A7E09D33C23DD2E23C36D6750B25BF86EFDF843",
            "ABC123DEF456", "ABC123DEF456", "ABC123DEF456", "100001603870661",
            "333887969", "103942815740852792445", "1R2RtA", "CustomField"
    };

    //sha256 or original value
    String[] mFirstOrSha256KeyName={
            "cdb2", "cdb2", "cdb2", "cdb2", null, "cdb2",
            "cdb4", "cdb4", "cdb4", "cdb4", "cdb4", null, "cdb4",
            "cdb6", "cdb6", "cdb6", "cdb6",
            "cdb6", "cdb6", "cdb6", "cdb6",
/*
                "cdb6", "cdb6","cdb6","cdb6",
                "cdb6", "cdb6","cdb6","cdb6",
*/
            null, "cdb6",
            "cdb7", "cdb8", "cdb9", "cdb10",
            "cdb11", "cdb12", "cdb13", "cdb51"
    };

    String[] mMdKeyName={
            "cdb1", "cdb1", "cdb1", "cdb1", "cdb1", null,
            "cdb3", "cdb3", "cdb3", "cdb3", "cdb3", "cdb3", null,
            "cdb5", "cdb5", "cdb5", "cdb5",
            "cdb5", "cdb5", "cdb5", "cdb5",
/*
                "cdb5", "cd5","cdb5","cdb5",
                "cdb5", "cd5","cdb5","cdb5",
*/
            "cdb5", null,
            null, null, null, null,
            null, null, null, null
    };

    String[] mFirstOrSha256Value = {
            "1f9e575ad4234c30a81d30c70affd4bba7b0d57d8e8607ad255496863d72c8bb", "1f9e575ad4234c30a81d30c70affd4bba7b0d57d8e8607ad255496863d72c8bb","1f9e575ad4234c30a81d30c70affd4bba7b0d57d8e8607ad255496863d72c8bb","1f9e575ad4234c30a81d30c70affd4bba7b0d57d8e8607ad255496863d72c8bb", null, "1f9e575ad4234c30a81d30c70affd4bba7b0d57d8e8607ad255496863d72c8bb",
            "629d99e8350b704511f8fe6506c38888c0749dacc0f091d7f8914cdd6b5b7862", "27e75156a4134c75a019efcb7f899d62fb23d300667a79289fd4a11c4bcdbf87", "27e75156a4134c75a019efcb7f899d62fb23d300667a79289fd4a11c4bcdbf87", "6497ae00a154a09fc6b39c9e4c4ba6f64885e8279d587b66626fec44e8cc468c", "629d99e8350b704511f8fe6506c38888c0749dacc0f091d7f8914cdd6b5b7862",null, "629d99e8350b704511f8fe6506c38888c0749dacc0f091d7f8914cdd6b5b7862",
            "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843", "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843", "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843", "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843",
            "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843", "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843", "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843", "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843",
            null, "6e65555ea5d3c707ef3e7bbc6a7e09d33c23dd2e23c36d6750b25bf86efdf843",
            "abc123def456", "abc123def456", "abc123def456", "574852115fa603e477907c4284f5a45d92f3194a759f33b2d66f72309cc7ba07",
            "8182771b8680ca5bd979b339f3e3c1416342c3ea62133819c76c71aebaa38efb", "af3e9f1b964c6a377ba4ad61a37a84f5ba527ffd7b014515885217919c900ba6", "44a6998e43e432440de3b0045c278664b62fa9e77b32b12937561c67d385a732", "customfield"
    };

    String[] mMdFieldValue={
            "ef8ca1c0ff7d2e34dc0953d4222655b8", "ef8ca1c0ff7d2e34dc0953d4222655b8", "ef8ca1c0ff7d2e34dc0953d4222655b8", "ef8ca1c0ff7d2e34dc0953d4222655b8","ef8ca1c0ff7d2e34dc0953d4222655b8", null,
            "6af3cc537ab15ffb500167af24d2b9d6", "15a7498681d67ecc0b9c62c0087a9faa", "15a7498681d67ecc0b9c62c0087a9faa", "03f5113c45423448356b1c1c5a3e0027", "6af3cc537ab15ffb500167af24d2b9d6","6af3cc537ab15ffb500167af24d2b9d6", null,
            "756e1fd66e46d930707acd1b2d2dcc14", "756e1fd66e46d930707acd1b2d2dcc14", "756e1fd66e46d930707acd1b2d2dcc14", "756e1fd66e46d930707acd1b2d2dcc14",
            "756e1fd66e46d930707acd1b2d2dcc14", "756e1fd66e46d930707acd1b2d2dcc14", "756e1fd66e46d930707acd1b2d2dcc14", "756e1fd66e46d930707acd1b2d2dcc14",
            "756e1fd66e46d930707acd1b2d2dcc14", null,
            null, null, null, null,
            null, null, null, null
    };


    int[][] mCycleTestArr = {{0,6,13,23,24,25,26,27,28,29, 30},
            {1, 7, 14}, {2, 8, 15},  {3, 9, 16}, {4, 5, 10, 17},
            {11, 12, 18}, {19}, {20}, {21, 22}};


    private void doCDBTest()
    {

        Webtrekk webtrekk = Webtrekk.getInstance();

        if (mTestCycleID == 0)
           addTextToConsole("Start CDB test.........................\n");

        addTextToConsole("Start test cycle "+mTestCycleID+"..............................\n");

        mSendedURL = null;

        switch (mTestCycleID)
        {
            case 0:
                webtrekk.track(new WebtrekkUserParameters().
                        setEmail(mParametersValue[mCycleTestArr[mTestCycleID][0]]).
                        setPhone(mParametersValue[mCycleTestArr[mTestCycleID][1]]).
                        setAddress(mParametersValue[mCycleTestArr[mTestCycleID][2]]).
                        setAndroidId(mParametersValue[mCycleTestArr[mTestCycleID][3]]).
                        setiOSId(mParametersValue[mCycleTestArr[mTestCycleID][4]]).
                        setWindowsId(mParametersValue[mCycleTestArr[mTestCycleID][5]]).
                        setFacebookID(mParametersValue[mCycleTestArr[mTestCycleID][6]]).
                        setTwitterID(mParametersValue[mCycleTestArr[mTestCycleID][7]]).
                        setGooglePlusID(mParametersValue[mCycleTestArr[mTestCycleID][8]]).
                        setLinkedInID(mParametersValue[mCycleTestArr[mTestCycleID][9]]).
                        setCustom(1, mParametersValue[mCycleTestArr[mTestCycleID][10]]));
                break;
            case 1:
            case 2:
            case 3:
                webtrekk.track(new WebtrekkUserParameters().
                        setEmail(mParametersValue[mCycleTestArr[mTestCycleID][0]]).
                        setPhone(mParametersValue[mCycleTestArr[mTestCycleID][1]]).
                        setAddress(mParametersValue[mCycleTestArr[mTestCycleID][2]]));
                break;
            case 4:
                webtrekk.track(new WebtrekkUserParameters().
                        setEmailMD5(mParametersValue[mCycleTestArr[mTestCycleID][0]]).
                        setEmailSHA256(mParametersValue[mCycleTestArr[mTestCycleID][1]]).
                        setPhone(mParametersValue[mCycleTestArr[mTestCycleID][2]]).
                        setAddress(mParametersValue[mCycleTestArr[mTestCycleID][3]]));
                break;
            case 5:
                webtrekk.track(new WebtrekkUserParameters().
                        setPhoneMD5(mParametersValue[mCycleTestArr[mTestCycleID][0]]).
                        setPhoneSHA256(mParametersValue[mCycleTestArr[mTestCycleID][1]]).
                        setAddress(mParametersValue[mCycleTestArr[mTestCycleID][2]]));
                break;
            case 6:
            case 7:
                webtrekk.track(new WebtrekkUserParameters().
                        setAddress(mParametersValue[mCycleTestArr[mTestCycleID][0]]));
                break;
            case 8:
                webtrekk.track(new WebtrekkUserParameters().
                        setAddressMD5(mParametersValue[mCycleTestArr[mTestCycleID][0]]).
                        setAddressSHA256(mParametersValue[mCycleTestArr[mTestCycleID][1]]));
                break;
        }

        addTextToConsole("Wait for result....\n");
        mIsWaitForResult = true;

    }

    private void processResult()
    {
        URLParsel parcel = new URLParsel();

        parcel.parseURL(mSendedURL);

        for (int value: mCycleTestArr[mTestCycleID])
        {
            String parName = mParametersName[value];
            String parValue = mParametersValue[value];
            String firstKey = mFirstOrSha256KeyName[value];
            String firstValue = mFirstOrSha256Value[value];
            String mdKey = mMdKeyName[value];
            String mdValue = mMdFieldValue[value];

            if (mParametersValue == null)
                continue;

            addTextToConsole("test " + parName + " value:" + parValue+"\n");


            if (firstKey != null) {
                if (parcel.getValue(firstKey).equals(firstValue)) {
                    addTextToConsole(parName + " first value test OK\n");
                } else
                    addTextToConsole(parName + " first value FAILED!!!!!!!!!!!!!!!!. " + parName + " is " + parValue + " ER:" + firstValue + ", AR:" + parcel.getValue(firstKey) + "\n");
            }

            if (mdKey != null) {

                if (parcel.getValue(mdKey).equals(mdValue)) {
                    addTextToConsole(parName + " md value test OK\n");
                } else
                    addTextToConsole(parName + " md value FAILED!!!!!!!!!!!!!!!!. " + parName + " is " + parValue + " ER:" + mdValue + ", AR:" + parcel.getValue(mdKey) + "\n");
            }
        }

        mIsWaitForResult = false;
    }

}
