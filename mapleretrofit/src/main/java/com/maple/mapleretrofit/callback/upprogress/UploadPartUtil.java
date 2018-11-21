package com.maple.mapleretrofit.callback.upprogress;

import java.io.File;
import java.nio.file.Files;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UploadPartUtil {
    public static MultipartBody.Part create(File file) {
        RequestBody body = RequestBody.create(MediaType.parse("application/otcet-stream"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);
        return part;
    }
    public static MultipartBody.Part createwithProgress(File file,ProgressCallback callback) {
        RequestBody body = RequestBody.create(MediaType.parse("application/otcet-stream"), file);
        UpRequestBody ur = new UpRequestBody(body, callback);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), ur);
        return part;
    }
}
