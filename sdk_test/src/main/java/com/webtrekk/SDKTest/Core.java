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
 * Created by Widgetlabs, www.widgetlabs.eu around 2014
 */

package com.webtrekk.SDKTest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


class Core {
	
	public static final String VERSION = "2.0";
	
	private static final String TAG = Core.class.getName();
	
	private static final String PARAMETER_ACTION_ID     = "ct";
	private static final String PARAMETER_EVER_ID       = "eid";
	private static final String PARAMETER_PIXEL         = "p";    // default data. format: <version>,<contentId>,<javascript available>,<screen resolution>,<screen color depth>,<cookies available>,<timestamp>,<referer>,<browser size>,<java available>
	private static final String PARAMETER_SAMPLING_RATE = "ps";
	private static final String PARAMETER_SESSION_ID    = "sid";
	private static final String PARAMETER_USER_AGENT    = "X-WT-UA";
	private static final String PIXEL_VERSION           = "210";
	
	private static final String INSTALL_REFERRER_PARAMS_MC  = "mc";
	private static final String INSTALL_REFERRER_PARAMS_MCV = "mcv";
	
	private static final String PREFERENCE_KEY_IS_SAMPLING   = "sampling";
	private static final String PREFERENCE_KEY_SAMPLING_RATE = "samplingRate";
	private static final String PREFERENCE_KEY_OPTED_OUT     = "optedOut";
	private static final String PREFERENCE_KEY_EVER_ID       = "everId";
	private static final String PREFERENCES_FILE_NAME        = "webtrekk-preferences";
	
	private static final long INITIAL_SEND_DELAY = 5 * 1000;       // 5 seconds
	private static final long DEFAULT_SEND_DELAY = 5 * 60 * 1000;  // 5 minutes
	
	
	private Context                 context;
	private WeakReference<Activity> currentActivity;
	private String                  everId;
	private boolean                 isFirstSession;
	private boolean                 isSampling;
	private boolean                 loggingEnabled;
	private boolean                 optedOut;
	private int                     samplingRate;
	private String                  serverUrl;
	private String                  sessionId;
	private boolean                 started;
	private String                  trackId;
	private String                  userAgent;
	
	
	
