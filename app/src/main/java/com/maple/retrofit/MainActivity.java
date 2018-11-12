package com.maple.retrofit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.maple.mapleretrofit.SchedulerProvider;
import com.maple.mapleretrofit.callback.BaseObserver;
import com.maple.mapleretrofit.OkHttpFactory;
import com.maple.mapleretrofit.RetrofitFactory;
import com.maple.mapleretrofit.RetryWhenNetworkException;
import com.maple.mapleretrofit.interceptor.CacheInterceptor;
import com.maple.mapleretrofit.interceptor.UpdateUrlInterceptor;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static String baseUrl = "https://www.baidu.com";
    private GankApi gankApi;
    private String[] urls = new String[]{"https://gank.io", "https://www.baidu.com"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gankApi = RetrofitFactory.create().baseUrl("http://gank.io").build().create(GankApi.class);
        findViewById(R.id.btn_retry).setOnClickListener(v -> send());
        findViewById(R.id.btn_active_url).setOnClickListener(v -> sendActive());
        findViewById(R.id.btn_cache).setOnClickListener(view -> sendCache());
    }

    // 请求重试
    public void send() {
        Log.i(TAG, "请求重试:");
        gankApi.getError().retryWhen(new RetryWhenNetworkException(3, 0, 3))
                .compose(SchedulerProvider.applyHttp())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.i(TAG, "onNext: " + s);
                    }
                });
    }

    //请求成功后更换baseUrl
    private void sendActive() {
        Log.i(TAG, "sendActive: ");
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(UpdateUrlInterceptor.create("http://gank.io"))
                .build();
        GankApi api = RetrofitFactory.create()
                .baseUrl("http://gank.io")
                .client(client)
                .build().create(GankApi.class);
        api.getPictures(10, 1)
                .compose(SchedulerProvider.applyHttp())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        s = s.substring(0, 28);
                        Log.i(TAG, "onNext: " + s);
                        UpdateUrlInterceptor.setBaseUrl("http://www.baidu.com");
                        sendAfterChange(api);
                    }
                });
    }

    private void sendAfterChange(GankApi api) {
        api.getPictures(10, 1)
                .compose(SchedulerProvider.applyHttp())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        s = s.substring(0, 28);
                        Log.i(TAG, "onNext: " + s);
                    }
                });
    }

    //带缓存的客户端
    public void sendCache() {
        Log.i(TAG, "sendCache: ");
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new CacheInterceptor(getApplicationContext()));
        // 配置缓存
        OkHttpFactory.setCache(builder, 1024 * 1024 * 10, getApplicationContext());
        // 配置 忽略https
        OkHttpFactory.ignoreHttps(builder);
        GankApi api = RetrofitFactory.create()
                .baseUrl(" http://csbianjian.cn:86")
                .client(builder.build())
                .build().create(GankApi.class);
        api.getHtml()
                .compose(SchedulerProvider.applyHttp())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.i(TAG, "onNext: " + s);
                    }
                });

    }

}
