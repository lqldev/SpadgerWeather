package com.lqldev.spadgerweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017-03-17.
 */

public class ForeCast {
    public String date;

    @SerializedName("cond")
    public More more;

    @SerializedName("tmp")
    public Temperature temperature;

    /**
     * 内部类定义
     */
    public class More {
        @SerializedName("txt_d")
        public String day_info;
        @SerializedName("txt_n")
        public String night_info;
    }

    public class Temperature {
        public String max;
        public String min;
    }
}
