package net.suntrans.bikecharge.bean;

import java.util.List;

/**
 * Created by Looney on 2017/5/26.
 */

public class ReportListResult {
    public String status;
    public Result result;

    public static class Result {
        public List<Cost> cost;
    }

    public static class Cost {
        public String id;
        public String ammeter_id;
        public String created_at;
        public String count_at;
        public String charge_money;
        public String current_cost;
        public String electricity;
    }
}
