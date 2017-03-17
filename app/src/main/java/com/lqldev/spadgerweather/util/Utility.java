package com.lqldev.spadgerweather.util;

import android.text.TextUtils;

import com.lqldev.spadgerweather.db.City;
import com.lqldev.spadgerweather.db.County;
import com.lqldev.spadgerweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017-03-17.
 */

public class Utility {
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject jsonProvince = allProvinces.getJSONObject(i);
                    Province province =new Province();
                    province.setProvinceCode(jsonProvince.getInt("id"));
                    province.setProvinceName(jsonProvince.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityResponse(String response,int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject jsonCity = allCities.getJSONObject(i);
                    City city =new City();
                    city.setCityCode(jsonCity.getInt("id"));
                    city.setCityName(jsonCity.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountyResponse(String response,int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject jsonCounty = allCounties.getJSONObject(i);
                    County county =new County();
                    county.setWeatherId(jsonCounty.getString("weather_id"));
                    county.setCountyName(jsonCounty.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
