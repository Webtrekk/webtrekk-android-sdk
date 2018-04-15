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
 * Created by Thomas Dahlmann on 12.11.15.
 */

package com.webtrekk.webtrekksdk.Configuration;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

public class ActivityConfiguration {
    private String mClassName;
    private String mMappingName;
    private boolean mIsAutoTrack;
    private TrackingParameter mActivityTrackingParameter;
    private TrackingParameter mConstActivityTrackingParameter;

    public ActivityConfiguration() {
    }

    public ActivityConfiguration(String className, String mappingName, boolean isAutoTrack, TrackingParameter tp, TrackingParameter constTp) {
        mClassName = className;
        mMappingName = mappingName;
        mIsAutoTrack = isAutoTrack;
        mActivityTrackingParameter = tp;
        mConstActivityTrackingParameter = constTp;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public String getMappingName() {
        return mMappingName;
    }

    public void setMappingName(String mappingName) {
        mMappingName = mappingName;
    }

    public boolean isAutoTrack() {
        return mIsAutoTrack;
    }

    public void setIsAutoTrack(boolean isAutoTrack) {
        mIsAutoTrack = isAutoTrack;
    }

    public TrackingParameter getActivityTrackingParameter() {
        return mActivityTrackingParameter;
    }

    public void setActivityTrackingParameter(TrackingParameter activityTrackingParameter) {
        this.mActivityTrackingParameter = activityTrackingParameter;
    }

    public TrackingParameter getConstActivityTrackingParameter() {
        return mConstActivityTrackingParameter;
    }

    public void setConstActivityTrackingParameter(TrackingParameter constActivityTrackingParameter) {
        mConstActivityTrackingParameter = constActivityTrackingParameter;
        //validae URL if any
        if (mConstActivityTrackingParameter.containsKey(TrackingParameter.Parameter.PAGE_URL))
        {
            String url = mConstActivityTrackingParameter.getDefaultParameter().get(TrackingParameter.Parameter.PAGE_URL);
            if (!HelperFunctions.testIsValidURL(url))
            {
                WebtrekkLogging.log("Incorrece URL:" + url + " in configuration. Don't track it for pu parameter");
                mConstActivityTrackingParameter.getDefaultParameter().remove(TrackingParameter.Parameter.PAGE_URL);
            }
        }
    }

    public void setOverridenPageURL(String url)
    {
        mConstActivityTrackingParameter.getDefaultParameter().put(TrackingParameter.Parameter.PAGE_URL, url);
    }

    public void resetOverridenPageURL()
    {
        if (mConstActivityTrackingParameter!= null && mConstActivityTrackingParameter.getDefaultParameter() != null)
          mConstActivityTrackingParameter.getDefaultParameter().remove(TrackingParameter.Parameter.PAGE_URL);
    }


}
