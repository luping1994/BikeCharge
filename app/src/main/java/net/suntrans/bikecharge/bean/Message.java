package net.suntrans.bikecharge.bean;

/**
 * Created by Looney on 2017/5/26.
 */

public class Message {
    private String status_code;
    private String message;

    public String getStatus_code() {
        return status_code;
    }

    public void setStatus_code(String status_code) {
        this.status_code = status_code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(String status_code, String message) {
        this.status_code = status_code;
        this.message = message;
    }
}
