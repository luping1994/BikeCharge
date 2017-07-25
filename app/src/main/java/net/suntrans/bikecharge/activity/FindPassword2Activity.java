package net.suntrans.bikecharge.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.MSG;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.EditView;
import net.suntrans.bikecharge.view.IosAlertDialog;
import net.suntrans.bikecharge.view.LoadingDialog;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/5/26.
 */

public class FindPassword2Activity extends BasedActivity {

    private TextView number;
    private EditView password;
    private EditView repassword;
    private LoadingDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpassstep2);
        number = (TextView) findViewById(R.id.mobile);
        password = (EditView) findViewById(R.id.password);
        repassword = (EditView) findViewById(R.id.repassword);
        dialog = new LoadingDialog(this);
        dialog.setCancelable(false);
        number.setText(getIntent().getStringExtra("mobile"));

        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(FindPassword2Activity.this)
                        .setMessage("您还未完成操作,是否退出?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton("取消",null)
                        .create().show();
            }
        });
    }


    public void register(View view) {
        String mobile = number.getText().toString();
        String password1 = password.getText().toString();
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

        dialog.setWaitText("正在提交,请稍后。。。");
        dialog.show();
        RetrofitHelper.getCookieApi().forget2("2", mobile, password1)
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
                        UiUtils.showToast("失败了");
                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(MSG msg) {
                        dialog.dismiss();
                        if (msg.getStatus().equals("0")) {
                            UiUtils.showToast(msg.getMsg());
                        } else {
                            new IosAlertDialog(FindPassword2Activity.this).builder()
                                    .setCancelable(false)
                                    .setMsg(msg.getMsg())
                                    .setPositiveButton("去登录", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            finish();
                                        }
                                    }).setNegativeButton("关闭", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();

                                }
                            }).show();
                        }
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(FindPassword2Activity.this)
                    .setMessage("您还未完成操作,是否退出?")
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
