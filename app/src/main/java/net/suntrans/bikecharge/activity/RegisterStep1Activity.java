package net.suntrans.bikecharge.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.MSG;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.EditView;
import net.suntrans.bikecharge.view.LoadingDialog;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/5/26.
 */

public class RegisterStep1Activity extends BasedActivity {

    private EditView number;
    private EditView yanzhenma;
    private LoadingDialog dialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerstep1);
        number = (EditView) findViewById(R.id.mobile);
        yanzhenma = (EditView) findViewById(R.id.yanzhenma);
        dialog = new LoadingDialog(this);
        dialog.setCancelable(false);
        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(RegisterStep1Activity.this)
                        .setMessage("还差一步就完成注册了,是否退出?")
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

    public void getVerify(View view){
        String mobile = number.getText().toString();
        if (!mobile.matches("^(((13[0-9]{1})|(15[0-35-9]{1})|(17[0-9]{1})|(18[0-9]{1}))+\\d{8})$")){
            UiUtils.showToast("手机号码格式不正确");
        }else {
            dialog.setWaitText("发送中..");
            dialog.show();
            RetrofitHelper.getCookieApi().getCode(mobile,"register")
                    .compose(this.<MSG>bindToLifecycle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<MSG>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            UiUtils.showToast("发送失败");
                            dialog.dismiss();
                        }

                        @Override
                        public void onNext(MSG msg) {
                            dialog.dismiss();
                            UiUtils.showToast(msg.getMsg());
                        }
                    });
        }
    }

    private boolean isPressNext =false;
    public void next(View view){
        if (isPressNext){
            UiUtils.showToast("正在验证您的验证码...");
            return;
        }
        final String mobile = number.getText().toString();
        String yanzhenma1 = yanzhenma.getText().toString();
        if (TextUtils.isEmpty(mobile)||TextUtils.isEmpty(yanzhenma1)){
            UiUtils.showToast("手机号或验证码不能为空!");
            return;
        }
        isPressNext =true;
        RetrofitHelper.getCookieApi().register("1",mobile,yanzhenma1,"")
                .compose(this.<MSG>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<MSG>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("服务器错误");
                        isPressNext =false;

                    }

                    @Override
                    public void onNext(MSG msg) {
                        isPressNext =false;
                        LogUtil.i("onnext"+msg.getMsg());
                        if (msg.getStatus().equals("0")){
                            UiUtils.showToast(msg.getMsg());
                        }else {
                            Intent intent = new Intent();
                            intent.putExtra("mobile",mobile);
                            intent.setClass(RegisterStep1Activity.this,RegisterStep2Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(RegisterStep1Activity.this)
                    .setMessage("还差一步就完成注册了,是否退出?")
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
