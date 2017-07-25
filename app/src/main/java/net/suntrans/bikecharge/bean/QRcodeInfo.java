package net.suntrans.bikecharge.bean;

/**
 * Created by Looney on 2017/6/8.
 */

public class QRcodeInfo {
    public String status;
    public String msg;
    public Result result;
    public int code;

    public static class Result{
        public String qrcode;
    }
}
