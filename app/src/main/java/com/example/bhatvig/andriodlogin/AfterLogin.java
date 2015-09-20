package com.example.bhatvig.andriodlogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AfterLogin extends Activity {

    WebView myWebView;

    private final static String TAG = "WebView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        myWebView = (WebView) findViewById(R.id.webview);

        new WebViewTask().execute();
    }

    private class HelloWebViewClient extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            myWebView.loadUrl(url);

            return true;
        }
        public void onPageFinished(WebView view, String url) {
            CookieSyncManager.getInstance().sync();
        }
    }

    private class WebViewTask extends AsyncTask<Void, Void, Boolean> {
        String sessionCookie;
        CookieManager cookieManager;

        @Override
        protected void onPreExecute() {
            CookieManager.setAcceptFileSchemeCookies(true);
            CookieSyncManager.createInstance(AfterLogin.this);
            cookieManager = CookieManager.getInstance();
            CookieSyncManager.getInstance().startSync();
            sessionCookie = new PersistentConfig(getApplicationContext()).getCookieString();
            if (sessionCookie != null) {
                                /* delete old cookies */
                cookieManager.removeSessionCookie();
            }
            super.onPreExecute();
        }
        protected Boolean doInBackground(Void... param) {
                        /* this is very important - THIS IS THE HACK */
            SystemClock.sleep(1000);
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (sessionCookie != null) {
                cookieManager.setCookie("http://9mints.com", sessionCookie);
                CookieSyncManager.getInstance().sync();
            }
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            myWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return super.shouldOverrideUrlLoading(view, url);
                }

            });
            myWebView.loadUrl("http://9mints.com");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK && myWebView.canGoBack()){
            myWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

     class PersistentConfig {

        private static final String PREFS_NAME = "prefs_file";

        private SharedPreferences settings;

        public PersistentConfig(Context context) {

            settings = context.getSharedPreferences(PREFS_NAME, 0);

        }

        public String getCookieString() {

            return settings.getString("my_cookie", "");

        }

        public void setCookie(String cookie) {

            SharedPreferences.Editor editor = settings.edit();

            editor.putString("my_cookie", cookie);

            editor.commit();

        }

    }
}