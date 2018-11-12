package com.maple.retrofit;


import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

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

}
