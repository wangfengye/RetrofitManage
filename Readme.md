# Retrofit2 + Rxjava2 封装

### funture
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