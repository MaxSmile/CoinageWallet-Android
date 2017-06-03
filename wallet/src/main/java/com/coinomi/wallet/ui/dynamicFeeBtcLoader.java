package com.coinomi.wallet.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v4.content.CursorLoader;

import com.coinomi.wallet.Configuration;
import com.coinomi.wallet.DynamicFeeProvider;
import com.coinomi.wallet.ExchangeRatesProvider;

/**
 * Created by vitaligrabovski on 6/3/17.
 */

public class DynamicFeeBtcLoader extends CursorLoader implements SharedPreferences.OnSharedPreferenceChangeListener  {

    private final Configuration config;
    private final String packageName;
    private final Context context;
    private String localCurrency;

    public DynamicFeeBtcLoader(final Context context, final Configuration config,
                               final String localSymbol) {
        super(context, DynamicFeeProvider.contentUriBtcFee(context.getPackageName(), localSymbol, false),
                null, null, new String[]{null}, null);

        this.config = config;
        this.packageName = context.getPackageName();
        this.context = context;
        this.localCurrency = localSymbol;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        //refreshUri(config.getExchangeCurrencyCode());

        config.registerOnSharedPreferenceChangeListener(this);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        context.registerReceiver(broadcastReceiver, intentFilter);

        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        config.unregisterOnSharedPreferenceChangeListener(this);
        try {
            context.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) { /* ignore */ }
        super.onStopLoading();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        /*if (Configuration.PREFS_KEY_EXCHANGE_CURRENCY.equals(key)) {
            onCurrencyChange();
        }*/
    }

    private void onCurrencyChange() {
        //refreshUri(config.getExchangeCurrencyCode());
        forceLoad();
    }

    private void refreshUri(String newLocalCurrency) {
        if (!newLocalCurrency.equals(localCurrency)) {
            localCurrency = newLocalCurrency;
            setUri(ExchangeRatesProvider.contentUriToCrypto(packageName, localCurrency, false));
        }
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        private boolean hasConnectivity = true;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                hasConnectivity = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                if (hasConnectivity) {
                    forceLoad();
                }
            } else if (Intent.ACTION_TIME_TICK.equals(action) && hasConnectivity) {
                forceLoad();
            }
        }
    };
}
