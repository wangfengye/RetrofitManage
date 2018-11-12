package com.maple.mapleretrofit;


import android.util.Log;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * Retrofit 请求重试
 */

@SuppressWarnings("unused")
public class RetryWhenNetworkException implements Function<Observable<? extends Throwable>, Observable<?>> {
    private int mRetryCount = 3;
    private int mInitDelay = 0;//单位:s
    private int mExponentialBase = 2;

    public RetryWhenNetworkException() {
    }

    /**
     * 重试时间公式  mInitDelay + exponentialBase* 2 ^(mRetiedCount-1)
     *
     * @param mRetryCount     重试次数
     * @param mInitDelay      首次重试时间
     * @param exponentialBase 重试时间增长基数
     */
    public RetryWhenNetworkException(int mRetryCount, int mInitDelay, int exponentialBase) {
        this.mRetryCount = mRetryCount;
        this.mInitDelay = mInitDelay;
        this.mExponentialBase = exponentialBase;
    }

    @Override
    public Observable<?> apply(final Observable<? extends Throwable> input) {
        return input.zipWith(Observable.range(1, mRetryCount + 1), new BiFunction<Throwable, Integer, ThrowableWrapper>() {
            @Override
            public ThrowableWrapper apply(Throwable throwable, Integer integer) throws Exception {
                // range 操作符每次+1;
                return new ThrowableWrapper(integer, throwable);
            }
        }).flatMap(new Function<ThrowableWrapper, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(ThrowableWrapper wrapper) throws Exception {
                //网络相关异常才会发起重试请求
                if (needRetry(wrapper.throwable) && wrapper.mRetiedCount <= mRetryCount) {
                    long delayTime = getDelayTime(mInitDelay, wrapper.mRetiedCount, mExponentialBase);
                    Log.i("Retrofit-retry", wrapper.mRetiedCount + "次 --- " + delayTime + "s");
                    return Observable.timer(delayTime, TimeUnit.SECONDS, Schedulers.trampoline());

                }
                return Observable.error(wrapper.throwable);

            }
        });
    }

    /**
     * 根据异常判断是否需要重试
     *
     * @param throwable 异常
     * @return 是否需要重试
     */
    private boolean needRetry(Throwable throwable) {
        return throwable instanceof ConnectException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof TimeoutException
                || throwable instanceof HttpException;
    }

    /**
     * 重试时间算法,可重新该方法实现自己的重试时间递增机制
     *
     * @param initDelay       初始延时
     * @param count           当前是0第几次重试
     * @param exponentialBase 重试递增的基数
     * @return 重试延时
     */
    private int getDelayTime(int initDelay, int count, int exponentialBase) {
        return (int) (initDelay + Math.pow(exponentialBase, count - 1));
    }

    private class ThrowableWrapper {
        private int mRetiedCount;
        private Throwable throwable;

        ThrowableWrapper(int mRetiedCount, Throwable throwable) {
            this.mRetiedCount = mRetiedCount;
            this.throwable = throwable;
        }
    }
}
