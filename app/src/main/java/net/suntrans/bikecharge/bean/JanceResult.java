package net.suntrans.bikecharge.bean;

import java.util.List;

/**
 * Created by Looney on 2017/5/27.
 */

public class JanceResult {
    public String status;
    public Result result;

    public String msg;
    public String uid;

    public static class Result {
        public Ammeter ammeter;
        public Cost cost;
        public List<Electricity> electricity;
    }

    public static class Ammeter {
        public String updated_at;
        public String U;
        public String I;
        public String Power;
        public String PowerRate;
        public String EletricityValue;

    }

    public static class Cost {
        public String ammeter_id;
        public String created_at;
        public String count_at;
        public String charge_money;
        public String current_cost;
        public String start_electricity;
        public String electricity;
        public String times;

    }

    public static class Electricity {
        public String created_at;
        public String t;
        public String data;
    }
}
