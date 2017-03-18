package com.lqldev.spadgerweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.util.LogTime;
import com.google.gson.Gson;
import com.lqldev.spadgerweather.db.City;
import com.lqldev.spadgerweather.db.County;
import com.lqldev.spadgerweather.db.Province;
import com.lqldev.spadgerweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017-03-17.
 */

public class Utility {
    private static final String TAG = "Utility";

    /**
     * 解析json格式的省份列表字符串，并保存到数据库。
     *
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject jsonProvince = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(jsonProvince.getInt("id"));
                    province.setProvinceName(jsonProvince.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                Log.e(TAG, "handleProvinceResponse: json字符串解析错误：" + response, e);
            }
        }
        return false;
    }

    /**
     * 解析json格式的某个省份的全部城市列表字符串，并保存到数据库。
     *
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject jsonCity = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(jsonCity.getInt("id"));
                    city.setCityName(jsonCity.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                Log.e(TAG, "handleCityResponse: json字符串解析错误：" + response, e);
            }
        }
        return false;
    }

    /**
     * 解析json格式的某个城市所有县的列表字符串，并保存到数据库。
     *
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject jsonCounty = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(jsonCounty.getString("weather_id"));
                    county.setCountyName(jsonCounty.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                Log.e(TAG, "handleCountyResponse: json字符串解析错误：" + response, e);
            }
        }
        return false;
    }

    /**
     * 解析json格式的天气信息字符串。
     *
     * @param response
     * @return 天气实体类对象
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherString = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherString, Weather.class);
        } catch (Exception e) {
            Log.e(TAG, "handleWeatherResponse: json字符串解析错误：" + response, e);
        }
        return null;
    }
}
