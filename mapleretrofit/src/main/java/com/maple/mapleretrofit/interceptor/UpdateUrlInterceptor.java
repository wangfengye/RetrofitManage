package com.maple.mapleretrofit.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 动态url拦截器
 * <p>
 * 添加拦截器后,发送请求会被该拦截器,更改请求的域名(例如:https://www.baidu.com)
 */
@SuppressWarnings("unused")
public class UpdateUrlInterceptor implements Interceptor {
    private static String mBaseUrl;

    public static String getBaseUrl() {
        return mBaseUrl;
    }

    public static void setBaseUrl(String mBaseUrl) {
        UpdateUrlInterceptor.mBaseUrl = mBaseUrl;
    }

    private UpdateUrlInterceptor() {
    }

    public static UpdateUrlInterceptor create(String baseUrl) {
        mBaseUrl = baseUrl;
        return Builder.mInterceptor;
    }

    private static class Builder {
        private static UpdateUrlInterceptor mInterceptor = new UpdateUrlInterceptor();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String url = request.url().url().getPath();
        request = request.newBuilder()
                .url(mBaseUrl + url).build();
        return chain.proceed(request);
    }
}
