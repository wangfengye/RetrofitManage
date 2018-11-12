package com.maple.mapleretrofit;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.maple.mapleretrofit.interceptor.CacheInterceptor;
import com.maple.mapleretrofit.interceptor.CacheNetInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class OkHttpFactory {
    /**
     * 忽略https认证
     *
     * @param builder OkHttpClient.builder
     * @return kHttpClient.builder
     */
    public static OkHttpClient.Builder ignoreHttps(OkHttpClient.Builder builder) {
        return builder.sslSocketFactory(getIgnoreSSLSocketFactory()).hostnameVerifier(getHostnameVerifier());
    }

    /**
     * 添加缓存功能
     * 无网络直接使用缓存,有网根据头信息`Cache-Control`控制缓存
     *
     * @param builder OkHttpClient.builder
     * @param size    最大缓存空间
     * @param context 全局context
     * @return OkHttpClient.builder
     */
    public static OkHttpClient.Builder setCache(OkHttpClient.Builder builder, int size, Context context) {
        File file = new File(context.getCacheDir(), "cache");
        Cache cache = new Cache(file, size);
        return builder.cache(cache)
                .addInterceptor(new CacheInterceptor(context))
                .addNetworkInterceptor(new CacheNetInterceptor(context));
    }

    /**
     * 添加cookie;
     *
     * @param builder OkHttpClient.builder
     * @param context 全局context
     * @return OkHttpClient.builder
     */
    public static OkHttpClient.Builder setCookie(OkHttpClient.Builder builder, Context context) {
        PersistentCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        return builder.cookieJar(cookieJar);
    }

    /**
     * 使用自定义证书
     *
     * @param type     证书类型
     * @param protocol 协议
     * @param password 密码
     * @param is       证书文件的输入流
     * @return SSLSocket
     */
    public static SSLSocketFactory getSSLSocketFactory(String type, String protocol, String password, InputStream is) {
        SSLContext sslContext = null;
        SSLSocketFactory sslSocketFactory = null;
        try {
            sslContext = SSLContext.getInstance(protocol);
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore tks = KeyStore.getInstance(type);
            tks.load(is, password.toCharArray());
            trustManagerFactory.init(tks);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sslSocketFactory;
    }

    /**
     * 忽略证书认证,直接使用证书
     *
     * @return SSLSocket
     */
    private static SSLSocketFactory getIgnoreSSLSocketFactory() {
        TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                }
        };
        SSLContext sslContext;
        SSLSocketFactory sslSocketFactory = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagers, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslSocketFactory;
    }

    @SuppressWarnings("all")
    private static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier verifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        return verifier;
    }

}
