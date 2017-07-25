package net.suntrans.bikecharge.fragment;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.trello.rxlifecycle.android.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxFragment;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.activity.AboutActivity;
import net.suntrans.bikecharge.activity.ChargeJianCeActivity;
import net.suntrans.bikecharge.activity.DealActivity;
import net.suntrans.bikecharge.activity.LoginActivity;
import net.suntrans.bikecharge.activity.PerInfoActivity;
import net.suntrans.bikecharge.activity.PushHistoryActivity;
import net.suntrans.bikecharge.activity.ReportListActivity;
import net.suntrans.bikecharge.activity.VipPayActivity;
import net.suntrans.bikecharge.activity.WalletActivity;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.MSG;
import net.suntrans.bikecharge.bean.UserInfo;
import net.suntrans.bikecharge.utils.ActivityUtils;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.RxBus;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.IosAlertDialog;
import net.suntrans.bikecharge.view.LoadingDialog;
import net.suntrans.bikecharge.view.ScrollChildSwipeRefreshLayout;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/5/25.
 */

public class PersonFragment extends RxFragment implements View.OnClickListener {

    private static final String TAG = "PersonFragment";
    private TextView login;
    private TextView username;
    private boolean isLogin;
    private Subscription subscribe;
    private TextView isvip;
    private TextView yuerValue;
    public static final String UPDATE_PERSON_INFO = "updateLoginState";
    private ScrollChildSwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person, container, false);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onResume() {
//        System.out.println("personFragment 的onresume方法执行了");
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        login = (TextView) view.findViewById(R.id.textLogin);
        username = (TextView) view.findViewById(R.id.username);
        isvip = (TextView) view.findViewById(R.id.isVip);
        yuerValue = (TextView) view.findViewById(R.id.yuerValue);
        refreshLayout = (ScrollChildSwipeRefreshLayout) view.findViewById(R.id.refreshlayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserInfo();
            }
        });

        checkLoginState();
        username.setText(App.getSharedPreferences().getString("username", "用户名"));
        view.findViewById(R.id.textLogin).setOnClickListener(this);
//        view.findViewById(R.id.myCharge).setOnClickListener(this);
        view.findViewById(R.id.yuer).setOnClickListener(this);
        view.findViewById(R.id.baogao).setOnClickListener(this);
        view.findViewById(R.id.jiankong).setOnClickListener(this);
        view.findViewById(R.id.setting).setOnClickListener(this);
        view.findViewById(R.id.tuichi).setOnClickListener(this);
//        view.findViewById(R.id.auth).setOnClickListener(this);
        view.findViewById(R.id.header).setOnClickListener(this);
        view.findViewById(R.id.vip).setOnClickListener(this);
        view.findViewById(R.id.message).setOnClickListener(this);
