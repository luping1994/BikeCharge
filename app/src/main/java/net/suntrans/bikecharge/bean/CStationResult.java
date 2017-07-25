package net.suntrans.bikecharge.bean;

import java.util.List;

/**
 * Created by Looney on 2017/6/8.
 */

public class CStationResult {
    public String status;

    public Result result;

    public static class Result {
       public List<CStation> sites;
    }

    public static class CStation {
        public String id;
        public String name;
        public String code;
        //        public String created_at;
//        public String updated_at;
//        public String sort_order;
//        public String status;
        public String latitude;
        public String longitude;
        public String sensus_id;
        public String address;
    }

}
