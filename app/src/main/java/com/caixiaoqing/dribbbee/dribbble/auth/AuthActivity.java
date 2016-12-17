package com.caixiaoqing.dribbbee.dribbble.auth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.dribbble.Dribbble;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by caixiaoqing on 14/12/16.
 */

public class AuthActivity extends AppCompatActivity {

    public static final String KEY_URL = "url";
    public static final String KEY_CODE = "code";

    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.webview) WebView webView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    //private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);

        //webView = (WebView)findViewById(R.id.webview);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.auth_activity_title));



        //progressBar.setMax(100);
        //CookieSyncManager.createInstance(this);
        //CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.removeAllCookies(callback);
        //cookieManager.setAcceptCookie(false);

        //WebSettings ws = webView.getSettings();
        //ws.setSaveFormData(false);
        //ws.setSavePassword(false);

        webView.clearCache(true);
        webView.clearHistory();


        //CookieManager cookieManager = CookieManager.getInstance();
        //cookieManager.setAcceptCookie(false);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(Auth.REDIRECT_URI)) {
                    Log.i("xqc", "onCreate Auth.URI_REDIRECT");
                    Uri uri = Uri.parse(url);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(KEY_CODE, uri.getQueryParameter(KEY_CODE));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                Log.i("xqc", "onCreate NOT Auth.URI_REDIRECT"+url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            /*
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }*/
        });

        /*
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });*/

        String url = getIntent().getStringExtra(KEY_URL);

        Log.i("xqc", "webView.loadUrl" + url);
        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
