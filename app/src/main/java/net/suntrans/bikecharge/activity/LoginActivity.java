package net.suntrans.bikecharge.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;

import com.autonavi.amap.mapcore.interfaces.IUiSettings;
import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.LoginResult;
import net.suntrans.bikecharge.bean.MSG;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.RxBus;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.EditView;
import net.suntrans.bikecharge.view.LoadingDialog;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static net.suntrans.bikecharge.R.id.mobile;

/**
 * Created by Looney on 2017/5/26.
 */

public class LoginActivity extends BasedActivity implements View.OnClickListener {
    public static final String TRANSITION_SLIDE_BOTTOM = "SLIDE_BOTTOM";
    public static final String EXTRA_TRANSITION = "EXTRA_TRANSITION";
    private EditView mobile;
    private EditView password;
    private LoadingDialog dialog;
    private Subscription subscribe;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTransition();
        setContentView(R.layout.activity_login);
        init();
    }

    private void applyTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String transition = getIntent().getStringExtra(EXTRA_TRANSITION);
            switch (transition) {
                case TRANSITION_SLIDE_BOTTOM:
                    Transition transitionSlideBottom =
                            TransitionInflater.from(this).inflateTransition(R.transition.slide_bottom);
                    getWindow().setEnterTransition(transitionSlideBottom);
                    break;
            }
        }
    }

    private void init() {
        mobile = (EditView) findViewById(R.id.mobile);
        password = (EditView) findViewById(R.id.password);
        dialog = new LoadingDialog(this);
        dialog.setCancelable(false);
        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.wangjimima).setOnClickListener(this);
        findViewById(R.id.zhuce).setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);

        subscribe = RxBus.getInstance().toObserverable(String.class)
                .compose(this.<String>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        if (s.equals("finish")) {
                            finish();
                        } else if (s.contains("login")) {
                            String[] split = s.split("_");
                            login(split[1], split[2]);
                        }
                    }
                });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                supportFinishAfterTransition();
                break;
            case R.id.wangjimima:
                Intent intent1 = new Intent();
                intent1.setClass(this, FindPassword1Activity.class);
                startActivity(intent1);
                break;
            case R.id.zhuce:
                Intent intent = new Intent();
                intent.setClass(this, RegisterStep1Activity.class);
                startActivity(intent);
                break;
            case R.id.login:
                String username = "";
                String password1 = "";
                username = mobile.getText().toString();
                password1 = password.getText().toString();
                if (!username.matches("^(((13[0-9]{1})|(15[0-35-9]{1})|(17[0-9]{1})|(18[0-9]{1}))+\\d{8})$")){
                    UiUtils.showToast("手机号码格式不正确");
                    break;
                }
                if (TextUtils.isEmpty(password1)) {
                    UiUtils.showToast("密码不能为空");
                    break;
                }
                login(username,password1);
                break;
        }
    }

    private void login(String username,String password1) {

        dialog.setWaitText("登陆中,请稍后");
        dialog.show();
        RetrofitHelper.getLoginApi().login("password", "1", "559eb687a4fcafdabe991c320172fcc9", username, password1)
                .compose(this.<LoginResult>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<LoginResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("登录失败!");
                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(LoginResult msg) {
                        dialog.dismiss();

                        if (msg != null) {
                            String access_token = msg.access_token;
                            String expires_in = msg.expires_in;
                            if (msg.error == null) {
                                App.getSharedPreferences()
                                        .edit()
                                        .putString("access_token", access_token)
                                        .putLong("expires_in", Long.valueOf(expires_in))
                                        .putBoolean("isLogin", true)
                                        .putLong("fristLoginTime", System.currentTimeMillis() / 1000)
                                        .commit();
                                UiUtils.showToast("登录成功!");
                                RxBus.getInstance().post("updateLoginState");
                                finish();
                            } else {
                                UiUtils.showToast(msg.error_description);
                            }

                        } else {
                            UiUtils.showToast("登录失败");
                        }
                    }
                });

    }

    @Override
    protected void onDestroy() {
        if (!subscribe.isUnsubscribed()){
            subscribe.unsubscribe();
        }
        super.onDestroy();
    }
}
