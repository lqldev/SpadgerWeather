package com.lqldev.spadgerweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017-03-17.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    /**
     * 内部类定义
     */
    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
