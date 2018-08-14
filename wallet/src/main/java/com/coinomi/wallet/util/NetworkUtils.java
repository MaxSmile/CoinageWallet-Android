package com.coinomi.wallet.util;

import android.content.Context;
import android.util.Log;

import com.coinomi.wallet.Constants;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;

/**
 * @author John L. Jegutanis
 */
public class NetworkUtils {
    private static OkHttpClient httpClient;
    private static OkHttpClient httpClient2;

    public static OkHttpClient getHttpClient(Context context) {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
            httpClient.interceptors().add(new LoggingInterceptor());
            httpClient.setConnectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS));
            httpClient.setConnectTimeout(Constants.NETWORK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            // Setup cache
            File cacheDir = new File(context.getCacheDir(), Constants.HTTP_CACHE_NAME);
            Cache cache = new Cache(cacheDir, Constants.HTTP_CACHE_SIZE);
            httpClient.setCache(cache);
        }
        return httpClient;
    }

    public static OkHttpClient getHttpClientUsual(Context context) {
        if (httpClient2 == null) {
            httpClient2 = new OkHttpClient();
            httpClient2.interceptors().add(new LoggingInterceptor());
            httpClient2.setConnectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT));
            httpClient2.setConnectTimeout(Constants.NETWORK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            // Setup cache
            File cacheDir = new File(context.getCacheDir(), Constants.HTTP_CACHE_NAME+"2");
            Cache cache = new Cache(cacheDir, Constants.HTTP_CACHE_SIZE);
            httpClient2.setCache(cache);
        }
        return httpClient2;
    }
    public static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.d("OkHttp", String.format("--> Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.d("OkHttp", String.format("<-- Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            MediaType contentType = response.body().contentType();
            String content = response.body().string();
            Log.d("OkHttp", content);

            ResponseBody wrappedBody = ResponseBody.create(contentType, content);
            return response.newBuilder().body(wrappedBody).build();
        }
    }
}