//        view.findViewById(R.id.energy).setOnClickListener(this);

        subscribe = RxBus.getInstance().toObserverable(String.class)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (s.equals(UPDATE_PERSON_INFO))
                            checkLoginState();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });

    }

    private final TagAliasCallback mAliasCallback = new TagAliasCallback() {
        @Override
        public void gotResult(int code, String alias, Set<String> tags) {
            String logs;
            switch (code) {
                case 0:
                    logs = "Set tag and alias success";
                    LogUtil.i(TAG, logs);
                    App.getSharedPreferences().edit().putBoolean("isSetAlias", true).commit();
                    // 建议这里往 SharePreference 里写一个成功设置的状态。成功设置一次后，以后不必再次设置了。
                    break;
                case 6002:
                    logs = "Failed to set alias and tags due to timeout. Try again after 60s.";
                    LogUtil.e(TAG, logs);
                    // 延迟 60 秒来调用 Handler 设置别名
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ALIAS, alias), 1000 * 60);
                    break;
                default:
                    logs = "Failed with errorCode = " + code;
                    LogUtil.e(TAG, logs);
            }
        }
    };

    private static final int MSG_SET_ALIAS = 1001;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SET_ALIAS:
                    Log.d(TAG, "Set alias in handler.");
                    // 调用 JPush 接口来设置别名。
                    JPushInterface.setAliasAndTags(getActivity().getApplicationContext(),
                            (String) msg.obj,
                            null,
                            mAliasCallback);
                    break;
                default:
                    Log.i(TAG, "Unhandled msg - " + msg.what);
            }
        }
    };

    private void bindPush(String username) {
        // 调用 Handler 来异步设置别名
        boolean isSetAlias = App.getSharedPreferences().getBoolean("isSetAlias", false);
        if (!isSetAlias)
            mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_ALIAS, username));

    }

    private void checkLoginState() {
        isLogin = App.getSharedPreferences().getBoolean("isLogin", false);
        if (isLogin) {
            login.setText("已登录");
            getUserInfo();
        } else {
            login.setText("登录 | 注册");
            username.setText("用户名");
            yuerValue.setText("--");
            isvip.setText("--");
        }
    }

    private void getUserInfo() {
        if (!isLogin) {
            refreshLayout.setRefreshing(false);
            return;
        }
        username.setText("--");
        RetrofitHelper.getApi().getUserInfo()
                .compose(this.<UserInfo>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (refreshLayout != null)
                            refreshLayout.setRefreshing(false);
                        UiUtils.showToast("获取用户信息失败请检查你的网络");
                    }

                    @Override
                    public void onNext(UserInfo userInfo) {
                        if (refreshLayout != null)
                            refreshLayout.setRefreshing(false);
                        if (userInfo != null) {
                            LogUtil.i("获取用户信息成功!");
                            String pp = userInfo.result.user.isdeposit;

                            String username = userInfo.result.user.username;
                            String id = userInfo.result.user.id;
//                            System.out.println("id=" + id);
                            if (pp.equals("0")) {
                                PersonFragment.this.isvip.setTextColor(Color.parseColor("#ff0000"));
                                PersonFragment.this.isvip.setText("未缴纳");
                            } else {
                                PersonFragment.this.isvip.setTextColor(Color.parseColor("#0000ff"));
                                PersonFragment.this.isvip.setText("已缴纳");
                            }
                            App.getSharedPreferences().edit().putString("username", username)
                                    .putString("isdeposit", pp)
                                    .putString("id", id)
                                    .commit();
                            PersonFragment.this.username.setText(username);
                            yuerValue.setText(userInfo.result.user.money + "元");
                            yuerValue.setTextColor(getResources().getColor(R.color.colorPrimary));
                            bindPush(username);
                        } else {
                            UiUtils.showToast("获取用户信息失败请检查你的网络");

                        }
                    }
                });
    }


    @Override
    public void onDestroyView() {
        if (subscribe != null) {
            if (!subscribe.isUnsubscribed())
                subscribe.unsubscribe();
        }
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vip:
                if (!isLogin) {
                    UiUtils.showToast("请先登录");
                    ActivityUtils.startLogin(getActivity());
                    return;
                }
                if (canClickVip)
                    checkVip();
                else {
                    UiUtils.showToast("正在确认您的会员信息!");
                }
                break;
            case R.id.header:
                if (!isLogin) {
                    UiUtils.showToast("请先登录");
                    ActivityUtils.startLogin(getActivity());
                    return;
                }
                startActivity(new Intent(getActivity(), PerInfoActivity.class));
                break;
            case R.id.textLogin:
                if (!isLogin) {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.putExtra(
                            LoginActivity.EXTRA_TRANSITION, LoginActivity.TRANSITION_SLIDE_BOTTOM);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions transitionActivity = ActivityOptions.makeSceneTransitionAnimation(getActivity());
                        startActivity(intent, transitionActivity.toBundle());
                    } else {
                        startActivity(intent);
                    }
                } else {
//                    UiUtils.showToast("你已登录");
                }
                break;
            case R.id.myCharge:
                if (!isLogin) {
                    UiUtils.showToast("请先登录");
                    ActivityUtils.startLogin(getActivity());
                    return;
                }
                Intent intent4 = new Intent(getActivity(), DealActivity.class);
                startActivity(intent4);
                break;
            case R.id.yuer:
                if (!isLogin) {
                    ActivityUtils.startLogin(getActivity());
                    UiUtils.showToast("请先登录");
                    return;
                }
                Intent intent = new Intent(getActivity(), WalletActivity.class);
                startActivity(intent);
                break;
            case R.id.baogao:
                if (!isLogin) {
                    UiUtils.showToast("请先登录");
                    ActivityUtils.startLogin(getActivity());
                    return;
                }
                Intent intent2 = new Intent(getActivity(), ReportListActivity.class);
                startActivity(intent2);
                break;
            case R.id.message:
                if (!isLogin) {
                    UiUtils.showToast("请先登录");
                    ActivityUtils.startLogin(getActivity());
                    return;
                }
                Intent a = new Intent(getActivity(), PushHistoryActivity.class);
                startActivity(a);
                break;
            case R.id.jiankong:
                if (!isLogin) {
                    UiUtils.showToast("请先登录");
                    ActivityUtils.startLogin(getActivity());
                    return;
                }
                Intent intent1 = new Intent(getActivity(), ChargeJianCeActivity.class);
                startActivity(intent1);
                break;
