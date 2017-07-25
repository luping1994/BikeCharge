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
import net.suntrans.bikecharge.bean.PushHisResult;
import net.suntrans.bikecharge.utils.UiUtils;
import net.suntrans.bikecharge.view.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/5/27.
 */

public class PushHistoryActivity extends BasedActivity {
    private List<PushHisResult.History> datas=new ArrayList<>();
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
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("消息中心");
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
            View view = LayoutInflater.from(PushHistoryActivity.this).inflate(R.layout.item_push, parent, false);
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
            private TextView msg;
            private LinearLayout root;

            public ViewHolder(View itemView) {
                super(itemView);
                time = (TextView) itemView.findViewById(R.id.time);
                msg = (TextView) itemView.findViewById(R.id.msg);
                root = (LinearLayout) itemView.findViewById(R.id.root);
                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                root.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new AlertDialog.Builder(PushHistoryActivity.this)
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
                msg.setText( datas.get(position).message);
            }
        }
    }

    private void delete(String id) {
        RetrofitHelper.getApi().deleteHistory(id)
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
        RetrofitHelper.getApi().getPushHis()
                .compose(this.<PushHisResult>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PushHisResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        UiUtils.showToast("获取历史消息失败");
                        if (refreshLayout != null)
                            refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(PushHisResult result) {
                        if (refreshLayout != null)
                            refreshLayout.setRefreshing(false);
                        if (result.result != null) {
                            if (result.result.history!=null){
                                List<PushHisResult.History> cost = result.result.history;
                                datas.clear();
                                datas.addAll(cost);
                                adapter.notifyDataSetChanged();
                            }else {
                                UiUtils.showToast("暂无历史消息");
                            }


                            if (datas.size()==0){
                                UiUtils.showToast("暂无历史消息");
                            }
                        } else {
                            UiUtils.showToast("暂无历史消息");
                        }
                    }
                });
    }

}
