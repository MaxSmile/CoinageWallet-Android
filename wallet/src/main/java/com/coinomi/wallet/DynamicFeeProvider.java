package com.coinomi.wallet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.net.Uri;
import android.provider.BaseColumns;

import com.coinomi.core.coins.CoinType;
import com.coinomi.wallet.util.NetworkUtils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.net.MalformedURLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by vitaligrabovski on 6/2/17.
 */

public class DynamicFeeProvider extends ContentProvider {

    private ConnectivityManager connManager;
    private Configuration config;

    private String lastLocalCurrency = null;
    private long localFeeUpdated = 0;

    private String localBtcFee;

    //private static final String BASE_URL = "http://52.42.64.29/currfee.php?getfee_=1";
    private static final String BASE_URL = "http://tritiumcoin.com/currfee.php";

    private static final String QUERY_PARAM_OFFLINE = "offline";

    private static final Logger log = LoggerFactory.getLogger(DynamicFeeProvider.class);

    @Override
    public boolean onCreate() {
        final Context context = getContext();

        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        config = new Configuration(PreferenceManager.getDefaultSharedPreferences(context));

        return true;
    }

    private static Uri.Builder contentUri(@Nonnull final String packageName, final boolean offline) {
        final Uri.Builder builder =
                Uri.parse("content://" + packageName + ".btc_dynamic_fee").buildUpon();
        if (offline)
            builder.appendQueryParameter(QUERY_PARAM_OFFLINE, "1");
        return builder;
    }

    public static Uri contentUriBtcFee(@Nonnull final String packageName,
                                        @Nonnull final String coinSymbol,
                                        final boolean offline) {
        final Uri.Builder uri = contentUri(packageName, offline);
        uri.appendPath("to-fee").appendPath(coinSymbol);
        return uri.build();
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues  values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(final Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static String getBtcFee(final Context context, @Nonnull final String coinSymbol) {
        String fee = "0.003";

        if (context != null) {
            final Uri uri = contentUriBtcFee(context.getPackageName(), coinSymbol, false);
            final Cursor cursor = context.getContentResolver().query(uri, null, null,
                    new String[]{null}, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    fee = getFeeCursor(cursor);
                }
                cursor.close();
            }
        }

        return fee;
    }

    public static String getFeeCursor(@Nonnull final Cursor cursor) {
        final String fee = cursor.getString(cursor.getColumnIndexOrThrow( "btc_fee_" ));
        return fee;
    }


    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection,
                        final String[] selectionArgs, final String sortOrder) {
        final long now = System.currentTimeMillis();

        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 2) {
            throw new IllegalArgumentException("Unrecognized URI: " + uri);
        }

        final boolean offline = uri.getQueryParameter(QUERY_PARAM_OFFLINE) != null;
        long lastUpdated;

        final String symbol;
        final boolean isFetchingFee;

        if (pathSegments.get(0).equals("to-fee")) {
            isFetchingFee = true;
            symbol = pathSegments.get(1);
            lastUpdated = symbol.equals(lastLocalCurrency) ? localFeeUpdated : 0;
        }
        else {
            throw new IllegalArgumentException("Unrecognized URI path: " + uri);
        }

        if (!offline && (lastUpdated == 0 || now - lastUpdated > Constants.RATE_UPDATE_FREQ_MS)) {
            URL url;
            try {
                url = new URL( BASE_URL );
            } catch (final MalformedURLException x) {
                throw new RuntimeException(x); // Should not happen
            }

            JSONObject newBtcFee = requestExchangeRatesJson(url);
            String newFeeVal_ = parseFee_(newBtcFee);

            //log.info("we are here fee {} value", newFeeVal_);

            if (newFeeVal_ != null) {
                if (isFetchingFee) {
                    localBtcFee = newFeeVal_;
                    localFeeUpdated = now;
                    lastLocalCurrency = symbol;
                    config.setBtcFee_( newFeeVal_ );
                }
            }
        }

        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, "btc_id", "btc_fee_"});
        addRow(cursor, localBtcFee);
        return cursor;
    }

    private void addRow(MatrixCursor cursor, String fee_) {

        final String codeId = "BTC";
        cursor.newRow().add(codeId.hashCode())
                .add(codeId)
                .add(fee_);
    }

    @Nullable
    private JSONObject requestExchangeRatesJson(final URL url) {
        // Return null if no connection
        final NetworkInfo activeInfo = connManager.getActiveNetworkInfo();
        if (activeInfo == null || !activeInfo.isConnected()) return null;

        final long start = System.currentTimeMillis();

        OkHttpClient client = NetworkUtils.getHttpClientUsual(getContext().getApplicationContext());
        Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                log.info("fetched btc_fee from {}, took {} ms", url,
                        System.currentTimeMillis() - start);
                return new JSONObject(response.body().string());
            } else {
                log.warn("Error HTTP code '{}' when fetching btc_fee from {}",
                        response.code(), url);
            }
        } catch (IOException e) {
            log.warn("Error while data accessing '{}' when fetching btc_fee from {}", e.getMessage(), url);
        } catch (JSONException e) {
            log.warn("Could not parse btc_fee JSON: {}", e.getMessage());
        }
        return null;
    }

    private String parseFee_(JSONObject json) {
        if (json == null) return null;

        String dFee = "";
        try {
            for (final Iterator<String> i = json.keys(); i.hasNext(); ) {
                final String toSymbol = i.next();

                final String rateStr = json.optString(toSymbol, null);
                if (rateStr != null) {
                    try {
                        dFee = rateStr;
                    } catch (final Exception x) {
                        log.debug("ignoring exception_ {}", x.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("problem parsing btc_fee: {}", e.getMessage());
        }

        if (dFee.length() == 0) {
            return null;
        } else {
            return dFee;
        }
    }
}
