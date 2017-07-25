package net.suntrans.bikecharge.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxFragment;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.activity.CameraScan_Activity;
import net.suntrans.bikecharge.activity.ChargeJianCeActivity;
import net.suntrans.bikecharge.activity.PayActivity;
import net.suntrans.bikecharge.activity.PreChargeActivity;
import net.suntrans.bikecharge.activity.VipPayActivity;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.ContinueChargeEntity;
import net.suntrans.bikecharge.bean.JanceResult;
import net.suntrans.bikecharge.bean.QRcodeInfo;
import net.suntrans.bikecharge.bean.SceneResult;
import net.suntrans.bikecharge.utils.ActivityUtils;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.IosAlertDialog;
import net.suntrans.bikecharge.view.LoadingDialog;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.R.attr.data;
import static net.suntrans.bikecharge.R.id.cost;


/**
 * Created by Looney on 2017/5/25.
 */

public class ChargeFragment extends RxFragment implements View.OnClickListener {
    private static final String TAG = "ChargeFragment";
    private static final String BASEURL = "http://ebike.suntrans-cloud.com/ebike.php/charge/qrcode/";

    private LoadingDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charge, container, false);
        setHasOptionsMenu(true);
        initView(view);
        return view;
    }

    private void initView(View view) {
        view.findViewById(R.id.scan).setOnClickListener(this);
        view.findViewById(R.id.jiankong).setOnClickListener(this);
        view.findViewById(R.id.reconnected).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                startScan();
                break;
            case R.id.jiankong:
                if (!App.getSharedPreferences().getBoolean("isLogin", false)) {
                    ActivityUtils.startLogin(getActivity());
                    UiUtils.showToast("请先登录");
                    return;
                }
                getData();
                break;
            case R.id.reconnected:
                reConnected();
                break;
        }
    }

    boolean canContinue = true;
    private void reConnected() {
        if (!canContinue){
            UiUtils.showToast("请求服务器中,请稍后");
            return;
        }
        canContinue = false;
        RetrofitHelper.getApi().continueCharge()
                .compose(this.<ContinueChargeEntity>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ContinueChargeEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        canContinue = true;
                        e.printStackTrace();
                        if (e instanceof HttpException){
                            UiUtils.showToast("网络连接异常");
                        }else {
                            UiUtils.showToast("请求服务器失败");
                        }
                    }

                    @Override
                    public void onNext(ContinueChargeEntity data) {
                            if (data.status.equals("1")){
                                handleScanResult(data.url);
                            }else{
                                canContinue =true;
                                new IosAlertDialog(getContext()).builder().setMsg(data.msg)
                                        .setNegativeButton("我知道了",null).show();

                            }
                    }
                });
    }

    private void startScan() {
        if (!App.getSharedPreferences().getBoolean("isLogin", false)) {
            ActivityUtils.startLogin(getActivity());
            UiUtils.showToast("请先登录!");
            return;
        }
        checkPermission();

    }


    public void checkPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        HiPermission.create(getActivity())
                .checkSinglePermission(Manifest.permission.CAMERA, new PermissionCallback() {
                    @Override
                    public void onClose() {

                    }

                    @Override
                    public void onFinish() {

                    }

                    @Override
                    public void onDeny(String permisson, int position) {
                        UiUtils.showToast("您已拒绝开启摄像头,无法开始扫描");
                    }

                    @Override
                    public void onGuarantee(String permisson, int position) {
                        System.out.println("权限允许");
                        IntentIntegrator.forSupportFragment(ChargeFragment.this)
                                .setOrientationLocked(false)
                                .setBeepEnabled(false)
                                .setCaptureActivity(CameraScan_Activity.class) // 设置自定义的activity是CustomActivity
                                .initiateScan(); // 初始化扫描
                    }
                });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 300) {

        } else {
//            System.out.println("请求码=" + resultCode);
            IntentResult result1 = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result1 != null) {
                if (result1.getContents() == null) {
//                    UiUtils.showToast("无效的二维码");
                    return;
                } else {
                    String result = result1.getContents();
                    LogUtil.i("收到的结果为:" + result);
                    handleScanResult(result);

                }
            } else {
                UiUtils.showToast("扫描二维码失败!");
            }
        }


    }

    public static final String TEN_SWITCH = "4100";
    public static final String SIX_SWITCH = "4300";

    private void handleScanResult(String result) {
        canContinue = true;
        if (result == null) {
            return;
        }
        if (!result.contains("/charge/qrcode/id/")) {
            UiUtils.showToast("无法识别的二维码");
            return;
        }
        if (dialog == null)
            dialog = new LoadingDialog(getActivity());
        dialog.setWaitText("请稍后...");
        dialog.show();
        RetrofitHelper.getApi()
                .getQRcodeInfo(result.substring(RetrofitHelper.BASE_URL.length(), result.length()))
                .compose(this.<QRcodeInfo>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<QRcodeInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("服务器错误!");
                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(QRcodeInfo result) {
                        dialog.dismiss();
                        if (result.status.equals("1")) {
                            String qrcode = result.result.qrcode;
                            handleQRcode(qrcode);
                        } else {
                            if (result.code == 201) {
                                new IosAlertDialog(getActivity())
                                        .builder()
                                        .setMsg("充电之前需加入会员,是否马上加入?")
                                        .setPositiveButton("立刻加入", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setClass(getActivity(), VipPayActivity.class);
                                                startActivity(intent);
                                            }
                                        }).setNegativeButton("我再看看", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                            } else if (result.code == 202) {
                                new IosAlertDialog(getActivity())
                                        .builder()
                                        .setMsg("余额不足,请充值")
                                        .setPositiveButton("去充值", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setClass(getActivity(), PayActivity.class);
                                                startActivity(intent);
                                            }
                                        }).setNegativeButton("不了", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                            } else if (result.code == 404) {
                                UiUtils.showToast(result.msg);
                            }
                        }
                    }
                });
    }

    private void handleQRcode(String result) {
        String[] split = result.split("_");
        if (split == null) {
            UiUtils.showToast("无法识别的二维码");
            return;
        }

        if (split.length != 3) {
            UiUtils.showToast("无法识别的二维码");
            return;
        }
        final String type = split[0];
        final String addr = split[1];
        final String channel = split[2];


        String s = "";
        if (type.equals(SIX_SWITCH)) {
            s = "此插座为汽车充电插座,是否继续?";
        } else if (type.equals(TEN_SWITCH)) {
            s = "此插座为电动自行车充电插座,是否继续?";
        }
        new IosAlertDialog(getActivity()).builder()
                .setCancelable(false).setTitle("温馨提示").setMsg(s)
                .setPositiveButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), PreChargeActivity.class);
                        intent.putExtra("type", type.equals("4300") ? SIX_SWITCH : TEN_SWITCH);
                        intent.putExtra("switch_addr", addr);
                        intent.putExtra("channel_number", channel);
                        startActivity(intent);
                    }
                }).setNegativeButton("取消", null).show();
    }


    boolean canOpenJianCe = true;

    private void getData() {
        if (!canOpenJianCe) {
            UiUtils.showToast("准备中,请稍后...");
            return;
        }
        canOpenJianCe = false;
        RetrofitHelper.getApi().getChargeCurrent()
                .compose(this.<JanceResult>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JanceResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        canOpenJianCe = true;

                        UiUtils.showToast("获取实时监测信息失败,请检查网络连接");
                    }

                    @Override
                    public void onNext(JanceResult result) {
                        canOpenJianCe = true;
                        if (result != null) {
                            if (result.status.equals("1")) {

                                Intent intent = new Intent(getActivity(), ChargeJianCeActivity.class);
                                startActivity(intent);
                            } else {

                                new IosAlertDialog(getContext()).builder().setMsg(result.msg)
                                        .setNegativeButton("我知道了",null).show();
                            }
                        } else {
                            UiUtils.showToast(result.msg);
                        }
                    }
                });
    }




}
