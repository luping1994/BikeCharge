package net.suntrans.bikecharge.wxapi;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.Constants;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.activity.BasedActivity;
import net.suntrans.bikecharge.bean.PayResult;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.RxBus;
import net.suntrans.bikecharge.utils.UiUtils;

import rx.Subscriber;


public class WXPayEntryActivity extends BasedActivity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private IWXAPI api;
    private TextView tips;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        tips = (TextView) findViewById(R.id.tips);
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID);
        api.handleIntent(getIntent(), this);
        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              finish();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        LogUtil.i(TAG, "onPayFinish, errCode = " + resp.errCode);

        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {


            if (resp.errCode == 0) {
//                Intent intent = new Intent();
//                intent.setClass(this, PaySuccessActivity.class);
//                intent.putExtra("result", "支付成功!");
//                startActivity(intent);
                RxBus.getInstance().post(new PayResult("支付成功", 0));
                tips.setText("支付成功!");
                RxBus.getInstance().post("updateLoginState");
            } else if (resp.errCode == -2) {
                RxBus.getInstance().post(new PayResult("您已取消支付", -2));
                tips.setText("您已取消支付!");
            } else if (resp.errCode == -1) {
                RxBus.getInstance().post(new PayResult("支付失败,服务器错误", -1));
                tips.setText("支付失败,服务器错误!");

            }
//            finish();
        }

    }
}