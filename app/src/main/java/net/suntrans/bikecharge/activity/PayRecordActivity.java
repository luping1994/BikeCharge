package net.suntrans.bikecharge.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.PayRecordResult;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/6/4.
 */

public class PayRecordActivity extends BasedActivity {

    private ScrollChildSwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private TextView tips;


    private List<PayRecordResult.PayRecord> datas;
    private MyAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_record);
        initView();
    }

    private void initView() {
        datas = new ArrayList<>();


        adapter = new MyAdapter();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        refreshLayout = (ScrollChildSwipeRefreshLayout) findViewById(R.id.refreshlayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
        tips = (TextView) findViewById(R.id.tips);
        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new ViewHolder(LayoutInflater.from(PayRecordActivity.this).inflate(R.layout.item_pay_record, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setData(position);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView order_sn;
            TextView total_amount;
            TextView created_at;
            TextView pay_code;

            public ViewHolder(View itemView) {
                super(itemView);
                order_sn = (TextView) itemView.findViewById(R.id.order_sn);
                total_amount = (TextView) itemView.findViewById(R.id.total_amount);
                created_at = (TextView) itemView.findViewById(R.id.created_at);
                pay_code = (TextView) itemView.findViewById(R.id.pay_code);
            }

            public void setData(int position) {
                order_sn.setText("订单号:" + datas.get(position).order_sn);
                total_amount.setText("+" + datas.get(position).total_amount);
                created_at.setText("交易时间:" + datas.get(position).created_at);
                String type = datas.get(position).pay_code.equals("1") ? "微信支付" : "支付宝";
                pay_code.setText("支付方式:" + type);
            }
        }
    }

    private void getData() {
        RetrofitHelper.getApi().getPayRecord()
                .compose(this.<PayRecordResult>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<PayRecordResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("获取数据失败");
                        tips.setVisibility(View.VISIBLE);
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(PayRecordResult data) {
                        refreshLayout.setRefreshing(false);
                        if (data==null){
                            UiUtils.showToast("获取数据失败");
                            tips.setVisibility(View.VISIBLE);
                            return;
                        }
                        if (data.result != null) {
                            datas.clear();
                            datas.addAll(data.result);
                            tips.setVisibility(View.INVISIBLE);

                            if (datas.size() == 0) {
                                tips.setVisibility(View.VISIBLE);
                            }
                            adapter.notifyDataSetChanged();
                        }else {
                            UiUtils.showToast("获取数据失败");
                            tips.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}
