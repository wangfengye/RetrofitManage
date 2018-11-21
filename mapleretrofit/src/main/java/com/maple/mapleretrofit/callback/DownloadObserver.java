package com.maple.mapleretrofit.callback;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.observers.DisposableObserver;
import okhttp3.ResponseBody;

/**
 * 下载文件的Observer
 */
public abstract class DownloadObserver extends DisposableObserver<ResponseBody> {
    private static final String TAG = "DownloadObserver";
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public abstract void onDownloadStart();

    /**
     * @param percent 已下载百分比
     * @param loaded  已下载长度
     * @param allSize 总长度
     */
    public abstract void onDownloading(int percent, long loaded, long allSize);

    public abstract void onDownloaded();

    /**
     * @return 设置下载的文件
     */
    public abstract File getFile();


    @Override
    public void onNext(ResponseBody body) {
        mainHandler.post(this::onDownloadStart);
        download(body);

    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "onError: " + e.getMessage());
    }

    @Override
    public void onComplete() {

    }

    @RequiresPermission("RITE_EXTERNAL_STORAGE")
    private void download(ResponseBody body) {
        File futureStudioIconFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        int pro = 0;
        try {
            futureStudioIconFile = getFile();

            byte[] fileReader = new byte[4096];

            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;

            inputStream = body.byteStream();
            outputStream = new FileOutputStream(futureStudioIconFile);

            while (true) {
                int read = inputStream.read(fileReader);

                if (read == -1) {
                    break;
                }

                outputStream.write(fileReader, 0, read);

                fileSizeDownloaded += read;
                int proC = (int) (fileSizeDownloaded * 100 / fileSize);
                if (pro < proC) {
                    pro = proC;
                    final int proFinal = pro;
                    final long fileSizeDownloadedFinal = fileSizeDownloaded;
                    mainHandler.post(() -> onDownloading(proFinal, fileSizeDownloadedFinal, fileSize));
                }
            }
            outputStream.flush();
            mainHandler.post(this::onDownloaded);
        } catch (IOException e) {
            onError(e);
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
