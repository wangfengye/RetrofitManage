package com.maple.mapleretrofit.interceptor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.maple.mapleretrofit.util.NetUtil;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 只缓存Get请求
 * 缓存策略,无网络读取缓存,有网络,且不超时读取缓存
 * 头信息 "Cache-Control: max-age=<seconds>"
 */
public class CacheInterceptor implements Interceptor {
    public static final String TAG = CacheInterceptor.class.getSimpleName();
    private Context context;

    public CacheInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!NetUtil.isNetAvailable(context)) {
            // 网络不可用,走缓存
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
        }
        return chain.proceed(request);

    }

}
