package net.suntrans.bikecharge.activity;

import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bigkoo.pickerview.OptionsPickerView;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.Message;
import net.suntrans.bikecharge.bean.UserInfo;
import net.suntrans.bikecharge.fragment.ChargeFragment;
import net.suntrans.bikecharge.fragment.PersonFragment;
import net.suntrans.bikecharge.service.WebScketService;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.RxBus;
import net.suntrans.bikecharge.utils.UiUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.R.attr.id;
import static android.R.attr.mode;
import static android.R.attr.value;
import static net.suntrans.bikecharge.R.id.yuerValue;

/**
 * Created by Looney on 2017/5/25.
 */

public class PreChargeActivity extends BasedActivity {
    List<String> money;
    private TextView jiner;
    private String type;
    private String userid;
    private WebScketService.ibinder binder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (WebScketService.ibinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i("绑定失败");
        }
    };
    private String channel_number;
    private String switch_addr;
    private Subscription subscribe;
    private Observable<UserInfo> userInfoObservable;
    private String money1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_charge);
        init();
        Intent intent = new Intent(this, WebScketService.class);
        this.bindService(intent, connection, ContextWrapper.BIND_AUTO_CREATE);

        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        type = getIntent().getStringExtra("type");
        channel_number = getIntent().getStringExtra("channel_number");
        switch_addr = getIntent().getStringExtra("switch_addr");
        userid = App.getSharedPreferences().getString("id", "");
        subscribe = RxBus.getInstance().toObserverable(Message.class)
                .compose(this.<Message>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Message>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Message message) {
                        if (message.getStatus_code().equals("200")) {
                            if (message.getMessage().contains("开始充电")) {
                                LogUtil.e("启动activity");
                                UiUtils.showToast(message.getMessage());
                                startActivity(new Intent(PreChargeActivity.this, ChargeJianCeActivity.class));
                                finish();
                            }

                        } else {
                            UiUtils.showToast(message.getMessage());
                        }
                    }
                });
    }

    Handler handler = new Handler();

    private void init() {
        jiner = (TextView) findViewById(R.id.jiner);

        money = new ArrayList<>();
        if (getIntent().getStringExtra("type").equals(ChargeFragment.TEN_SWITCH)) {
            money.add("1");
            money.add("2");
            money.add("3");
            money.add("4");
            money.add("5");
            money.add("6");
            money.add("7");
            money.add("8");
            money.add("9");
            money.add("10");
        } else {
            money.add("5");
            money.add("10");
            money.add("20");
            money.add("30");
            money.add("40");
            money.add("50");
            money.add("60");
            money.add("70");
            money.add("80");
            money.add("90");
            money.add("100");
        }
    }

    public void chooseMoney(View view) {
        OptionsPickerView pickerView1 = null;
        if (pickerView1 == null) {
            pickerView1 = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int options2, int options3, View v) {
                    jiner.setText(money.get(options1));
                }
            })
                    .setTitleText("选择金额(元)")
                    .build();
            pickerView1.setPicker(money);
        }
        pickerView1.show();
    }

    public void startCharge(View view) {
        LogUtil.i("id=" + userid);
        boolean isLogin = App.getSharedPreferences().getBoolean("isLogin", false);
        if (!isLogin) {
            UiUtils.showToast("您还未登录!请先登录");
            return;
        }
        money1 = jiner.getText().toString();
        if (TextUtils.isEmpty(money1)) {
            UiUtils.showToast("请选择充电金额");
            return;
        }
        getUserInfo();
    }

    private void startChargeByOrder(String money) {
        if (binder != null) {
            try {
                JSONObject jsonObject = new JSONObject();
                if (type.equals(ChargeFragment.SIX_SWITCH)) {
                    jsonObject.put("device", "slc6");
                } else {
                    jsonObject.put("device", "slc10");
                }
                jsonObject.put("action", "start_charge");
                jsonObject.put("user_id", Integer.valueOf(userid));
                jsonObject.put("switch_addr", switch_addr);
                jsonObject.put("channel_number", Integer.valueOf(channel_number));
                jsonObject.put("cost", money);

                if (!binder.sendOrder(jsonObject.toString())) {
                    UiUtils.showToast("无法连接服务器,请10秒后再试");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onDestroy() {
        if (connection != null) {
            unbindService(connection);
        }
        if (!subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    boolean canStart = true;

    private void getUserInfo() {
        if (!canStart) {
            UiUtils.showToast("连接服务器中,请稍后...");
            return;
        }
        canStart = false;
        if (userInfoObservable == null) {
            userInfoObservable = RetrofitHelper.getApi().getUserInfo()
                    .compose(this.<UserInfo>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        userInfoObservable.subscribe(new Subscriber<UserInfo>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                canStart = true;
                if (e instanceof HttpException) {
                    UiUtils.showToast("网络连接失败!");

                } else {
                    UiUtils.showToast("无法获取您的账户余额,请检查网络连接");
                }
            }

            @Override
            public void onNext(UserInfo userInfo) {
                canStart = true;
                if (userInfo != null) {
                    LogUtil.i("获取用户信息成功!");
                    float money = Float.parseFloat(userInfo.result.user.money);
                    if (money < Float.valueOf(money1)) {
                        UiUtils.showToast("当前账户余额不足" + money1 + "元");
                    } else {
                        startChargeByOrder(money1);
                    }

                } else {
                    UiUtils.showToast("获取用户信息失败请检查你的网络");

                }
            }
        });
    }
}
