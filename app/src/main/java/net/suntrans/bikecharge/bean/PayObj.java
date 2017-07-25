package net.suntrans.bikecharge.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Looney on 2017/6/2.
 */

public class PayObj {
    public String appid;
    public String partnerid;
    public String prepayid;

    @SerializedName("package")
    public String packages;
    public String noncestr;
    public String timestamp;
    public String sign;

    @Override
    public String toString() {
        return "PayObj{" +
                "appid='" + appid + '\'' +
                ", partnerid='" + partnerid + '\'' +
                ", prepayid='" + prepayid + '\'' +
                ", packages='" + packages + '\'' +
                ", nonceStr='" + noncestr + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
