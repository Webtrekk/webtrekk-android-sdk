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
 * Created by Arsen Vartbaronov on 28.04.16.
 */

package com.Webtrekk.SDKTest;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLParsel
{
    final private Map<String, String> mMap = new HashMap<>();
    public static String URLKEY = "MAIN_URL_KEY";

    public boolean parseURL(String url)
    {
        Pattern pattern = Pattern.compile("([^?&]+)");
        Matcher matcher = pattern.matcher(url);

        matcher.find();
        mMap.put(URLKEY, matcher.group());
        while (matcher.find())
        {
            final String parValue[] = matcher.group().split("=");

            if (parValue.length == 2)
               mMap.put(parValue[0], parValue[1]);
            else
            {
                WebtrekkLogging.log("key:"+parValue[0]+" don't have value");
                return false;
            }
        }

        return true;
    }

    public String getValue(String key)
    {
        return  mMap.get(key);
    }

    public String getDecodedValue(String key)
    {
        return HelperFunctions.urlDecode(getValue(key));
    }
}
