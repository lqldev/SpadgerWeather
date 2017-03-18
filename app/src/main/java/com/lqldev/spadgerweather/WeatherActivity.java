package com.lqldev.spadgerweather;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lqldev.spadgerweather.gson.ForeCast;
import com.lqldev.spadgerweather.gson.Now;
import com.lqldev.spadgerweather.gson.Weather;
import com.lqldev.spadgerweather.util.HttpUtil;
import com.lqldev.spadgerweather.util.Utility;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public static final String TAG = "WeatherActivity";

    //天气API接口相关配置
    private static final String WEATHER_API_ADDRESS = "http://guolin.tech/api/weather";
    private static final String WEATHER_API_PARAM_NAME_WEATHER_ID = "cityid";
    private static final String WEATHER_API_PARAM_NAME_KEY = "key";
    private static final String WEATHER_API_KEY = "29cdf0722dd347eaa5be191ab05aea5f";
    private static final String WEATHER_API_BING_DAILY_PICTURE_ADDRESS = "http://guolin.tech/api/bing_pic";
    /**
     * 天气界面各种控件
     */
    private ImageView weatherBackgroundImage;
    private SwipeRefreshLayout swipeRefresh;
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

    private String mWeatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //获取各个控件实例

        weatherBackgroundImage = (ImageView) findViewById(R.id.weather_background_image);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
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

        String bingPictureUrl = preferences.getString("bing_picture", null);
        String bingPicUpdateDate = preferences.getString("bing_picture_update_date", null);
        //如果有缓存图片地址而且是今天的，直接显示，否则获取今天的必应每日一图
        if (bingPictureUrl != null && bingPicUpdateDate !=null && bingPicUpdateDate.equals(getNowDate())) {
            Glide.with(this).load(bingPictureUrl).into(weatherBackgroundImage);
        } else {
            loadBingPicture();
        }

        String weatherString = preferences.getString("weather", null);
        if (weatherString != null) {
            //本地有缓存，直接显示缓存的天气信息
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //没有缓存，从服务器查询天气信息
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        //下拉刷新功能
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
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
        weatherWindText.setText(wind.level + "级 " + wind.direction + (wind.direction.endsWith("风") ? "" : "风"));
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
     * 从服务器查询天气信息，同时缓存所查询到的最新天气，然后显示到界面上。
     *
     * @param weatherId
     */
    private void requestWeather(final String weatherId) {
        String weatherUrl = WEATHER_API_ADDRESS
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
                        swipeRefresh.setRefreshing(false);
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
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Log.d(TAG, "requestWeather > onResponse: 获取天气失败。");
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 获取必应每日一图
     */
    private void loadBingPicture() {
        HttpUtil.sendOkHttpRequest(WEATHER_API_BING_DAILY_PICTURE_ADDRESS, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "loadBingPicture > onFailure: 请求必应图片出错。", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String pictureUrl = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_picture", pictureUrl);
                editor.putString("bing_picture_update_date", getNowDate());
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(pictureUrl).into(weatherBackgroundImage);
                    }
                });
            }
        });
    }

    /**
     * 获取今天的日期
     * @return
     */
    private String getNowDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String today = df.format(new Date());
        return today;
    }
}
