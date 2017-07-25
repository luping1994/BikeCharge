package net.suntrans.bikecharge.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.Constants;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.PayObj;
import net.suntrans.bikecharge.bean.PayResult;
import net.suntrans.bikecharge.bean.UserSetting;
import net.suntrans.bikecharge.fragment.PersonFragment;
import net.suntrans.bikecharge.utils.RxBus;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.EditView;
import net.suntrans.bikecharge.view.IosAlertDialog;
import net.suntrans.bikecharge.view.LoadingDialog;

import org.json.JSONException;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/5/28.
 */

public class VipPayActivity extends BasedActivity {

    private TextView mobile;
    private TextView money;
    private Button button;
    private IWXAPI wxapi;
    private static final String WXPAY_ID = "wxd41679d9bf36e90d";
    private LoadingDialog dialog;
    private Subscription subscribe;
    private boolean isGetYaJin = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_pay);

        wxapi = WXAPIFactory.createWXAPI(this, WXPAY_ID);

        mobile = (TextView) findViewById(R.id.mobile);
        money = (TextView) findViewById(R.id.money);
        button = (Button) findViewById(R.id.pay);
        mobile.setText(App.getSharedPreferences().getString("username", "--"));

        dialog = new LoadingDialog(this);
        dialog.setCancelable(false);

        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        subscribe = RxBus.getInstance().toObserverable(PayResult.class)
                .compose(this.<PayResult>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PayResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(PayResult s) {
                        dialog.dismiss();
                        if (s.errorCode == 0)
                            finish();
                    }
                });
    }

    @Override
    protected void onResume() {
        getPayMin();
        super.onResume();
    }

    public void pay(View view) {
        try {
            payUseWX();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void payUseWX() throws JSONException {
        String moneys = money.getText().toString();

        if (!wxapi.isWXAppInstalled()) {
            UiUtils.showToast("您未安装微信客户端");
            return;
        }
        if (!isGetYaJin|| TextUtils.isEmpty(moneys)){
            UiUtils.showToast("无法获得押金金额,请稍后再试");
            return;
        }
        dialog.setWaitText("正在发起微信支付");
        dialog.show();
        RetrofitHelper.getCookieApi().getVipPayObj(moneys, "入会会费", "1")
                .compose(this.<PayObj>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PayObj>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("获取订单失败");
                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(PayObj payObj) {
                        dialog.dismiss();

                        if (wxapi == null) {
                            wxapi = WXAPIFactory.createWXAPI(VipPayActivity.this.getApplicationContext(), WXPAY_ID);
                        }
                        wxapi.registerApp(Constants.APP_ID);
                        PayReq req = new PayReq();

                        req.appId = payObj.appid;  // 测试用appId
                        req.partnerId = payObj.partnerid;
                        req.prepayId = payObj.prepayid;
                        req.nonceStr = payObj.noncestr;
                        req.timeStamp = payObj.timestamp;
                        req.packageValue = payObj.packages;
                        req.sign = payObj.sign;

                        wxapi.sendReq(req);
                    }
                });

    }

    @Override
    protected void onDestroy() {
        wxapi.detach();
        if (!subscribe.isUnsubscribed()){
            subscribe.unsubscribe();
        }
        super.onDestroy();
    }

    private void getPayMin(){
        RetrofitHelper.getApi().getUserSetting()
                .compose(this.<UserSetting>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserSetting>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(UserSetting set) {
                        if (set.result!=null){
                            if (set.result.cost_min!=null){
                                money.setText(set.result.cost_min);
                                isGetYaJin =true;
                            }
                        }
                    }
                });
    }
}
