package net.suntrans.bikecharge.bean;

/**
 * Created by Looney on 2017/5/26.
 */

public class UserInfo {
    public String status;
    public Result result;

    public static class Result {
        public User user;
    }

    public static class User {
        public String id;
        public String username;
        public String nickname;
        public String money;
        public String sex;
        public String jobs;
        public String birthday;
        public String ebike;
        public String city;
        public String isauth;
        public String isdeposit;
    }
}
