package com.lqldev.spadgerweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.lqldev.spadgerweather.MainActivity;
import com.lqldev.spadgerweather.WeatherActivity;
import com.lqldev.spadgerweather.gson.Weather;
import com.lqldev.spadgerweather.util.HttpUtil;
import com.lqldev.spadgerweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017-03-19.
 */

public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updatePicture();
        updateWeather();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int intervalMS = 8 * 60 * 60 * 1000;//8小时
        long triggerAtTime = SystemClock.elapsedRealtime() + intervalMS;
        Intent loopIntent = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, loopIntent, 0);
        //先取消上一次的，防止一直开启应用，后台运行多个提醒任务
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        String weatherString = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(WeatherActivity.PK_WEATHER, null);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            final String weatherId = weather.basic.weatherId;
            HttpUtil.sendOkHttpRequest(WeatherActivity.getWeatherRequestUrl(weatherId), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "updateWeather 定时更新天气失败", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseString);
                    //能正确解析并且状态成功
                    if (weather != null && WeatherActivity.WEATHER_API_STATUS_OK.equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString(WeatherActivity.PK_WEATHER, responseString);
                        editor.apply();
                        Log.d(TAG, "updateWeather > onResponse: 定时更新天气成功并缓存 " + responseString);
                    } else {
                        Log.d(TAG, "updateWeather > onResponse: 定时请求天气成功，但天气信息解析失败\n" + responseString);
                    }
                }
            });
        }
    }

    private void updatePicture() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        String picUrl = preferences.getString(WeatherActivity.PK_WEATHER_PICTURE, null);
        String picUpdateDate = preferences.getString(WeatherActivity.PK_WEATHER_PICTURE_UPDATE_DATE, null);
        if (picUpdateDate == null || picUpdateDate == null || !picUpdateDate.equals(Utility.getNowDate())) {
            HttpUtil.sendOkHttpRequest(WeatherActivity.WEATHER_API_BING_DAILY_PICTURE_ADDRESS, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "updatePicture > onFailure: 定时请求必应图片失败", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String pictureUrl = response.body().string();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(WeatherActivity.PK_WEATHER_PICTURE, pictureUrl);
                    editor.putString(WeatherActivity.PK_WEATHER_PICTURE_UPDATE_DATE, Utility.getNowDate());
                    editor.apply();
                    Log.d(TAG, "updatePicture > onResponse: 定时更新必应图片成功 URl：" + pictureUrl);
                }
            });
        }
    }
}
