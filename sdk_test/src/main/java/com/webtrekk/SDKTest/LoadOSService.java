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
 * Created by Arsen Vartbaronov on 22.04.16.
 */

package com.webtrekk.SDKTest;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class LoadOSService extends Service {

    public static String MODE = "MODE";
    public enum Mode {LOAD_MEMORY, LOAD_CPU};
    public static Set<ArrayHolder> mMemory= new HashSet<>();
    private final long oneMeg = 1024*1024;


    private static class ArrayHolder{
        final byte[] mArray;
        //private final Bitmap mBitmap;

        public ArrayHolder(int size)
        {
            mArray = new byte[size];
            //mBitmap = Bitmap.createBitmap(size/2, size/2, Bitmap.Config.ALPHA_8);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    static public int getAvailableMemory(Context context)
    {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        Log.d(LoadOSService.class.getName(),"current available memory KB is:"+mi.availMem/1024);

        return (int)mi.availMem;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            Mode mode = Mode.values()[intent.getExtras().getInt(MODE)];

            switch (mode) {
                case LOAD_MEMORY:
                    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                    ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    activityManager.getMemoryInfo(mi);
                    Runtime info = Runtime.getRuntime();

                    Log.d(getClass().getName(), "current free memory runtime is:" + info.freeMemory() / oneMeg);
                    Log.d(getClass().getName(), "current total memory runtime is:" + info.totalMemory() / oneMeg);
                    Log.d(getClass().getName(), "current available memory is:" + mi.availMem / oneMeg);
                    Log.d(getClass().getName(), "current is lowMemory value:" + mi.lowMemory);
                    Log.d(getClass().getName(), "current threshold value:" + mi.threshold / oneMeg);

                    //leave only one meg
                    boolean exceptionThrown = false;
                    long memoryAllocated = 0;
                    final int allocateSize = 1024 * 1024;

                    while (!exceptionThrown && !mi.lowMemory) {
                        try {
                            mMemory.add(new ArrayHolder(allocateSize));
                            activityManager.getMemoryInfo(mi);
                        } catch (OutOfMemoryError e) {
                            exceptionThrown = true;
                        }
                        if (!exceptionThrown)
                            memoryAllocated += allocateSize;
                    }

                    Log.d(getClass().getName(), "Allocated memory before crash:" + memoryAllocated / oneMeg);
                    Log.d(getClass().getName(), "Available memory before crash is:" + mi.availMem / oneMeg);

                    //mMemory = new byte[getAvailableMemory(this) - 4*1024*1024];
                    Log.d(getClass().getName(), "current is lowMemory value:" + mi.lowMemory);
                    break;
                case LOAD_CPU: {
                    class RegexThread extends Thread {
                        RegexThread() {
                            // Create a new, second thread
                            super("Regex Thread");
                            start(); // Start the thread
                        }

                        // This is the entry point for the second thread.
                        public void run() {
                            while (true) {
                                //Pattern p = Pattern.compile("a*b");
                            }
                        }
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(getClass().getName(), "Load processor is started");
                            int NUM_THREADS = 20, RUNNING_TIME = 120; // run 10 threads for 120s
                            for (int i = 0; i < NUM_THREADS; ++i) {
                                new RegexThread(); // create a new thread
                            }
                            try {
                                Thread.sleep(1000 * RUNNING_TIME);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
