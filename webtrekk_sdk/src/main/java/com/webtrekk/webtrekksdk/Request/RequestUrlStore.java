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
 * Created by Thomas Dahlmann on 17.09.15.
 */

package com.webtrekk.webtrekksdk.Request;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.LruCache;

import com.webtrekk.webtrekksdk.Utils.HelperFunctions;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * this class acts as a local storage for the url strings before they are sent
 * it gets instantiated only once by the main Webtrekk class
 */

public class RequestUrlStore {

    final static private String FILE_NAME = "wt-tracking-requests";
    final private File mRequestStoreFile;
    final private LruCache<Integer, String> mURLCache;
    // keys for current queue. Key can be point to not loaded URL
    final private SortedMap<Integer, Long> mIDs = Collections.synchronizedSortedMap(new TreeMap<Integer, Long>());
    final int mReadGroupSize = 200;
    final private Map<Integer, String> mLoadedIDs = new HashMap<>(mReadGroupSize);

    // Next string index
    private int mIndex;
    // current read index in file
    private volatile long mLatestSavedURLID = -1;
    private static String URL_STORE_CURRENT_SIZE = "URL_STORE_CURRENT_SIZE";
    private static String URL_STORE_SENT_URL_OFFSET = "URL_STORE_SENT_URL_OFFSET";
    final private Context mContext;

    /**
     * constructs a new RequestUrlStore object
     *
     * @param context the application/activity context to find the cache dir
     */
    public RequestUrlStore(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("no valid context");
        }

        mContext = context;
        mRequestStoreFile = new File(context.getFilesDir(), FILE_NAME);

        File fileInCash = new File(context.getCacheDir(), FILE_NAME);

        if (fileInCash.exists() && !mRequestStoreFile.exists()) {
            fileInCash.renameTo(mRequestStoreFile);
        }

        initFileAttributes();

        final int maxSize = 20;

