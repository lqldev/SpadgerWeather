package com.lqldev.spadgerweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017-03-17.
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    /**
     * 内部类定义
     */
    public class Comfort{
        @SerializedName("txt")
        public String info;
    }

    public class CarWash {
        @SerializedName("txt")
        public String info;
    }

    public class Sport {
        @SerializedName("txt")
        public String info;
    }
}
