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
 * Created by Thomas Dahlmann on 24.04.15.
 */

package com.webtrekk.SDKTest;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Webtrekk;

public class TrackedVideoView extends VideoView {
    private Webtrekk webtrekk;
    TrackingParameter tp;

    public TrackedVideoView(Context context) {
        super(context);
        initMediaFile();
    }

    public TrackedVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMediaFile();
    }

    public TrackedVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMediaFile();
    }
    public void initMediaFile() {
        tp = new TrackingParameter();
        tp.add(TrackingParameter.Parameter.MEDIA_FILE, getResources().getResourceEntryName(R.raw.wt));
        tp.add(TrackingParameter.Parameter.MEDIA_LENGTH, String.valueOf(getDuration()));
        tp.add(TrackingParameter.Parameter.MEDIA_POS, String.valueOf(getCurrentPosition()/1000));
        tp.add(TrackingParameter.Parameter.MEDIA_CAT, "1", "mp4");
        tp.add(TrackingParameter.Parameter.MEDIA_CAT, "1", "example");
    }

    @Override
    public void pause() {
        super.pause();
        tp.add(TrackingParameter.Parameter.MEDIA_ACTION, "pause");
        tp.add(TrackingParameter.Parameter.MEDIA_POS, String.valueOf(getCurrentPosition()/1000));
        webtrekk.track(tp);
    }

    @Override
    public void start() {
        super.start();
        tp.add(TrackingParameter.Parameter.MEDIA_ACTION, "start");
        tp.add(TrackingParameter.Parameter.MEDIA_POS, String.valueOf(getCurrentPosition()/1000));
        webtrekk.track(tp);
    }

    @Override
    public void seekTo(int msec) {
        super.seekTo(msec);
        tp.add(TrackingParameter.Parameter.MEDIA_ACTION, "seek");
        int sec = msec/1000;
        tp.add(TrackingParameter.Parameter.MEDIA_POS, String.valueOf(sec));
        webtrekk.track(tp);
    }

    @Override
    public void stopPlayback() {
        super.stopPlayback();
        tp.add(TrackingParameter.Parameter.MEDIA_POS, String.valueOf(getCurrentPosition()/1000));
        tp.add(TrackingParameter.Parameter.MEDIA_ACTION, "stop");
        webtrekk.track(tp);
    }

    public Webtrekk getWebtrekk() {
        return webtrekk;
    }

    public void setWebtrekk(Webtrekk webtrekk) {
        this.webtrekk = webtrekk;
    }
}
