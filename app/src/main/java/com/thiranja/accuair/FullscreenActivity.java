package com.thiranja.accuair;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;


public class FullscreenActivity extends AppCompatActivity {

    private static final boolean AUTO_HIDE = true;

    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private WebView mWebView;

    // flag to keep track failiure to load
    private Boolean anyLoadError = false;
    // Array list to handel navigation
    ArrayList<String> urlList;

    private View mControlsView;
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mWebView = findViewById(R.id.webview);

        // Setting a empty url list at the begging

        urlList = new ArrayList<>();
        urlList.add("http://accuair.cf/");

        // Setting the webview to load accuair website

        mWebView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {

                    // adding url to a array list to unable back navigation through it
                    // if found a link previouly visited remove links up to size from that index
                    // and adding previously unvisited links
                    if (urlList.contains(url)){
                        int index = urlList.indexOf(url);
                        int length = urlList.size();
                        for (int i = (index +1); i < length; i++){
                            urlList.remove(i);
                        }
                    }else {
                        urlList.add(url);
                    }

                    mWebView.loadUrl(url);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                mWebView.setVisibility(View.GONE);
                mContentView.setVisibility(View.VISIBLE);
                anyLoadError = true;
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT > 15) {
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        }
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.loadUrl("http://accuair.cf/");

        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (anyLoadError){
            mWebView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
            mWebView.loadUrl(urlList.get(urlList.size()-1));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hide();

    }

    @Override
    public void onBackPressed() {
        if (urlList.size() == 0){
            // call previous activity in this case close the app
            super.onBackPressed();
        }else{
            urlList.remove(urlList.size()-1);
            String url = urlList.get(urlList.size() - 1);
            mWebView.loadUrl(url);
        }

    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

}
