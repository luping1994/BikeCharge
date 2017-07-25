package net.suntrans.bikecharge.bean;

import java.util.List;

/**
 * Created by Looney on 2017/6/5.
 */

public class PayRecordResult {
    public String status;
    public List<PayRecord> result;

    public static class PayRecord {
        public String order_sn;
        public String total_amount;
        public String created_at;
        public String pay_code;
    }
}
