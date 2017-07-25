package net.suntrans.bikecharge.bean;

import java.util.List;

/**
 * Created by Looney on 2017/5/27.
 */

public class ReportResult {
    public String status;
    public Result result;

    public static class Result {
        public Info info;
        public Cost cost;
        public List<Electricity> electricity;
    }

    public static class Info {
        public String name;
        public String code;

    }

    public static class Cost {
        public String ammeter_id;
        public String created_at;
        public String count_at;
        public String charge_money;
        public String current_cost;
        public String start_electricity;
        public String electricity;
        public String end_type;
        public String times;

    }

    public static class Electricity {
        public String created_at;
        public String t;
        public String data;
    }
}
