package com.coinomi.wallet.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.coinomi.wallet.R;

public class BurseWebviewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressDialog progDailog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burse_webview);
        webView = findViewById(R.id.burseWebview);
        setupProgressDialog();
        getSupportActionBar().hide();
        setupWebView();
    }

    private void setupProgressDialog(){
        progDailog = new ProgressDialog(this);
        progDailog.setMessage(getString(R.string.activity_burse_please_wait));
        progDailog.setTitle(getString(R.string.activity_burse_please_wait));
        progDailog.setCancelable(false);
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Intent intent = new Intent(MainActivity.this, MainActivity.class);
                //intent.putExtra("url", url);
                //startActivity(intent);
                progDailog.show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    webView.animate().alpha(0).setDuration(333).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }).start();
                }
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
                webView.animate().alpha(1).setDuration(333).setInterpolator(new AccelerateInterpolator()).start();
                progDailog.dismiss();
            }
        });

        progDailog.show();
        webView.loadUrl("https://coinagewallet.com/login");
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}
