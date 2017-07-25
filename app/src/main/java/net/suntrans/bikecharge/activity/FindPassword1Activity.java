package net.suntrans.bikecharge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

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

public class FindPassword1Activity extends BasedActivity {

    private EditView number;
    private EditView yanzhenma;
    private LoadingDialog dialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findpassword1);
        number = (EditView) findViewById(R.id.mobile);
        yanzhenma = (EditView) findViewById(R.id.yanzhenma);
        dialog = new LoadingDialog(this);
        dialog.setCancelable(false);

        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
            RetrofitHelper.getCookieApi().getCode(mobile,"forgot")
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

    public void next(View view){
        final String mobile = number.getText().toString();
        String yanzhenma1 = yanzhenma.getText().toString();
        if (TextUtils.isEmpty(mobile)||TextUtils.isEmpty(yanzhenma1)){
            UiUtils.showToast("手机号或验证码不能为空!");
            return;
        }
        yanzhenma1 = yanzhenma1.replace(" ","");
        RetrofitHelper.getCookieApi().forget1(yanzhenma1,mobile,"1")
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
                        UiUtils.showToast("验证失败");
                    }

                    @Override
                    public void onNext(MSG msg) {
                        LogUtil.i("onnext"+msg.getMsg());
                        if (msg.getStatus().equals("0")){
                            UiUtils.showToast(msg.getMsg());
                        }else {
                            Intent intent = new Intent();
                            intent.putExtra("mobile",mobile);
                            intent.setClass(FindPassword1Activity.this,FindPassword2Activity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

    }
}
