package net.suntrans.bikecharge.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.trello.rxlifecycle.android.ActivityEvent;
import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.MSG;
import net.suntrans.bikecharge.bean.ReportListResult;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.ScrollChildSwipeRefreshLayout;
import java.util.List;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/5/27.
 */

public class ReportListActivity extends BasedActivity {
    private List<ReportListResult.Cost> datas;
    private RecyclerView recyclerView;
    private ScrollChildSwipeRefreshLayout refreshLayout;
    private MyAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportlist);
        init();
    }

    private void init() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);
//        refreshLayout = (ScrollChildSwipeRefreshLayout) findViewById(R.id.refreshlayout);
//        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getData();
//            }
//        });
        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onResume() {
        getData();
        super.onResume();
    }

    class MyAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ReportListActivity.this).inflate(R.layout.item_report, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ViewHolder) holder).setData(position);
        }

        @Override
        public int getItemCount() {
            return datas == null ? 0 : datas.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView time;
            private TextView cost;
            private LinearLayout root;

            public ViewHolder(View itemView) {
                super(itemView);
                time = (TextView) itemView.findViewById(R.id.time);
                cost = (TextView) itemView.findViewById(R.id.cost);
                root = (LinearLayout) itemView.findViewById(R.id.root);
                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ReportListActivity.this,ReportDetailActivity.class);
                        intent.putExtra("report_id",datas.get(getAdapterPosition()).id);
                        startActivity(intent);
                    }
                });
                root.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new AlertDialog.Builder(ReportListActivity.this)
                                .setMessage("是否删除该条纪录?")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int position =getAdapterPosition();
                                        if (position==-1){
                                            UiUtils.showToast("删除失败,请稍后再试");
                                            return;
                                        }else {
                                            delete(datas.get(position).id);
                                        }
                                    }
                                })
                                .setNegativeButton("取消",null)
                                .create().show();
                        return true;
                    }
                });
            }

            public void setData(int position) {
                time.setText(datas.get(position).created_at);
                cost.setText("充入金额:" + datas.get(position).charge_money);
            }
        }
    }

    private void delete(String id) {
        RetrofitHelper.getApi().deleteChargeMsg(id)
                .compose(this.<MSG>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MSG>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        UiUtils.showToast("服务器错误");
                    }

                    @Override
                    public void onNext(MSG msg) {
                        UiUtils.showToast(msg.getMsg());
                        getData();
                    }
                });
    }


    private void getData() {
        if (refreshLayout != null)
            refreshLayout.setRefreshing(true);
        RetrofitHelper.getApi().getHistory()
                .compose(this.<ReportListResult>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ReportListResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("获取充电报告列表失败");
                        if (refreshLayout != null)
                            refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(ReportListResult result) {
                        if (refreshLayout != null)
                            refreshLayout.setRefreshing(false);
                        if (result.result != null) {
                            if (result.result.cost!=null){
                                List<ReportListResult.Cost> cost = result.result.cost;
                                datas = cost;
                                adapter.notifyDataSetChanged();
                            }else {
                                UiUtils.showToast("暂无充电报告");
                            }


                            if (datas.size()==0){

                            }
                        } else {
                            UiUtils.showToast("暂无充电报告");
                        }
                    }
                });
    }

}
