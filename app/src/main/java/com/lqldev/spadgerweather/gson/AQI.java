package com.lqldev.spadgerweather.gson;

import com.lqldev.spadgerweather.db.City;

/**
 * Created by Administrator on 2017-03-17.
 */

public class AQI {

    public AQICity city;

     /**
     * 内部类定义
     */
    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
