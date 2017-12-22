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
 * Created by Niko Pruessner.
 */

package com.Webtrekk.SDKTest;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.filters.LargeTest;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Utils.AdClearIdUtil;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WebtrekkClassRunner.class)
@LargeTest
public class AdClearIdTest extends WebtrekkBaseMainTest {

    private static final long MILLISECONDS_UNTIL_01012011 = 1293840000000L;

    @Rule
    public final WebtrekkTestRule<EmptyActivity> mActivityRule =
            new WebtrekkTestRule<>(EmptyActivity.class, this);

    @Override
    public void before() throws Exception {
        super.before();
        initWebtrekk(R.raw.webtrekk_config_no_auto_track);
        clearAdClearId();
    }


    @After
    @Override
    public void after() throws Exception {
        super.after();
    }

    @Test
    public void testAdclearIdInRequestURL() {

        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                mWebtrekk.track();
            }
        });
        String URL = waitForTrackedURL();

        URLParsel parcel = new URLParsel();
        parcel.parseURL(URL);

        assertTrue(parcel.getValue("cs808") != null && !parcel.getValue("cs808").isEmpty());

        // check for event tracking
        initWaitingForTrack(new Runnable() {
            @Override
            public void run() {
                TrackingParameter pt = new TrackingParameter();
                pt.add(TrackingParameter.Parameter.ACTION_NAME, "test");
                mWebtrekk.track(pt);
            }
        });
        URL = waitForTrackedURL();

        parcel = new URLParsel();
        parcel.parseURL(URL);

        String adClearID = parcel.getValue("cs808");
        assertTrue(adClearID != null && !adClearID.isEmpty());
        assertionsGenerateAdClearId_millisecondsPart(Long.parseLong(adClearID));
        assertionsGenerateAdClearId_ApplicationId_part(Long.parseLong(adClearID));

        try {
            long adClearIDNum = Long.valueOf(adClearID);
            assertTrue(adClearIDNum > 0);
        }catch (NumberFormatException e){
            assertTrue("ad ClearID format isn't correct", false);
        }
    }


    /**
     * using the application ID 713 and setting everything else to 0
     */
    @Test
    public void testCombineAdClearId1() {

        // using the application ID 713 (binary: 1011001001) and setting everything else to 0:
        long adClearId = new AdClearIdUtil().combineAdClearId(0L, 0, 713, 0);

        // this should result in the following long (reason, see below):
        assertEquals(11408L, adClearId);

        // whose bit representation is: 0...0 | 0...0 | 1011001001 | 0000
        // the last 4 bits (0000):
        assertEquals(0, getBit(adClearId, 0));
        assertEquals(0, getBit(adClearId, 1));
        assertEquals(0, getBit(adClearId, 2));
        assertEquals(0, getBit(adClearId, 3));
        // the following 10 bits (1011001001):
        assertEquals(1, getBit(adClearId, 4));
        assertEquals(0, getBit(adClearId, 5));
        assertEquals(0, getBit(adClearId, 6));
        assertEquals(1, getBit(adClearId, 7));
        assertEquals(0, getBit(adClearId, 8));
        assertEquals(0, getBit(adClearId, 9));
        assertEquals(1, getBit(adClearId, 10));
        assertEquals(1, getBit(adClearId, 11));
        assertEquals(0, getBit(adClearId, 12));
        assertEquals(1, getBit(adClearId, 13));

        for (int i = 14; i<64; i++) {
            assertEquals(0, getBit(adClearId, i));
        }
    }


    /**
     * Using application ID 713 and setting process ID to 15
     */
    @Test
    public void testCombineAdClearId2() {

        // using the application ID 713 (binary: 1011001001) and setting process ID to 15 (binary: 1111):
        long adClearId = new AdClearIdUtil().combineAdClearId(0L, 0, 713, 15);

        // this should result in the following long (reason, see below):
        assertEquals(11423L, adClearId);

        // whose bit representation is: 0...0 | 0...0 | 1011001001 | 1111
        // the last 4 bits (1111):
        assertEquals(1, getBit(adClearId, 0));
        assertEquals(1, getBit(adClearId, 1));
        assertEquals(1, getBit(adClearId, 2));
        assertEquals(1, getBit(adClearId, 3));
        // the following 10 bits (1011001001):
        assertEquals(1, getBit(adClearId, 4));
        assertEquals(0, getBit(adClearId, 5));
        assertEquals(0, getBit(adClearId, 6));
        assertEquals(1, getBit(adClearId, 7));
        assertEquals(0, getBit(adClearId, 8));
        assertEquals(0, getBit(adClearId, 9));
        assertEquals(1, getBit(adClearId, 10));
        assertEquals(1, getBit(adClearId, 11));
        assertEquals(0, getBit(adClearId, 12));
        assertEquals(1, getBit(adClearId, 13));

        for (int i = 14; i<64; i++) {
            assertEquals(0, getBit(adClearId, i));
        }
    }



    /**
     * Using application ID 713 and setting process ID to 16. 16 is a number which is too big for
     * the available number of bits for this position. 16 decimal is 10000 in binary. Since only 4
     * bits are available 16 should be altered to 16%16 = 0 (binary 0000)
     */
    @Test
    public void testCombineAdClearId3() {

        // using the application ID 713 (binary: 1011001001) and setting process ID to 16 (binary: 10000):
        long adClearId = new AdClearIdUtil().combineAdClearId(0L, 0, 713, 16);

        // this should result in the following long (reason, see below):
        assertEquals(11408L, adClearId);

        // whose bit representation is: 0...0 | 0...0 | 1011001001 | 0000
        // the last 4 bits (0000):
        assertEquals(0, getBit(adClearId, 0));
        assertEquals(0, getBit(adClearId, 1));
        assertEquals(0, getBit(adClearId, 2));
        assertEquals(0, getBit(adClearId, 3));
        // the following 10 bits (1011001001):
       assertEquals(1, getBit(adClearId, 4));
        assertEquals(0, getBit(adClearId, 5));
        assertEquals(0, getBit(adClearId, 6));
        assertEquals(1, getBit(adClearId, 7));
        assertEquals(0, getBit(adClearId, 8));
        assertEquals(0, getBit(adClearId, 9));
        assertEquals(1, getBit(adClearId, 10));
        assertEquals(1, getBit(adClearId, 11));
        assertEquals(0, getBit(adClearId, 12));
        assertEquals(1, getBit(adClearId, 13));

        for (int i = 14; i<64; i++) {
            assertEquals(0, getBit(adClearId, i));
        }
    }


    /**
     * Using application ID 1025 and setting process ID to 15. 1025 is a number which is too big for
     * the available number of bits for this position. 1025 decimal is 10000000001 in binary. Since
     * only 10 bits are available 1025 should be altered to 1025%1024 = 1 (binary 0000000001)
     */
    @Test
    public void testCombineAdClearId4() {

        // using the application ID 1025 (binary: 10000000001) and setting everything else to 0:
        long adClearId = new AdClearIdUtil().combineAdClearId(0L, 0, 1025, 0);

        // this should result in the following long (reason, see below):
        assertEquals(16L, adClearId);

        // whose bit representation is: 0...0 | 0...01100 | 0000000001 | 0000
        // the last 4 bits (0000):
        assertEquals(0, getBit(adClearId, 0));
        assertEquals(0, getBit(adClearId, 1));
        assertEquals(0, getBit(adClearId, 2));
        assertEquals(0, getBit(adClearId, 3));
        // the following bits (11000000111001):
        assertEquals(1, getBit(adClearId, 4));
        assertEquals(0, getBit(adClearId, 5));
        assertEquals(0, getBit(adClearId, 6));
        assertEquals(0, getBit(adClearId, 7));
        assertEquals(0, getBit(adClearId, 8));
        assertEquals(0, getBit(adClearId, 9));
        assertEquals(0, getBit(adClearId, 10));
        assertEquals(0, getBit(adClearId, 11));
        assertEquals(0, getBit(adClearId, 12));
        assertEquals(0, getBit(adClearId, 13));

        for (int i = 14; i<64; i++) {
            assertEquals(0, getBit(adClearId, i));
        }
    }



    @Test
    public void testLimitToBits() {
        AdClearIdUtil a = new AdClearIdUtil();
        assertEquals(6, a.limitToBits(6, 3));
        assertEquals(7, a.limitToBits(7, 3));
        assertEquals(0, a.limitToBits(8, 3));
        assertEquals(1, a.limitToBits(9, 3));
    }


    @Test
    public void testGenerateAdClearId_millisecondsPart() {
        long adClearId = new AdClearIdUtil().generateAdClearId();
        assertionsGenerateAdClearId_millisecondsPart(adClearId);
    }


    /**
     * making sure that a generated adclear id contains a plausible part which encodes the
     * time passed since 01.01.2011
     */
    private void assertionsGenerateAdClearId_millisecondsPart(long adClearId) {

        // Getting the 39 bits containing the elapsed milliseconds since 01.01.2011
        // Counting from the right side, these are the 39 bits starting at bit 24
        long millisecSince_01_01_2011 = getBitRange(adClearId, 24, 39);

        // 1st: Do a very rough plausibility check:
        // assert that the encoded time since 01.01.2011 is between 6 years and 20 years:
        long yearsSince_01_01_2011 = millisecSince_01_01_2011 / 1000 / 60 / 60 / 24 / 365;
        assertTrue(yearsSince_01_01_2011 >= 6);
        assertTrue(yearsSince_01_01_2011 <= 20);

        // 2nd: Do a more precise check:
        // assert that the encoded time does not differ by more than 5 minutes from the real value:
        long nowUTC = System.currentTimeMillis();
        long realMilliSecSince_01_01_2011 = nowUTC - MILLISECONDS_UNTIL_01012011;
        long difference = Math.abs(realMilliSecSince_01_01_2011 - millisecSince_01_01_2011);
        assertTrue(difference <= 5*60*1000);
    }



    @Test
    public void testGenerateAdClearId_ApplicationId_part() {
        long adClearId = new AdClearIdUtil().generateAdClearId();
        assertionsGenerateAdClearId_ApplicationId_part(adClearId);
    }



    /**
     * making sure that a generated adclear id contains the correctly encoded application id (713)
     */
    private void assertionsGenerateAdClearId_ApplicationId_part(long adClearId) {
        // Getting the 10 bits containing the application id
        // Counting from the right side, these are the 10 bits starting at bit 4
        long appId = getBitRange(adClearId, 4, 10);

        // assert that the application id is 713:
        assertTrue(appId == 713);
    }


    /**
     * Extracts a range of bits from a long, and converts it to a decimal number. E.g.:
     *
     * l = 114
     * start = 2
     * bits = 3
     *
     * The binary representation of 114 is 1110010.
     * The 3 bits starting at 2, are 100 (counting from the right side).
     * The decimal representation of 100 is 4, thus:
     *
     * => return 4;
     *
     * @param l a long whose bit range should be extracted
     * @param start the first bit to extract
     * @param noOfBits the last bit to extract
     * @return the bit range represented as a decimal
     */
    private long getBitRange(long l, int start, int noOfBits) {
        long result = 0;
        int bit = 0;
        for (int i = start; i<start+noOfBits; i++) {
            if (getBit(l, i) == 1) {
                result += (long) Math.pow(2, bit);
            }
            bit++;
        }
        return result;
    }


    /**
     * Returns one bit of a long (counting the position from the right(!) side, staring from 0)
     *
     * e.g. called with  l=8L (binary: 0..0000000001000) and position=3 would return 1
     *
     * @param l a long value
     * @param position position in the long whose bit should be returned
     * @return the bit at the given position (counting from the right side!)
     */
    private int getBit(long l, int position) {
        return (int) ((l >> position) & 1);
    }


    /**
     * Clears the adClearId stored locally on the testing device
     */
    private void clearAdClearId() {
        SharedPreferences preferences = getInstrumentation().getTargetContext().getSharedPreferences(Webtrekk.PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        preferences.edit().remove(AdClearIdUtil.PREFERENCE_KEY_ADCLEAR_ID).commit();
    }
}