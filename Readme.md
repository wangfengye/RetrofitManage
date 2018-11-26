# Retrofit2 + Rxjava2 封装
![avatar](https://img.shields.io/badge/maplretrofit-v1.0-green.svg)

### 功能
* 引入
```
 implementation 'com.github.wangfengye:RetrofitManage:v1.1'
 ```
* demo
```java
   gankApi = RetrofitFactory.create().baseUrl("http://gank.io").build().create(GankApi.class);
```
* 支持动态修改baseUrl;
```java
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(UpdateUrlInterceptor.create("http://gank.io"))
                .build();
        GankApi api = RetrofitFactory.create()
                .baseUrl("http://gank.io")
                .client(client)
                .build().create(GankApi.class);
        //修改url
        UpdateUrlInterceptor.setBaseUrl("http://www.baidu.com");
```
* 请求重试 `observable.retryWhen(new RetryWhenNetworkException())`默认重试3次重试延时分别为(1s,2s,4s)
```java
     gankApi.getError().retryWhen(new RetryWhenNetworkException())
                .compose(SchedulerProvider.applyHttp())
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.i(TAG, "onNext: " + s);
                    }
                });
```
* https认证(分为跳过认证,文件认证)
```java
  // 跳过认证
  OkHttpFactory.ignoreHttps(builder);
  // 文件认证
  new OkHttpClient().sslSocketFactory(OkHttpFactory.getSSLSocketFactory(<证书类型>,<协议>,<密码>,<文件输入流>));
```
* 缓存功能
```java
   OkHttpFactory.setCache(builder, 1024 * 1024 * 10, getApplicationContext());
```
* cookie功能
```java
      OkHttpFactory.setCookie(builder,getApplicationContext())
```

* 下载返回进度(`DownloadObserver`)

> Rxjava的订阅线程需设置为IO线程
 
* 上传文件 `UploadPartUtil`(分为是否返回进度的两种版本)

> 由于retrofit本身是不支持文件上传进度显示,因此返回进度的版本重写了retrofit请求体,这部分未使用Rxjava2的返回体,详情
见MainActivity中的Demo