        mURLCache = new LruCache<Integer, String>(maxSize) {
            @Override
            protected void entryRemoved(boolean evicted, Integer key, final String oldValue, String newValue) {
                if (evicted && oldValue != null) {
                    saveURLsToFile(new SaveURLAction() {
                        @Override
                        public void onSave(PrintWriter writer) {
                            writer.println(oldValue);
                        }
                    });

                    mLatestSavedURLID = key;
                }
            }
        };
    }

    private void initFileAttributes() {
        synchronized (mIDs) {
            SharedPreferences pref = HelperFunctions.getWebTrekkSharedPreference(mContext);
            int index = mIndex = pref.getInt(URL_STORE_CURRENT_SIZE, 0);
            long sentURLFileOffset = pref.getLong(URL_STORE_SENT_URL_OFFSET, -1);
            WebtrekkLogging.log("read store size: " + index);

            for (int i = 0; i < index; i++) {
                mIDs.put(i, -1l);
            }

            if (index > 0) {
                mIDs.put(0, sentURLFileOffset);
            }
        }
    }

    private void writeFileAttributes() {
        synchronized (mIDs) {
            WebtrekkLogging.log("save store size: " + mIDs.size());
            SharedPreferences.Editor prefEdit = HelperFunctions.getWebTrekkSharedPreference(mContext).edit();
            prefEdit.putLong(URL_STORE_SENT_URL_OFFSET, mIDs.isEmpty() ? -1 : mIDs.get(mIDs.firstKey()));
            prefEdit.putInt(URL_STORE_CURRENT_SIZE, mIDs.size()).apply();
        }
    }

    private interface SaveURLAction {
        void onSave(PrintWriter writer);
    }

    public void reset() {
        // reset only if class is removed
        synchronized (mIDs) {
            if (mIDs.isEmpty()) {
                initFileAttributes();
            }
        }
    }

    // Save URL to file
    private void saveURLsToFile(SaveURLAction action) {
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mRequestStoreFile, true), "UTF-8")));
            try {
                action.onSave(writer);
            } finally {
                writer.close();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            WebtrekkLogging.log("can not save url ", e);
        }
    }

    // flush to file all data, clear cache.
    public void flush() {
        if (hasSpareIds()) {
            saveURLsToFile(new SaveURLAction() {
                @Override
                public void onSave(PrintWriter writer) {
                    synchronized (mIDs) {
                        Set<Integer> mIDsKeySet = mIDs.keySet();
                        for (Integer id : mIDsKeySet) {
                            if (id <= mLatestSavedURLID) {
                                continue;
                            }

                            String url = mURLCache.get(id);
                            if (url != null) {
                                writer.println(url);
                                mLatestSavedURLID = id;
                            }
                        }
                    }
                }
            });
        }
        writeFileAttributes();
        // for debug only uncomment
        //dumpFile();
    }

    public void clearAllTrackingData() {
        clearIds();
        mLoadedIDs.clear();
        mIndex = 0;
        mLatestSavedURLID = -1;
        deleteRequestsFile();
        writeFileAttributes();
    }

    private void clearIds() {
        synchronized (mIDs) {
            Set<Integer> mIDsKeySet = mIDs.keySet();
            for (Integer id : mIDsKeySet) {
                mURLCache.remove(id);
            }

            mIDs.clear();
        }
    }


    public String peek() {
        Integer id = getFirstId();
        if (id == null) {
            return null;
        }

        String url = mURLCache.get(id);
        if (url == null) {
            url = mLoadedIDs.get(id);
            if (url == null) {
                // not url in cache, get it from file
                if (mLoadedIDs.size() > 0) {
                    WebtrekkLogging.log("Something wrong with logic. mLoadedIDs should be zero if url isn't found");
                }

                Long mId = getValueById(id);
                if (isURLFileExists() && mId != null) {
                    if (loadRequestsFromFile(mReadGroupSize, mId, id)) {
                        url = mLoadedIDs.get(id);
                    } else { // file is corrupted or missed
                        deleteAllCachedIDs();
                        return mURLCache.get(id);
                    }
                } else {
                    WebtrekkLogging.log("No url in cache, but file doesn't exist as well. Some issue here");
                }
            }
        }

        if (url == null) {
            WebtrekkLogging.log("Can't get URL something wrong. ID: " + id);
        }

        return url;
    }

    private boolean isURLFileExists() {
        return mRequestStoreFile.exists();
    }


    /**
     * adds a new url string to the store, drops old ones if the maximum request limit is exceeded
     *
     * @param requestUrl string representation of a tracking request
     */
    public void addURL(String requestUrl) {
        mURLCache.put(mIndex, requestUrl);
        addToMap(mIndex++, -1l);
    }

    public int size() {
        synchronized (mIDs) {
            return mIDs.size();
        }
    }

    public void removeLastURL() {
        synchronized (mIDs) {
            if (!mIDs.isEmpty()) {
                removeKey(mIDs.firstKey());
            }
        }
    }

    private Integer getFirstId() {
        synchronized (mIDs) {
            if (!mIDs.isEmpty()) {
                return mIDs.firstKey();
            }
        }

        return null;
    }

    private Long getValueById(int id) {
        synchronized (mIDs) {
            if (mIDs.containsKey(id)) {
                return mIDs.get(id);
            }
        }

        return null;
    }

    private void addToMap(int key, long value) {
        synchronized (mIDs) {
            mIDs.put(key, value);
        }
    }

    private void removeKey(int key) {
        synchronized (mIDs) {
            if (mIDs.containsKey(key)) {
                if (mLoadedIDs.remove(key) == null) {
                    mURLCache.remove(key);
                }

                mIDs.remove(key);
            }
        }
    }

    private boolean hasSpareIds() {
        synchronized (mIDs) {
            if (mIDs.isEmpty()) {
                return false;
            }

            WebtrekkLogging.log("Flush items to memory. Size: " + mIDs.size() + " latest saved URL ID: " + mLatestSavedURLID + " latest IDS: " + mIDs.lastKey());
            return mLatestSavedURLID < mIDs.lastKey();
        }
    }

    private void dumpFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(mRequestStoreFile), "UTF-8"));

            WebtrekkLogging.log("Dump flushed file start ------------------------------------------------");
            String line;
            while ((line = reader.readLine()) != null) {
                WebtrekkLogging.log(line);
            }
            WebtrekkLogging.log("Dump flushed file end --------------------------------------------------");
            WebtrekkLogging.log("IDS: " + Arrays.asList(mIDs).toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * loads the requests from the cache file if present
     */
    private boolean loadRequestsFromFile(int numbersToLoad, Long startOffset, int firstID) {
        int id = firstID;
        long offset = startOffset < 0 ? 0 : startOffset;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mRequestStoreFile), "UTF-8"));
            reader.skip(offset);
            try {
                String line;
                int ind = 0;
                // set offset for first id
                addToMap(id, offset);
                while ((line = reader.readLine()) != null && ind++ < numbersToLoad && mURLCache.get(id) == null) {
                    if (getValueById(id) == null) {
                        WebtrekkLogging.log("File is more than existed keys. Error. Key: " + id + " offset: " + offset);
                        return false;
                    }
                    // put URL and increment id
                    mLoadedIDs.put(id++, line);
                    offset += (line.length() + System.getProperty("line.separator").length());
                    // set offset of next id if exists
                    if (mLatestSavedURLID >= id || mLatestSavedURLID == -1) {
                        addToMap(id, offset);
                    }
                }
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            WebtrekkLogging.log("cannot load backup file '" + mRequestStoreFile.getAbsolutePath() + "'", e);
            return false;
        }

        return true;
    }

    private void deleteAllCachedIDs() {
        while (true) {
            Integer id = getFirstId();
            if (id == null) {
                return;
            }

            String url = mURLCache.get(id);
            if (url == null) {
                url = mLoadedIDs.get(id);
                if (url == null) {
                    removeKey(id);
                }
            } else {
                break;
            }
        }
    }

    /**
     * this method removes the old cache file, it should be called after the requests are loaded into the store
     */
    public void deleteRequestsFile() {
        WebtrekkLogging.log("deleting old backup file");
        if (!isURLFileExists()) {
            return;
        }

        if (size() != 0) {
            WebtrekkLogging.log("still items to send. Error delete URL request File");
            return;
        }

        boolean success = mRequestStoreFile.delete();
        if (success) {
            WebtrekkLogging.log("old backup file deleted");
        } else {
            WebtrekkLogging.log("error deleting old backup file");
        }

        writeFileAttributes();
    }

    /**
     * for unit testing only
     *
     * @return
     */
    public File getRequestStoreFile() {
        return mRequestStoreFile;
    }
}
