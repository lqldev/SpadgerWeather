package com.lqldev.spadgerweather;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.util.LogTime;
import com.google.gson.annotations.SerializedName;
import com.lqldev.spadgerweather.gson.ForeCast;
import com.lqldev.spadgerweather.gson.Now;
import com.lqldev.spadgerweather.gson.Weather;
import com.lqldev.spadgerweather.util.HttpUtil;
import com.lqldev.spadgerweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public static final String TAG = "WeatherActivity";

    //天气API接口相关配置
    private static final String WEATHER_API_ADDR = "http://guolin.tech/api/weather";
    private static final String WEATHER_API_PARAM_NAME_WEATHER_ID = "cityid";
    private static final String WEATHER_API_PARAM_NAME_KEY = "key";
    private static final String WEATHER_API_KEY = "29cdf0722dd347eaa5be191ab05aea5f";
    /**
     * 天气界面各种控件
     */
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView weatherWindText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //获取各个控件实例
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        weatherWindText = (TextView) findViewById(R.id.weather_wind_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            //本地有缓存，直接显示缓存的天气信息
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //没有缓存，从服务器查询天气信息
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
    }

    /**
     * 显示天气信息到界面上
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        Now.Wind wind = weather.now.wind;
        //标题信息
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        //当天天气信息
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        weatherWindText.setText(wind.level + "级 " + wind.direction + (wind.direction.endsWith("风")?"":"风"));
        //预报信息
        forecastLayout.removeAllViews();
        String day_info;
        String night_info;
        for (ForeCast forecast : weather.foreCastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minTex = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);

            day_info = forecast.more.day_info;
            night_info = forecast.more.night_info;
            if (night_info != null && !night_info.equals(day_info)) {
                day_info = day_info + " 转 " + night_info;
            }
            infoText.setText(day_info);

            maxText.setText(forecast.temperature.max);
            minTex.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        //空气质量信息
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        //生活建议信息
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动指数：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        //所有设置完成后，显示天气信息
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 从服务器查询天气信息，然后显示到界面上，同时缓存所查询到的最新天气
     *
     * @param weatherId
     */
    private void requestWeather(final String weatherId) {
        String weatherUrl = WEATHER_API_ADDR
                + "?" + WEATHER_API_PARAM_NAME_WEATHER_ID + "=" + weatherId
                + "&" + WEATHER_API_PARAM_NAME_KEY + "=" + WEATHER_API_KEY;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "requestWeather > onFailure: 请求天气信息失败。", e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //需要在匿名类中线程中访问，需要定义为final
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                                    .edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Log.d(TAG, "requestWeather > onResponse: 获取天气失败。");
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
