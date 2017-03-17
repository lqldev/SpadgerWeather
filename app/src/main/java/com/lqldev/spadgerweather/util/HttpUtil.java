package com.lqldev.spadgerweather.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017-03-17.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String addr, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(addr).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
