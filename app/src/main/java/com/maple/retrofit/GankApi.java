package com.maple.retrofit;


import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface GankApi {
    /**
     * 请求一个不存在的地址 用于测试请求重发
     *
     * @return nodata
     */
    @GET("nofound")
    Observable<String> getError();


    @GET("api/data/福利/{limit}/{page}")
    Observable<String> getPictures(@Path("limit") int limit, @Path("page") int page);

    @Headers("Cache-Control: max-age=30")
    @GET("static/ce.html")
    Observable<String> getHtml();

    @Streaming
    @GET("https://116.62.223.98:8080/api/test/app/file/jxh/latest")
    Observable<ResponseBody> downLoadLatestApp();

    @Multipart
    @POST("http://csbianjian.cn:86/file/upload")
    Observable<String> upload(@Part MultipartBody.Part file);

    @Multipart
    @POST("http://csbianjian.cn:86/file/upload")
    Call<String> uploadWithProgress(@Part MultipartBody.Part file);
}
