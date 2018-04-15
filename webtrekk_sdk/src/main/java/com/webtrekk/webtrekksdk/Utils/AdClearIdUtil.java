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
 * Created by Niko Pruessner on 24.01.17.
 */

package com.webtrekk.webtrekksdk.Utils;
import java.security.SecureRandom;

/**
 * This util class is responsible for the generation of the adClearId.
 * This ID is a long (64 bits) with the following structure:
 *
 *  1 bit  (bit 0):      reserved
 * 39 bits (bits 1-39):  milliseconds since 2011-01-01 (39 bits should be enough for the next 17 years)
 * 10 bits (bits 40-49): a random value to add further uniqueness (max. value 1023)
 * 10 bits (bits 50-59): application ID (max. value: 1023)
 *  4 bits (bits 60-63): process ID, (max. value: 15)
 *
 */
public final class AdClearIdUtil {

    private static final int BITS_OF_MILLISECONDS         = 39;
    private static final int BITS_OF_RAND                 = 10;
    private static final int BITS_OF_APPLICATION          = 10;
    private static final int BITS_OF_PROCESS              = 4;
    private static final int BIT_SHIFT_FOR_APPLICATION    = BITS_OF_PROCESS;
    private static final int BIT_SHIFT_FOR_RAND           = BIT_SHIFT_FOR_APPLICATION + BITS_OF_APPLICATION;
    private static final int BIT_SHIFT_FOR_TIMESTAMP      = BIT_SHIFT_FOR_RAND + BITS_OF_RAND;
    private static final long MILLISECONDS_UNTIL_01012011 = 1293840000000L;
    private static final int APPLICATION_ID               = 713;
    public  static final String PREFERENCE_KEY_ADCLEAR_ID = "adClearId";


    /**
     * Generates a unique adClearId, a unique tracking identifier for the adClear tracking
     *
     * @return adClearId
     */
    public long generateAdClearId() {

        long nowUTC = System.currentTimeMillis();
        long diffInMilliseconds = nowUTC - MILLISECONDS_UNTIL_01012011;

        SecureRandom secureRandom = new SecureRandom();
        int rand = secureRandom.nextInt();

        int processId = android.os.Process.myPid();

        return combineAdClearId(diffInMilliseconds, rand, AdClearIdUtil.APPLICATION_ID, processId);
    }


    /**
     * Assembles the request ID based on the passed parameters
     *
     * @param diffInMilliseconds The difference in milliseconds since 2011-01-01 00:00:00.000
     * @param rand a random number to add further uniqueness to the adClear id (e.g. using dev/random)
     * @param applicationId Application specific integer value
     * @param processId process specific integer value (e.g. OS process ID)
     * @return Request ID
     */
    public final long combineAdClearId(long diffInMilliseconds, long rand, long applicationId, long processId) {

        diffInMilliseconds = sanitize(diffInMilliseconds, BITS_OF_MILLISECONDS);
        rand               = sanitize(rand,               BITS_OF_RAND);
        applicationId      = sanitize(applicationId,      BITS_OF_APPLICATION);
        processId          = sanitize(processId,          BITS_OF_PROCESS);

        return ((diffInMilliseconds << BIT_SHIFT_FOR_TIMESTAMP) + (rand << BIT_SHIFT_FOR_RAND)
                + (applicationId << BIT_SHIFT_FOR_APPLICATION) + processId);
    }


    /**
     * Guarantees l to be a positive number and limits its bits to maxBits
     *
     * @param l a long value
     * @param maxBits the max number of bits allowed
     * @return the sanitized long value
     */
    public long sanitize(long l, int maxBits) {
        if (l<0) {
            l = -l;
        }
        return limitToBits(l, maxBits);
    }


    /**
     * Limits a given value v to using a maximum of maxBits.
     *
     * @param v
     * @param maxBits
     * @return v%maxBits
     */
    public long limitToBits(long v, int maxBits) {
        long maxlong = (1L << maxBits) - 1L;
        return v & maxlong;
    }
}