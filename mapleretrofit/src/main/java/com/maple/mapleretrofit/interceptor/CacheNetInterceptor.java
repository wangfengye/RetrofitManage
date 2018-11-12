package com.maple.mapleretrofit.interceptor;

import android.content.Context;

import com.maple.mapleretrofit.util.NetUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 配置response必须使用网络拦截器(addNetworkInterceptor)
 */
public class CacheNetInterceptor implements Interceptor {
    private Context context;

    public CacheNetInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (NetUtil.isNetAvailable(context)&& !request.cacheControl().toString().isEmpty()) {
            request = request.newBuilder().removeHeader("Pragma")//移除干扰的头信息
                    .header("Cache-Control", request.cacheControl().toString())
                    .build();
            return chain.proceed(request);
        }
        return chain.proceed(request);
    }
}