//            case R.id.auth:
//                if (!isLogin) {
//                    UiUtils.showToast("请先登录");
//                    ActivityUtils.startLogin(getActivity());
//                    return;
//                }
//                Intent intent8 = new Intent(getActivity(), AuthActivity.class);
//                startActivity(intent8);
//                break;
            case R.id.setting:
                Intent intent3 = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent3);
                break;
//            case R.id.energy:
//                System.out.println("我被点击了哦");
//                Intent intent5 = new Intent(getActivity(), EnergyProfilerActivity.class);
//                startActivity(intent5);
//                break;
            case R.id.tuichi:
                if (isLogin) {
                    new IosAlertDialog(getContext()).builder()
                            .setMsg(getString(R.string.zhuxiao))
                            .setPositiveButton("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    App.getSharedPreferences().edit().clear().commit();
                                    checkLoginState();
                                    UiUtils.showToast(getString(R.string.exit_success));
                                }
                            }).setNegativeButton(getString(R.string.qvxiao), null).show();
                } else {
                    UiUtils.showToast("您还没有登录哦");
                }
                break;
        }
    }


    boolean canClickVip = true;

    private void checkVip() {
        canClickVip = false;
        RetrofitHelper.getApi().getUserInfo()
                .compose(this.<UserInfo>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("无法获取您的会员信息,请稍后再试");
                        canClickVip = true;
                    }

                    @Override
                    public void onNext(UserInfo userInfo) {
                        canClickVip = true;
                        if (userInfo != null) {
                            String isdeposit = userInfo.result.user.isdeposit;
                            String username = userInfo.result.user.username;
                            String id = userInfo.result.user.id;
                            System.out.println("id=" + id);
                            if (isdeposit.equals("0")) {
                                PersonFragment.this.isvip.setTextColor(Color.parseColor("#ff0000"));
                                PersonFragment.this.isvip.setText("未缴纳");
                            } else if (isdeposit.equals("1")){
                                PersonFragment.this.isvip.setTextColor(Color.parseColor("#0000ff"));
                                PersonFragment.this.isvip.setText("已缴纳");
                            }
                            App.getSharedPreferences().edit().putString("username", username)
                                    .putString("isdeposit", isdeposit)
                                    .putString("id", id)
                                    .commit();
                            PersonFragment.this.username.setText(username);
                            yuerValue.setText(userInfo.result.user.money + "元");
                            yuerValue.setTextColor(getResources().getColor(R.color.colorPrimary));
                            if (isdeposit.equals("0")) {
                                new IosAlertDialog(getActivity())
                                        .builder()
                                        .setMsg("您还未加入会员,是否缴纳会员费(可退)?")
                                        .setPositiveButton("确定", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setClass(getActivity(), VipPayActivity.class);
                                                startActivity(intent);
                                            }
                                        }).setNegativeButton("取消", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).show();
                            } else if (isdeposit.equals("1")) {
                                new IosAlertDialog(getActivity())
                                        .builder()
                                        .setMsg("是否退出会员?退出后将不能充电!")
                                        .setPositiveButton("我不退了", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                            }
                                        }).setNegativeButton("继续退款", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showReson();

                                    }
                                }).show();
                            } else {
                                UiUtils.showToast("无法获取您的会员信息,请稍后再试");
                            }

                        } else {
                            UiUtils.showToast("无法获取您的会员信息,请稍后再试");

                        }
                    }
                });

    }

    private void showReson() {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_edit, null, false);
        final EditText editView = (EditText) view.findViewById(R.id.value);
        new AlertDialog.Builder(getContext()).setTitle("请告诉我们退款理由")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String values = editView.getText().toString();
                        reFund();
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    private void reFund() {
        final LoadingDialog dialog = new LoadingDialog(getContext());
        dialog.setWaitText("正在退款,请稍后");
        dialog.show();
        RetrofitHelper.getApi().payRefund()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MSG>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("服务器错误,退款失败");
                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(MSG msg) {
                        dialog.dismiss();
                        UiUtils.showToast(msg.getMsg());
                        if (msg.getStatus().equals("1")) {
                            getUserInfo();
                        } else {
                            UiUtils.showToast("服务器错误,退款失败");
                        }
                    }
                });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            LogUtil.e("可见");
        } else {
            LogUtil.e("不可见");
        }
    }
}
