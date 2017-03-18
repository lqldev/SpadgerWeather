package com.lqldev.spadgerweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017-03-17.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public Wind wind;

    /**
     * 内部类定义
     */
    public class More {
        @SerializedName("txt")
        public String info;
    }

    public class Wind {

        @SerializedName("dir")
        public String direction;

        @SerializedName("sc")
        public String level;
    }
}
