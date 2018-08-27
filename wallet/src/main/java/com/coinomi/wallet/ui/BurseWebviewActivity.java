package com.coinomi.wallet.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.coinomi.core.coins.BitcoinMain;
import com.coinomi.core.coins.CoinID;
import com.coinomi.core.wallet.WalletAccount;
import com.coinomi.wallet.R;

import java.util.List;

import static com.coinomi.wallet.Constants.COINAGE_BURSE_URL;

public class BurseWebviewActivity extends BaseWalletActivity {

    private WebView webView;
    private ProgressDialog progDailog;
    private String token = "";
    private CookieManager cookieManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_burse_webview);
        webView = findViewById(R.id.burseWebview);
        setupCookieManager();
        setupProgressDialog();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.activity_burse_title);
        //getSupportActionBar().hide();
        setupWebView();



    }

    private void setupCookieManager() {
        cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                // a callback which is executed when the cookies have been removed
                @Override
                public void onReceiveValue(Boolean aBoolean) {
                    Log.d("cookie_removed", "Cookie removed: " + aBoolean);
                }
            });
        }
else cookieManager.removeAllCookie();
    }

    private void setupProgressDialog(){
        progDailog = new ProgressDialog(this);
        progDailog.setMessage(getString(R.string.activity_burse_please_wait));
        progDailog.setTitle(getString(R.string.activity_burse_please_wait));
        progDailog.setCancelable(false);
    }

    @SuppressLint("JavascriptInterface")
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
                showLoading();

                view.loadUrl(url);
                return true;
            }
            @SuppressLint("NewApi")
            @Override
            public void onPageFinished(WebView view, final String url) {
                hideLoading();
                if (url.contains(COINAGE_BURSE_URL + "settings")){//logged in --> get token and load trading page
                    view.evaluateJavascript("document.getElementsByName('authenticity_token')[0].value", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            token = s;
                            showLoading();
                            view.loadUrl(COINAGE_BURSE_URL + "trading/enrgeur");
                            invalidateOptionsMenu();
                        }
                    });
                    //view.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                }
                if (url.contains(COINAGE_BURSE_URL + "trading/enrgeur")){// if trading tab loaded, open  load trade tab
                    //showLoading();
                    /*view.evaluateJavascript("document.querySelectorAll('[data-for=m_t_trade]')[0].click()", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            hideLoading();
                        }
                    });*/
                }

                if (url.contains(COINAGE_BURSE_URL + "funds#/withdraws/")){// set withdraw address of mobile wallet
                    List<WalletAccount> all = getWalletApplication().getAllAccounts();
                    List<WalletAccount> b = getWalletApplication().getAccounts(CoinID.typeFromSymbol("BTC"));
                    if (url.endsWith("/eth")){
                        view.evaluateJavascript("document.getElementById(\"withdraw_rid\").value = \"" + getWalletApplication().getWallet().getAccounts(CoinID.typeFromSymbol("BTC")) + "\"", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String s) {
                                hideLoading();
                            }
                        });
                    }

                }
            }
        });
        showLoading();
        webView.loadUrl(COINAGE_BURSE_URL + "login");
    }

    private void showLoading(){
        webView.animate().alpha(0).setDuration(1).setInterpolator(new DecelerateInterpolator()).start();
        progDailog.show();
    }

    private void hideLoading(){
        webView.animate().alpha(1).setDuration(333).setInterpolator(new AccelerateInterpolator()).start();
        progDailog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_burse, menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem menuItem = menu.findItem(R.id.action_wallets);
        if (token.isEmpty()){
            menuItem.setVisible(false);
        } else {
            menuItem.setVisible(true);
        }

        return true;
    }

    private void openWallets(){
        showLoading();
        webView.loadUrl(COINAGE_BURSE_URL + "funds#");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_wallets:
                openWallets();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
