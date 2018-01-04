package com.webtrekk.webtrekksdk.Request;

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
 * Created by arsen vartbaronov on 12.12.17.
 */

import android.content.Context;
import android.support.annotation.NonNull;

import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

class TrackingRequestTemporaryStore {
    final static private String FILE_NAME = "wt-pending-requests.json";
    final File mStoreFile;
    final TrackingConfiguration mConfiguration;


    TrackingRequestTemporaryStore(@NonNull Context context,
                                  @NonNull TrackingConfiguration trackingConfiguration){
        mStoreFile = new File(context.getFilesDir(), FILE_NAME);
        mConfiguration = trackingConfiguration;
    }

    void saveTrackingRequest(@NonNull TrackingRequest request){
        PrintWriter writer = null;
        try {
            JSONObject jsonObject = request.saveToJson();
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(mStoreFile, true), "UTF-8")));
            writer.println(jsonObject.toString());
        } catch (JSONException e) {
            WebtrekkLogging.log("can't save pending tracking request:" + e.getLocalizedMessage());
        } catch (FileNotFoundException e) {
            WebtrekkLogging.log("can't save pending tracking request:" + e.getLocalizedMessage());
        } catch (UnsupportedEncodingException e) {
            WebtrekkLogging.log("can't save pending tracking request:" + e.getLocalizedMessage());
        } finally {
            if (writer != null){
                writer.close();
            }
        }
    }

    @NonNull
    List<TrackingRequest> getAllSavedRequests(){
        BufferedReader reader = null;
        List<TrackingRequest> list = new ArrayList<>();

        try {
            reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(mStoreFile),"UTF-8"));
            String line;
            while ((line = reader.readLine()) != null){
                JSONObject jsonObject = new JSONObject(line);
                list.add(TrackingRequest.createFromJson(jsonObject, mConfiguration));
            }
        } catch (UnsupportedEncodingException e) {
            WebtrekkLogging.log("can't read pending tracking request:" + e.getLocalizedMessage());
        } catch (FileNotFoundException e) {
            WebtrekkLogging.log("can't read pending tracking request:" + e.getLocalizedMessage());
        } catch (IOException e) {
            WebtrekkLogging.log("can't read pending tracking request:" + e.getLocalizedMessage());
        } catch (JSONException e) {
            WebtrekkLogging.log("can't read pending tracking request:" + e.getLocalizedMessage());
        }
        return list;
    }

    void deleteQueue(){
        mStoreFile.delete();
    }

    boolean queueIsEmpty(){
        return !mStoreFile.exists();
    }
}
