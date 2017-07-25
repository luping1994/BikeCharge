package net.suntrans.bikecharge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.UserInfo;
import net.suntrans.bikecharge.fragment.PersonFragment;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.UiUtils;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static net.suntrans.bikecharge.R.id.username;

/**
 * Created by Looney on 2017/5/27.
 */

public class WalletActivity extends BasedActivity {

    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        initView();
    }

    private void initView() {
        textView = (TextView) findViewById(R.id.money);
        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(WalletActivity.this,PayRecordActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {
       getUserInfo();
        super.onResume();
    }

    private void getUserInfo() {
        RetrofitHelper.getApi().getUserInfo()
                .compose(this.<UserInfo>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("获取用户信息失败请检查你的网络");
                    }

                    @Override
                    public void onNext(UserInfo userInfo) {
                        if (userInfo != null) {
                            LogUtil.i("获取用户信息成功!");
                            String username = userInfo.result.user.username;
                            String id = userInfo.result.user.id;
//                            System.out.println("id="+id);
                            App.getSharedPreferences().edit().putString("username", username)
                                    .putString("id", id)
                                    .commit();
                            String money = userInfo.result.user.money;
                            textView.setText(money+"元");
                        } else {
                            UiUtils.showToast("获取用户信息失败请检查你的网络");

                        }
                    }
                });
    }

    public void chongzhi(View view){
        startActivity(new Intent(this,PayActivity.class));
    }

}
