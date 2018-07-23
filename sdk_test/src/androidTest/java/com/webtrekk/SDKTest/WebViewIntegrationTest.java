package com.webtrekk.SDKTest;

import android.webkit.WebView;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


/**
 * Created by vartbaronov on 28.07.17.
 */

public class WebViewIntegrationTest extends WebtrekkBaseMainTest {

    volatile boolean mFinishTaskNotifierDone;
    final Object mWaiter = new Object();

    @Rule
    public final WebtrekkTestRule<MainActivity> mActivityRule =
            new WebtrekkTestRule<>(MainActivity.class, this);

    @Override
    public void before() throws Exception {
        super.before();
    }

    @Override
    @After
    public void after() throws Exception {
        //add sleep to wait until all messages are sent.
        super.after();
    }

    @Test
    public void webViewIntegrationTest(){
        // define intefrace to catch web tracking
        final MainActivity activity = mActivityRule.getActivity();
        final WebView webView = (WebView)activity.findViewById(R.id.main_web_view);
        final String everID = mWebtrekk.getEverId();

        // define intefrace to catch web tracking
        activity.setLoadResourceCallback(new MainActivity.LoadWebViewResource(){
                @Override
                public void load(String url) {
                    //check if everID equal
                    if (url.startsWith("https://q3.webtrekk.net/")) {

                        URLParsel parcel = new URLParsel();

                        parcel.parseURL(url);

                        final String webEverID = parcel.getValue("eid");

                        assertEquals(everID, webEverID);

                        synchronized (mWaiter) {
                            mFinishTaskNotifierDone = true;
                            mWaiter.notifyAll();
                        }
                    }
                }
        });

        //do webtracking
        onView(withId(R.id.show_webview)).perform(click());

        //wait for test done
        try {
            synchronized (mWaiter) {
                while (!mFinishTaskNotifierDone)
                    mWaiter.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
