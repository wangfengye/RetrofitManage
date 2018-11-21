package com.maple.mapleretrofit.callback.upprogress;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 返回进度的callback
 *
 * @param <T>
 */
public abstract class ProgressCallback<T> implements Callback<T> {
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onSuccess(call, response);
        } else {
            onFailure(call, new Throwable(response.message()));
        }
    }

    public abstract void onSuccess(Call<T> call, Response<T> response);

    public abstract void onLoading(long total, long progress);
}
