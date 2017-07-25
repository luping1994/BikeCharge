package net.suntrans.bikecharge.bean;

import java.util.List;

/**
 * Created by Looney on 2017/5/26.
 */

public class PushHisResult {
    public String status;
    public Result result;

    public static class Result {
        public List<History> history;
    }

    public static class History {
        public String id;
        public String created_at;
        public String end_type;
        public String message;

    }
}
