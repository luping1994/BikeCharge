package net.suntrans.bikecharge.bean;

/**
 * Created by Looney on 2017/6/8.
 */

public class SceneResult {
    public String status;
    public String msg;
    public Result result;


    public static class Result {
        public Sensus sensus;
    }
    public static class Sensus{
        public String id;
        public String created_at;
        public String dev_id;
        public String state;
        public String pm1;
        public String pm10;
        public String pm25;
        public String jiaquan;
        public String yanwu;
        public String wendu;
        public String shidu;
        public String renyuan;
    }
}
