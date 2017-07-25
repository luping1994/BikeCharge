package net.suntrans.bikecharge.bean;

/**
 * Created by Looney on 2017/6/8.
 */

public class UserSetting {
    public String status;
    public Result result;
    public static class Result {
        public String cost_min;
        public String cost_guarantee;
    }
}
