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
 * Created by Thomas Dahlmann on 21.05.15.
 */

package com.webtrekk.SDKTest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;

import com.webtrekk.webtrekksdk.Webtrekk;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MediaActivity extends Activity {
    private static final int MEDIA_LENGTH = 360;

    SeekBar playProgressBar;
    TextView currentMediaPositionField;
    private Webtrekk webtrekk;
    TrackingParameter tp;
    private String currentState = ""; // this variable stores the current state (play/pause/stop)
    private ScheduledExecutorService timerService;

    int getCurrentPlayProgress() {
        return (int) (MEDIA_LENGTH * (this.playProgressBar.getProgress() / 100.0));
    }

    public void initMediaTracking() {
        // Tracker initialisieren

        int progress = MediaActivity.this.getCurrentPlayProgress();

        if (MediaActivity.this.tp == null) {
            MediaActivity.this.tp = new TrackingParameter();
            MediaActivity.this.tp.add(Parameter.MEDIA_FILE, "android-demo-media"); // Name des Videos
            MediaActivity.this.tp.add(Parameter.MEDIA_LENGTH, String.valueOf(MEDIA_LENGTH)); // Dauer des Videos in Millisekunden
            MediaActivity.this.tp.add(Parameter.MEDIA_POS, String.valueOf(progress)); // Startposition des Videos in Millisekunden
            MediaActivity.this.tp.add(Parameter.MEDIA_CAT, "1", "demo-category-1"); // optional - Mediakategorien
            MediaActivity.this.tp.add(Parameter.MEDIA_CAT, "2", "demo-category-2");
            MediaActivity.this.tp.add(Parameter.MEDIA_CAT, "2", "demo-category-2");
            MediaActivity.this.tp.add(Parameter.MEDIA_ACTION, "init");
            MediaActivity.this.webtrekk.track(tp);


        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webtrekk = Webtrekk.getInstance();
        setContentView(R.layout.activity_media2);

        this.playProgressBar = (SeekBar) this.findViewById(R.id.media_current_psoition_progressbar);
        this.currentMediaPositionField = (TextView) this.findViewById(R.id.media_current_position_field);

        final Button playButton = (Button) this.findViewById(R.id.button_media_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMediaTracking();
                int progress = MediaActivity.this.getCurrentPlayProgress();
                currentState = "play";
                MediaActivity.this.tp.add(Parameter.MEDIA_ACTION, "play");
                MediaActivity.this.tp.add(Parameter.MEDIA_POS, String.valueOf(progress));
                MediaActivity.this.webtrekk.track(tp);
                startTimerService();

            }
        });



        final Button pauseButton = (Button) this.findViewById(R.id.button_media_pause);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = MediaActivity.this.getCurrentPlayProgress();
                currentState = "pause";
                MediaActivity.this.tp.add(Parameter.MEDIA_ACTION, "pause");
                MediaActivity.this.tp.add(Parameter.MEDIA_POS, String.valueOf(progress));
                MediaActivity.this.webtrekk.track(tp);
                timerService.shutdown();
            }
        });

        final Button stopButton = (Button) this.findViewById(R.id.button_media_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = MediaActivity.this.getCurrentPlayProgress();
                currentState = "stop";
                tp.add(Parameter.MEDIA_ACTION, "stop");
                tp.add(Parameter.MEDIA_POS, String.valueOf(progress));
                webtrekk.track(tp);
                // reset tracking params to null after stop
                //tp = null;
                timerService.shutdown();
            }
        });

        this.playProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = MediaActivity.this.getCurrentPlayProgress();
                // replace the current tracked action of seekend with the state before seek began
                // so if it was play before set it to play, otherwise set it to pause
                Log.d("MyApplication", "action: "+ MediaActivity.this.tp.getDefaultParameter().get(Parameter.MEDIA_ACTION));
                if(!currentState.equals("play")) {
                    MediaActivity.this.tp.add(Parameter.MEDIA_ACTION, "pause");
                } else {
                    MediaActivity.this.tp.add(Parameter.MEDIA_ACTION, "play");
                    startTimerService();
                }
                MediaActivity.this.tp.add(Parameter.MEDIA_POS, String.valueOf(progress));
                MediaActivity.this.webtrekk.track(tp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                int progress = MediaActivity.this.getCurrentPlayProgress();
                MediaActivity.this.tp.add(Parameter.MEDIA_ACTION, "seek");
                MediaActivity.this.tp.add(Parameter.MEDIA_POS, String.valueOf(progress));
                MediaActivity.this.webtrekk.track(tp);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //not interesting for us
            }
        });
    }

    public void startTimerService(){
        // start the timer service
        timerService = Executors.newSingleThreadScheduledExecutor();
        timerService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                onPlayIntervalOver();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * this method gets called every 30 seconds, when in play state, it sends the current position
     * TODO: discuss if this should really be in the SDK, if so, the mediaactions need an if/else block
     *
     */
    void onPlayIntervalOver() {
        if(currentState.equals("play")) {
            int progress = MediaActivity.this.getCurrentPlayProgress();
            MediaActivity.this.tp.add(Parameter.MEDIA_ACTION, "pos");
            MediaActivity.this.tp.add(Parameter.MEDIA_POS, String.valueOf(progress));
            MediaActivity.this.webtrekk.track(tp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_media, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart () {
        super.onStart();
        initMediaTracking();

    }

    @Override
    protected void onResume() {
        super.onResume();
        webtrekk.track();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
