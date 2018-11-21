package com.maple.mapleretrofit;

import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SchedulerProvider {
    /**
     *
     * @return normal http request
     */
    public static  <T> ObservableTransformer<T, T> applyHttp(){
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     *
     * @return Download or Upload request
     */
    public static  <T> ObservableTransformer<T, T> applyrDownload(){
        return upstream -> upstream.subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }
}
