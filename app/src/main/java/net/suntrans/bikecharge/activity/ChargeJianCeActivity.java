package net.suntrans.bikecharge.activity;

import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.gelitenight.waveview.library.WaveView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.JanceResult;
import net.suntrans.bikecharge.bean.Message;
import net.suntrans.bikecharge.service.WebScketService;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.RxBus;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.IosAlertDialog;
import net.suntrans.bikecharge.view.LoadingDialog;
import net.suntrans.bikecharge.view.WaveHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static net.suntrans.bikecharge.R.id.txt_msg;

/**
 * Created by Looney on 2017/5/27.
 */

public class ChargeJianCeActivity extends BasedActivity implements OnChartValueSelectedListener {
    LineChart mChart;

    private JanceResult.Ammeter ammeterData;
    private JanceResult.Cost cost;
    private List<JanceResult.Electricity> electricity;
    private WaveHelper helper;
    private Subscription subscribe;
    private String userid;
    private WebScketService.ibinder binder;
    private LoadingDialog dialog;
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
    private TextView startTime;
    private TextView yichongdianliang;
    private TextView xiaofeijiner;
    private TextView shishidianliu;
    private TextView chongrujiner;
    private TextView msg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jiance);
        userid = App.getSharedPreferences().getString("id", "");

        initView();


        Intent intent = new Intent(this, WebScketService.class);
        this.bindService(intent, connection, ContextWrapper.BIND_AUTO_CREATE);


        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initChart();
        timer.schedule(timerTask, 0, 3000);

        subscribe = RxBus.getInstance().toObserverable(Message.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Message>() {
                    @Override
                    public void call(Message message) {
                        dialog.dismiss();
                        LogUtil.i("code=" + message.getStatus_code() + "message=" + message.getMessage());
                        if (message.getStatus_code().equals("200")) {
                            if (message.getMessage().contains("停止充电")) {
                                if (timer != null) {
                                    timer.cancel();
                                    timer = null;
                                }
                                new IosAlertDialog(ChargeJianCeActivity.this).builder()
                                        .setMsg(message.getMessage())
                                        .setCancelable(false)
                                        .setNegativeButton("关闭", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                finish();
                                            }
                                        }).show();
                            }


                        } else {
                            UiUtils.showToast(message.getMessage());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void initView() {
        WaveView waveView = (WaveView) findViewById(R.id.wave);
        waveView.setShapeType(WaveView.ShapeType.CIRCLE);


        waveView.setWaveColor(Color.parseColor("#1ba7da"), getResources().getColor(R.color.colorPrimary));
        dialog = new LoadingDialog(this);
        helper = new WaveHelper(waveView);

        startTime = (TextView) findViewById(R.id.startTime);
        yichongdianliang = (TextView) findViewById(R.id.yichongdianliang);
        xiaofeijiner = (TextView) findViewById(R.id.xiaofeijiner);
        shishidianliu = (TextView) findViewById(R.id.shishidianliu);
        chongrujiner = (TextView) findViewById(R.id.chongrujiner);

        msg = (TextView) findViewById(R.id.txt_msg);
    }


    private void initChart() {
        mChart = (LineChart) findViewById(R.id.chart1);

//        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.setNoDataText("暂无数据...");

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);

        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
//        MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
//        mv.setChartView(mChart); // For bounds control
//        mChart.setMarker(mv); // Set the marker to the chart

        // x-axis limit line
//        LimitLine llXAxis = new LimitLine(10f, "Index 10");
//        llXAxis.setLineWidth(4f);
//        llXAxis.enableDashedLine(10f, 10f, 0f);
//        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//        llXAxis.setTextSize(10f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
//        xAxis.setAxisMaximum(30);
        xAxis.setAxisMinimum(0);
        xAxis.setDrawGridLines(false);
        xAxis.setGridLineWidth(1f);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line


//        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

//        LimitLine ll1 = new LimitLine(150f, "Upper Limit");
//        ll1.setLineWidth(4f);
//        ll1.enableDashedLine(10f, 10f, 0f);
//        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//        ll1.setTextSize(10f);
//        ll1.setTypeface(tf);

//        LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
//        ll2.setLineWidth(4f);
//        ll2.enableDashedLine(10f, 10f, 0f);
//        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//        ll2.setTextSize(10f);
//        ll2.setTypeface(tf);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
//        leftAxis.addLimitLine(ll1);
//        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaximum(1);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setYOffset(0.2f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(true);

        // limit lines are drawn behind data (and not on top)
//        leftAxis.setDrawLimitLinesBehindData(false);

        mChart.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
//        setChartData(30, 100);

//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(500);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        helper.start();
    }

    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            getData();
        }
    };

    @Override
    protected void onPause() {
        helper.cancel();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (connection != null) {
            unbindService(connection);
        }
        if (!subscribe.isUnsubscribed()) {
//            System.out.println("没有解除订阅");
            subscribe.unsubscribe();
        }
        super.onDestroy();
    }

    private void getData() {
        RetrofitHelper.getApi().getChargeCurrent()
                .compose(this.<JanceResult>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JanceResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("获取实时监测信息失败,请检查网络连接");
                    }

                    @Override
                    public void onNext(JanceResult result) {
                        if (result != null) {
                            if (result.status.equals("1")) {
                                ammeterData = result.result.ammeter;
                                cost = result.result.cost;
                                for (int i = 0; i < result.result.electricity.size(); i++) {
                                    LogUtil.i(result.result.electricity.get(i).data);
                                }
                                setChartData(result.result.electricity);
                                setOtherData(cost, result.result.ammeter);
                            } else {
//                                System.out.println(result.msg);
                                if (timer != null) {
                                    timer.cancel();
                                    timer = null;
                                }

                                new IosAlertDialog(ChargeJianCeActivity.this).builder()
                                        .setMsg(result.msg)
                                        .setCancelable(false)
                                        .setNegativeButton("关闭", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                finish();
                                            }
                                        }).show();
                            }
                        } else {
                            new IosAlertDialog(ChargeJianCeActivity.this).builder()
                                    .setMsg("获取实时监测信息失败")
                                    .setCancelable(false)
                                    .setNegativeButton("关闭", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            finish();
                                        }
                                    }).show();
                        }
                    }
                });
    }

    private void setOtherData(JanceResult.Cost cost, JanceResult.Ammeter ammeter) {
        msg.setText("正在充电...");
        if (cost != null) {
            startTime.setText("开始时间:" + cost.created_at);
            yichongdianliang.setText("已充电量:" + cost.electricity + "度");
            xiaofeijiner.setText("消费金额:" + cost.current_cost + "元");
            chongrujiner.setText("充入金额:" + cost.charge_money);
        }
        if (ammeter != null) {
            shishidianliu.setText("实时电流:" + ammeter.I + "A");

        }

    }


    private void setChartData(List<JanceResult.Electricity> datas) {
        if (datas == null)
            return;
        if (datas.size() == 0)
            return;
        ArrayList<Entry> values = new ArrayList<Entry>();
        DecimalFormat format = new DecimalFormat("##0.00000");
        List<Float> floats = new ArrayList<>();
        values.add(new Entry(0, 0));
        for (int i = 0; i < datas.size(); i++) {
            String format1 = format.format(Float.valueOf(datas.get(i).data));
            LogUtil.i(format1);
            floats.add(Float.valueOf(format1));
            values.add(new Entry(i + 1, Float.valueOf(format1)));
        }
        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAxisMaximum(datas.size());
//        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.setAxisMinimum(0);
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1f);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
//        leftAxis.addLimitLine(ll1);
//        leftAxis.addLimitLine(ll2);
        if (floats.size() != 0) {
            Float max = Collections.max(floats);
            leftAxis.setAxisMaximum(max);

        } else {

            leftAxis.setAxisMaximum(0);
        }
        leftAxis.setLabelCount(3);
//        leftAxis.setDrawTopYLabelEntry(true);
        leftAxis.setCenterAxisLabels(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(true);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(false);


        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.clear();
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "电流(A)");

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawValues(false);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
//                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
//                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);

        }
        mChart.invalidate();

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    public void stop(View view) {
        new AlertDialog.Builder(this)
                .setMessage("是否停止充电")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogs, int which) {
                        if (binder != null) {
                            dialog.setWaitText("请稍后...");
                            dialog.show();
                            try {
                                JSONObject object = new JSONObject();
                                object.put("action", "stop_charge");
                                object.put("user_id", userid);
                                binder.sendOrder(object.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).setNegativeButton("取消",null).create().show();


    }


}
