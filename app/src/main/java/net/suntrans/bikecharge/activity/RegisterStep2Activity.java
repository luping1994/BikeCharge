package net.suntrans.bikecharge.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.LoginResult;
import net.suntrans.bikecharge.bean.MSG;
import net.suntrans.bikecharge.utils.RxBus;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.EditView;
import net.suntrans.bikecharge.view.IosAlertDialog;
import net.suntrans.bikecharge.view.LoadingDialog;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.amap.api.mapcore.util.cz.I;
import static net.suntrans.bikecharge.R.id.login;

/**
 * Created by Looney on 2017/5/26.
 */

public class RegisterStep2Activity extends BasedActivity {

    private EditView number;
    private EditView password;
    private EditView repassword;
    private LoadingDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerstep2);
        number = (EditView) findViewById(R.id.mobile);
        password = (EditView) findViewById(R.id.password);
        repassword = (EditView) findViewById(R.id.repassword);
        dialog = new LoadingDialog(this);
        dialog.setCancelable(false);
        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(RegisterStep2Activity.this)
                        .setMessage("您还未完成注册,是否退出?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton("取消",null)
                        .create().show();
            }
        });
        number.setText(getIntent().getStringExtra("mobile"));

    }


    public void register(View view) {
        final String mobile = number.getText().toString();
        final String password1 = password.getText().toString();
        String repassword1 = repassword.getText().toString();

        if (TextUtils.isEmpty(mobile)) {
            UiUtils.showToast("手机号码不能为空");
            return;
        }
        if (TextUtils.isEmpty(password1)) {
            UiUtils.showToast("密码不能为空");
            return;
        }
        if (TextUtils.isEmpty(repassword1)) {
            UiUtils.showToast("请确认你的密码");
            return;
        }
        if (!repassword1.equals(password1)) {
            UiUtils.showToast("两次密码不一致");
            return;
        }

        dialog.setWaitText("注册中,请稍后。。。");

        dialog.show();
        RetrofitHelper.getCookieApi().register("2", mobile, "", password1)
                .compose(this.<MSG>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MSG>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("服务器错误");
                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(MSG msg) {
                        dialog.dismiss();
                        if (msg == null) {
                            UiUtils.showToast("注册失败");
                            return;
                        }
                        if (msg.getStatus().equals("0")) {
                            UiUtils.showToast(msg.getMsg());
                        } else {
                            UiUtils.showToast(msg.getMsg());
                            String s = "login_" + mobile + "_" + password1;
                            RxBus.getInstance().post(s);
                            finish();
                        }
                    }
                });
    }

    private void login(String username, String password1) {
        dialog.setWaitText("正在为您登录,请稍后...");
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
                                RxBus.getInstance().post("finish");
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(RegisterStep2Activity.this)
                    .setMessage("您还未完成注册,是否退出?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("取消",null)
                    .create().show();
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }
}
