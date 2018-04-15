package com.webtrekk.webtrekksdk.Modules;

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
 * Created by Arsen Vartbaronov on 20.10.17.
 */

/**
 * This class process appinstall goal on first application start
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;

public class AppinstallGoal {
    private static final String appinstallGoal = "appinstallGoal";
    private static final String appinstallGoalProcessed = "appinstallGoalProcessed";

    //init appinstall goal if it isn't processed yet
    public void initAppinstallGoal(@NonNull Context context){
        if (!isAppinstallGoalProcessed(context)){
            SharedPreferences.Editor editor = getPreferences(context).edit();
            editor.putBoolean(AppinstallGoal.appinstallGoal, true).apply();
        }
    }

    //return if there is appinstall goal to process
    public boolean isAppinstallGoal(@NonNull Context context){
        return getPreferences(context).getBoolean(AppinstallGoal.appinstallGoal, false);
    }

    //finish applinstall goal
    public void finishAppinstallGoal(@NonNull Context context){
        if (isAppinstallGoal(context)){
            SharedPreferences.Editor editor = getPreferences(context).edit();
            editor.remove(AppinstallGoal.appinstallGoal);
            editor.putBoolean(AppinstallGoal.appinstallGoalProcessed, true);
            editor.apply();
        }
    }

    //check if applinstall goal already processed
    private boolean isAppinstallGoalProcessed(@NonNull Context context){
        return getPreferences(context).getBoolean(AppinstallGoal.appinstallGoalProcessed, false);
    }

    private SharedPreferences getPreferences(@NonNull Context context){
        return HelperFunctions.getWebTrekkSharedPreference(context);
    }
}
