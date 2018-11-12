package com.maple.mapleretrofit;

import com.maple.mapleretrofit.converter.FastJsonConverterFactory;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Retrofit 创建工厂
 */
public class RetrofitFactory {
    public static Retrofit.Builder create() {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(FastJsonConverterFactory.create());
    }
}
