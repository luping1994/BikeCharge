package net.suntrans.bikecharge.bean;

/**
 * Created by Looney on 2017/6/3.
 */

public class PayResult {
    public PayResult(String msg,int code) {
        this.msg = msg;
        this.errorCode = code;
    }

    public String msg;
    public int errorCode;
}