	public Core() {

		this.setupUserAgent();
	}
	
	
	public void activityStart(Activity activity) {
		if (activity == null) {
			this.log("activityStart: 'activity' must not be null.");
			return;
		}
		
		if (!this.started) {
			this.setContext(activity);
		}
		
		if (this.serverUrl == null) {
			this.log("activityStart: 'serverUrl' was not set.");
			return;
		}
		if (this.trackId == null) {
			this.log("activityStart: 'trackId' was not set.");
			return;
		}
		
		if (this.currentActivity != null) {
			Activity currentActivity = this.currentActivity.get();
			if (currentActivity == activity) {
				return;
			}
		}
		
		this.currentActivity = new WeakReference<Activity>(activity);
		
		if (!this.started) {
			this.setupSampling();
			this.setupSessionId();
			
			this.started = true;
			this.log("activityStart: Started tracking.");
		}
	}
	
	
	public void activityStop(Activity activity) {
		if (activity == null) {
			this.log("activityStop: 'activity' must not be null.");
			return;
		}
		
		if (this.currentActivity == null || this.currentActivity.get() != activity) {
			return;
		}
		
		this.currentActivity = null;
		this.isFirstSession = false;
		this.isSampling = false;
		this.sessionId = null;
		
		this.started = false;
		
		this.log("activityStop: Stopped tracking.");
	}
	
	
	public Context getContext() {
		return this.context;
	}
	
	
	public String getEverId() {
		String everId = this.everId;
		if (everId == null) {
			if (this.context == null) {
				this.log("getEverId: 'context' was not set.");
				return null;
			}
			
			SharedPreferences preferences = this.context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
			everId = preferences.getString(PREFERENCE_KEY_EVER_ID, null);
			if (everId == null) {
				Random random = new Random();
				everId = String.format("%06d%06d%07d", Integer.valueOf(random.nextInt(1000000)), Integer.valueOf(random.nextInt(1000000)), Integer.valueOf(random.nextInt(10000000)));
				
				preferences.edit().putString(PREFERENCE_KEY_EVER_ID, everId).commit();
			}
			
			this.everId = everId;
		}
		
		return everId;
	}
	
	
	public int getSamplingRate() {
		return this.samplingRate;
	}
	
	
	public String getServerUrl() {
		return this.serverUrl;
	}
	
	
	public String getTrackId() {
		return this.trackId;
	}
	
	
	public String getUserAgent() {
		return this.userAgent;
	}
	
	
	public boolean isLoggingEnabled() {
		return this.loggingEnabled;
	}
	
	
	public boolean isOptedOut() {
		return this.optedOut;
	}
	
	
	public void log(String message) {
		this.log(message, null);
	}
	
	
	public void log(String message, Throwable throwable) {
		if (this.loggingEnabled) {
			Log.w(TAG, message, throwable);
		}
	}
	
	
	public void setContext(Context context) {
		if (this.started) {
			this.log("setContext: Cannot set context after tracking was started.");
			return;
		}
		
		if (context != null) {
			context = context.getApplicationContext();
		}
		
		this.context = context;
		
		if (context != null) {
			this.setupOptedOut();
		}
	}
	
	
	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}
	
	
	public void setOptedOut(boolean optedOut) {
		if (this.context == null) {
			this.log("Cannot set opted-out until a context was set.");
			return;
		}
		
		if (optedOut == this.optedOut) {
			return;
		}
		
		this.optedOut = optedOut;
		
		SharedPreferences preferences = this.context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		preferences.edit().putBoolean(PREFERENCE_KEY_OPTED_OUT, optedOut).commit();
	}
	
	
	public void setSamplingRate(int samplingRate) {
		if (samplingRate < 0) {
			this.log("setSamplingRate: 'samplingRate' must not be negative.");
			return;
		}
		if (this.started) {
			this.log("setSamplingRate: Cannot set sampling rate after tracking was started.");
			return;
		}
		
		this.samplingRate = samplingRate;
	}
	
	
	public void setSendDelay(long sendDelay) {
		if (sendDelay < 1000) {
			this.log("setSendDelay: 'sendDelay' must be at least one second.");
			return;
		}
		if (this.started) {
			this.log("setSendDelay: Cannot set send delay after tracking was started.");
			return;
		}
	}
	
	
	public void setServerUrl(String serverUrl) {
		if (this.started) {
			this.log("setServerUrl: Cannot set server URL after tracking was started.");
			return;
		}
		
		this.serverUrl = serverUrl;
	}
	
	
	public void setTrackId(String trackId) {
		if (this.started) {
			this.log("setTrackId: Cannot set track ID after tracking was started.");
			return;
		}
		
		this.trackId = trackId;
	}
	
	
	private void setupOptedOut() {
		SharedPreferences preferences = this.context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		this.optedOut = preferences.getBoolean(PREFERENCE_KEY_OPTED_OUT, false);
		
		this.log("optedOut = " + this.optedOut);
	}
	
	
	private void setupSampling() {
		SharedPreferences preferences = this.context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
		if (preferences.getInt(PREFERENCE_KEY_SAMPLING_RATE, 0) == this.samplingRate) {
			this.isSampling = preferences.getBoolean(PREFERENCE_KEY_IS_SAMPLING, true);
		}
		else {
			this.isSampling = (this.samplingRate <= 0 || new Random().nextInt(this.samplingRate) == 0);
			
			Editor editor = preferences.edit();
			editor.putBoolean(PREFERENCE_KEY_IS_SAMPLING, this.isSampling);
			editor.putInt(PREFERENCE_KEY_SAMPLING_RATE, this.samplingRate);
			editor.commit();
		}
		
		this.log("isSampling = " + this.isSampling + ", samplingRate = " + this.samplingRate);
	}
	
	
	private void setupSessionId() {
		String everId = this.getEverId();
		if (this.isFirstSession) {
			this.sessionId = everId;
		}
		else {
			Random random = new Random();
			this.sessionId = String.format("%06d%06d%07d", Integer.valueOf(random.nextInt(1000000)), Integer.valueOf(random.nextInt(1000000)), Integer.valueOf(random.nextInt(10000000)));
		}
		
		this.log("sessionId = " + this.sessionId);
	}
	
	
	private void setupUserAgent() {
		this.userAgent = "Tracking Library " + VERSION + "(" + System.getProperty("os.name").toString() + "; " + "Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + "; " + Locale.getDefault().toString() + ")";
	}
	
	
	public void trackAction(String pageId, String actionId) {
		this.trackAction(pageId, actionId, null);
	}
	
	
	public void trackAction(String pageId, String actionId, Map<String,String> data) {
		if (pageId == null) {
			this.log("trackAction: 'pageId' must not be null.");
			return;
		}
		if (actionId == null) {
			this.log("trackAction: 'actionId' must not be null.");
			return;
		}
		
		data = data != null ? data : new HashMap<String,String>();
		data.put(PARAMETER_ACTION_ID, actionId);
		
		this.trackPage(pageId, data);
	}
	
	
	public void trackEvent(Map<String,String> data) {
		if (!this.started) {
			this.log("trackEvent: Cannot track event as tracking is not started. Did you forget to call activityStart()?");
			return;
		}
		
		if (this.optedOut || !this.isSampling) {
			return;
		}
		
		Map<String,String> parameters = data != null ? new HashMap<String,String>(data) : new HashMap<String,String>();
		parameters.put(PARAMETER_EVER_ID,       this.everId);
		parameters.put(PARAMETER_SAMPLING_RATE, Integer.toString(this.samplingRate));
		parameters.put(PARAMETER_SESSION_ID,    this.sessionId);
		parameters.put(PARAMETER_USER_AGENT,    this.userAgent);
		
		boolean appendedParameters = false;
		
		StringBuilder url = new StringBuilder();
		url.append(this.serverUrl);
		if (!this.serverUrl.endsWith("/")) {
			url.append('/');
		}
		url.append(this.trackId);
		url.append("/wt.pl");
		
		String pixel = parameters.get(PARAMETER_PIXEL);
		if (pixel != null) {
			// pixel parameter must come first if present
			// pixel parameter must not be URL-encoded - its parts are already encoded
			
			parameters.remove(PARAMETER_PIXEL);
			
			url.append('?');
			url.append(urlEncode(PARAMETER_PIXEL));
			url.append('=');
			url.append(pixel);
			
			appendedParameters = true;
		}
		
		for (String parameterName : parameters.keySet()) {
			String parameterValue = parameters.get(parameterName);
			if (parameterValue == null) {
				continue;
			}
			
			if (appendedParameters) {
				url.append('&');
			}
			else {
				url.append('?');
				appendedParameters = true;
			}
			
			url.append(urlEncode(parameterName));
			url.append('=');
			url.append(urlEncode(parameterValue));
		}
	}
	
	
	public void trackPage(String pageId) {
		this.trackPage(pageId, null);
	}
	
	
	public void trackPage(String pageId, Map<String,String> data) {
		if (pageId == null) {
			this.log("trackPage: 'pageId' must not be null.");
			return;
		}
		
		String pixel = PIXEL_VERSION  + "," + urlEncode(pageId) + ",0,0,0,0," + (System.currentTimeMillis() / 1000);
		
		data = data != null ? data : new HashMap<String,String>();
		data.put(PARAMETER_PIXEL, pixel);
		this.trackEvent(data);
	}
	
	
	private static String urlEncode(String string) {
		if (string == null || string.length() == 0) {
			return string;
		}
		
		try {
			return URLEncoder.encode(string, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private static String urlDecode(String string) {
		if (string == null || string.length() == 0) {
			return string;
		}
		
		try {
			return URLDecoder.decode(string, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
