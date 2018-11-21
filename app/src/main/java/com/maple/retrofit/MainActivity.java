package com.maple.retrofit;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.maple.mapleretrofit.SchedulerProvider;
import com.maple.mapleretrofit.callback.BaseObserver;
import com.maple.mapleretrofit.OkHttpFactory;
import com.maple.mapleretrofit.RetrofitFactory;
import com.maple.mapleretrofit.RetryWhenNetworkException;
import com.maple.mapleretrofit.callback.DownloadObserver;
import com.maple.mapleretrofit.callback.upprogress.ProgressCallback;
import com.maple.mapleretrofit.callback.upprogress.UpRequestBody;
import com.maple.mapleretrofit.callback.upprogress.UploadPartUtil;
import com.maple.mapleretrofit.interceptor.CacheInterceptor;
import com.maple.mapleretrofit.interceptor.UpdateUrlInterceptor;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

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
        findViewById(R.id.btn_download).setOnClickListener(view -> download());
        findViewById(R.id.btn_upLoad).setOnClickListener(view -> upload());
        findViewById(R.id.btn_upLoad_progress).setOnClickListener(view -> uploadWithProgress());
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

    // 下载文件(返回进度)
    private void download() {
        GankApi api = RetrofitFactory.create()
                .baseUrl(" http://csbianjian.cn:86")
                .client(OkHttpFactory.ignoreHttps(new OkHttpClient.Builder()).build())
                .build().create(GankApi.class);
        api.downLoadLatestApp()
                .compose(SchedulerProvider.applyrDownload())
                .subscribe(new DownloadObserver() {
                    @Override
                    public void onDownloadStart() {
                        Toast.makeText(MainActivity.this, "下载开始", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onDownloadStart: ");
                    }

                    @Override
                    public void onDownloading(int percent, long loaded, long allSize) {
                        Log.i(TAG, "onDownloading: " + percent + "%~~~" + loaded + "~~~" + allSize);
                    }

                    @Override
                    public void onDownloaded() {
                        Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onDownloaded: ");
                    }

                    @Override
                    public File getFile() {
                        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/", "test.as");
                        if (!f.exists()) {
                            try {
                                if (!f.getParentFile().exists()) {
                                    //父目录不存在 创建父目录
                                    Log.d(TAG, "creating parent directory...");
                                    if (!f.getParentFile().mkdirs()) {
                                        Log.e(TAG, "created parent directory failed.");

                                    }
                                }
                                f.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return f;
                    }

                    @Override
                    public void onError(Throwable e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
    }

    // 上传文件(不返回进度)
    private void upload() {
        GankApi api = RetrofitFactory.create()
                .baseUrl(" http://csbianjian.cn:86")
                .client(OkHttpFactory.ignoreHttps(new OkHttpClient.Builder()).build())
                .build().create(GankApi.class);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/", "IMG_20181118_101506.jpg");


        api.upload(UploadPartUtil.create(file))
                .compose(SchedulerProvider.applyHttp())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onNext: " + s);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG, "onComplete: ");
                    }
                });
        Toast.makeText(MainActivity.this, "上传开始", Toast.LENGTH_SHORT).show();
    }

    // 上传文件(返回进度)
    private void uploadWithProgress() {
        GankApi api = RetrofitFactory.create()
                .baseUrl(" http://csbianjian.cn:86")
                .client(OkHttpFactory.ignoreHttps(new OkHttpClient.Builder()).build())
                .build().create(GankApi.class);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/", "IMG_20181118_101506.jpg");
        ProgressCallback<String> callback = new ProgressCallback<String>() {
            @Override
            public void onSuccess(Call<String> call, Response<String> response) {
                Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onSuccess: " + response.body());
            }

            @Override
            public void onLoading(long total, long progress) {
                Log.i(TAG, "onLoading: " + progress + " <<< " + total);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        api.uploadWithProgress(UploadPartUtil.createwithProgress(file, callback)).enqueue(callback);
        Toast.makeText(MainActivity.this, "上传开始", Toast.LENGTH_SHORT).show();

    }

}
