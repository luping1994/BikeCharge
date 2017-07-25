package net.suntrans.bikecharge.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.MSG;
import net.suntrans.bikecharge.bean.UserInfo;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.LoadingDialog;
import net.suntrans.bikecharge.view.ScrollChildSwipeRefreshLayout;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static net.suntrans.bikecharge.R.id.refreshlayout;

/**
 * Created by Looney on 2017/6/3.
 */

public class PerInfoActivity extends BasedActivity implements View.OnClickListener {

    private TextView nikeName;
    private TextView sex;
    private TextView job;
    private TextView birthday;
    private TextView city;
    private TextView bike;
    private ScrollChildSwipeRefreshLayout refreshLayout;


    private int mYear;
    private int mMonth;
    private int mDay;


    private final  String[] sexValue = new String[]{"男","女"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_perinfo);

        nikeName = (TextView) findViewById(R.id.nickName);
        sex = (TextView) findViewById(R.id.sex);
        job = (TextView) findViewById(R.id.job);
        birthday = (TextView) findViewById(R.id.birthday);
        city = (TextView) findViewById(R.id.city);
        bike = (TextView) findViewById(R.id.bike);


        findViewById(R.id.nikeNameRl).setOnClickListener(this);
        findViewById(R.id.sexRl).setOnClickListener(this);
        findViewById(R.id.jobRl).setOnClickListener(this);
        findViewById(R.id.birthdayRl).setOnClickListener(this);
        findViewById(R.id.cityRl).setOnClickListener(this);
        findViewById(R.id.bikeRl).setOnClickListener(this);


        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        refreshLayout = (ScrollChildSwipeRefreshLayout) findViewById(refreshlayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserInfo();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
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
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(UserInfo userInfo) {
                        refreshLayout.setRefreshing(false);

                        if (userInfo != null) {
                            LogUtil.i("获取用户信息成功!");
                            refreshView(userInfo);

                        } else {
                            UiUtils.showToast("获取用户信息失败请检查你的网络");

                        }
                    }
                });
    }

    private void refreshView(UserInfo userInfo) {
        nikeName.setText(TextUtils.isEmpty(userInfo.result.user.nickname) ? "未设置" : userInfo.result.user.nickname);
        job.setText(TextUtils.isEmpty(userInfo.result.user.jobs) ? "未设置" : userInfo.result.user.jobs);
        bike.setText(TextUtils.isEmpty(userInfo.result.user.ebike) ? "未设置" : userInfo.result.user.ebike);
        city.setText(TextUtils.isEmpty(userInfo.result.user.city) ? "未设置" : userInfo.result.user.city);
        birthday.setText(TextUtils.isEmpty(userInfo.result.user.birthday) ? "未设置" : userInfo.result.user.birthday);
        sex.setText(userInfo.result.user.sex.equals("1") ? "男" : userInfo.result.user.sex.equals("2") ? "女" : "未设置");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nikeNameRl:
            case R.id.jobRl:
            case R.id.cityRl:
            case R.id.bikeRl:
                showModifyDialog(v.getId());
                break;
            case R.id.birthdayRl:
                DatePickerDialog pickerDialog = new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
                pickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                pickerDialog.show();

                break;
            case  R.id.sexRl:
                new AlertDialog.Builder(this)
                        .setTitle("选择性别")
                        .setSingleChoiceItems(sexValue, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int sex1 = which+1;
                                modify("sex",sex1+"");
                                dialog.dismiss();
                            }
                        }).show();
                break;
        }
    }

    private void showModifyDialog(int id) {
        String s = "";
        String p = "";
        switch (id) {
            case R.id.nikeNameRl:
                s = "修改昵称";
                p = "nickname";
                break;
            case R.id.jobRl:
                s = "修改职业";
                p = "jobs";
                break;
            case R.id.cityRl:
                s = "修改城市";
                p = "city";

                break;
            case R.id.bikeRl:
                s = "修改电动车型号";
                p = "ebike";
                break;
        }
//        System.out.println(s);
        View view = LayoutInflater.from(this).inflate(R.layout.item_edit, null, false);
        final EditText editView = (EditText) view.findViewById(R.id.value);
        final String finalP = p;
        new AlertDialog.Builder(this).setTitle(s)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String values = editView.getText().toString();
                        if (TextUtils.isEmpty(values)) {
                            return;
                        }
                        modify(finalP, values);
                    }
                }).setNegativeButton("取消", null).create().show();
    }

    private void modify(String finalP, String values) {
        final LoadingDialog dialog = new LoadingDialog(this);
        dialog.setWaitText("修改中,请稍后");
        dialog.show();
        Map<String, String> map = new HashMap<>();
        map.put(finalP, values);
        RetrofitHelper.getApi().upDateInfo(map)
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
                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(MSG msg) {
                        dialog.dismiss();

                        UiUtils.showToast(msg.getMsg());
                        if (msg.getStatus().equals("1"))
                            getUserInfo();
                    }
                });
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    String s  = new StringBuilder()
                            .append(mYear).append("-")
                            .append(pad(mMonth + 1)).append("-")
                            .append(pad(mDay)).toString();
                    modify("birthday",s);

                }
            };


    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

}
