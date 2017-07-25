package net.suntrans.bikecharge.activity;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

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

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.ReportResult;
import net.suntrans.bikecharge.utils.LogUtil;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.ScrollChildSwipeRefreshLayout;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/5/27.
 */

public class ReportDetailActivity extends BasedActivity implements OnChartValueSelectedListener {

    private TextView bianhao;
    private TextView startTime;
    private TextView endTime;
    private TextView endType;
    private TextView meterStartDegrees;
    private TextView money;
    private TextView consumeDegrees;
    private TextView consumeMoney;
    private ScrollChildSwipeRefreshLayout refreshLayout;

    LineChart mChart;
    private String report_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        init();
    }

    private void init() {


        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        report_id = getIntent().getStringExtra("report_id");
        bianhao = (TextView) findViewById(R.id.bianhao);
        startTime = (TextView) findViewById(R.id.startTime);
        endTime = (TextView) findViewById(R.id.endTime);
        endType = (TextView) findViewById(R.id.endType);
        meterStartDegrees = (TextView) findViewById(R.id.meterStartDegrees);
        money = (TextView) findViewById(R.id.money);
        consumeDegrees = (TextView) findViewById(R.id.consumeDegrees);
        consumeMoney = (TextView) findViewById(R.id.consumeMoney);
        refreshLayout = (ScrollChildSwipeRefreshLayout) findViewById(R.id.refreshlayout);
        mChart = (LineChart) findViewById(R.id.chart1);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
        initChart();

    }

    private void initChart() {
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

//        XAxis xAxis = mChart.getXAxis();
//        xAxis.enableGridDashedLine(10f, 10f, 0f);
//        xAxis.setAxisMaximum(30);
//        xAxis.setAxisMinimum(0);
//        xAxis.setDrawGridLines(false);
//        xAxis.setGridLineWidth(1f);
//        xAxis.setGranularity(1f);
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

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

//        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
//        leftAxis.addLimitLine(ll1);
//        leftAxis.addLimitLine(ll2);
//        leftAxis.setAxisMaximum(200f);
//        leftAxis.setAxisMinimum(0f);
        //leftAxis.setYOffset(20f);
//        leftAxis.enableGridDashedLine(10f, 10f, 0f);
//        leftAxis.setDrawZeroLine(true);

        // limit lines are drawn behind data (and not on top)
//        leftAxis.setDrawLimitLinesBehindData(false);

        mChart.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        // add data
//        setChartData(30, 100);
//
//        mChart.setVisibleXRange(20);
//        mChart.setVisibleYRange(20f, YAxis.AxisDependency.LEFT);
//        mChart.centerViewTo(20, 50, YAxis.AxisDependency.LEFT);

        mChart.animateX(500);
        //mChart.invalidate();
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }


    @Override
    protected void onResume() {
        getData();
        super.onResume();
    }

    private void setChartData(List<ReportResult.Electricity> datas) {
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
//            LogUtil.i(format1);
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

    private void getData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        LogUtil.i(report_id);
        RetrofitHelper.getApi().getReportDetail(report_id)
                .compose(this.<ReportResult>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<ReportResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("服务器错误");
                        if (refreshLayout != null)
                            refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(ReportResult result) {
                        if (refreshLayout != null)
                            refreshLayout.setRefreshing(false);
                        if (result != null) {
                            setChartData(result.result.electricity);
                            setOtherData(result.result.info, result.result.cost);
                        }
                    }
                });
    }

    private void setOtherData(ReportResult.Info info, ReportResult.Cost cost) {
        bianhao.setText(info.code);
        startTime.setText(cost.created_at);
        endTime.setText(cost.count_at);
        String endTypes = "--";
        if (cost.end_type.equals("0")) {
            endTypes = "金额用完";
        }
        if (cost.end_type.equals("1")) {
            endTypes = "手动停止";
        }
        if (cost.end_type.equals("2")) {
            endTypes = "充满停止";

        }
        if (cost.end_type.equals("3")) {
            endTypes = "插拔异常";

        }
        if (cost.end_type.equals("4")) {
            endTypes = "通讯异常";

        }
        if (cost.end_type.equals("5")) {
            endTypes = "过流异常";

        }
        if (cost.end_type.equals("6")) {
            endTypes = "未充电时间过长";
        }
        if (cost.end_type.equals("7")){
            endTypes = "停止充电";

        }
        endType.setText(endTypes);
        meterStartDegrees.setText(cost.start_electricity + "度");
        money.setText(cost.charge_money + "元");
        consumeDegrees.setText(cost.electricity + "度");
        consumeMoney.setText(cost.current_cost + "元");
    }
}
