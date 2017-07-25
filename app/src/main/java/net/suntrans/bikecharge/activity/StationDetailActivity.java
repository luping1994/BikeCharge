package net.suntrans.bikecharge.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.TextView;

import com.amap.api.maps.model.Text;
import com.trello.rxlifecycle.android.ActivityEvent;

import net.suntrans.bikecharge.R;
import net.suntrans.bikecharge.api.RetrofitHelper;
import net.suntrans.bikecharge.bean.SceneResult;
import net.suntrans.bikecharge.view.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Looney on 2017/6/8.
 */

public class StationDetailActivity extends BasedActivity {
   private String id;
    private TextView allSwitch;
    private TextView wendu;
    private TextView shidu;
    private TextView yanwu;
    private TextView pm25;
    private ScrollChildSwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);
         id = getIntent().getStringExtra("id");
        initView();
    }

    private void initView() {
        allSwitch = (TextView) findViewById(R.id.allSwitch);
        wendu = (TextView) findViewById(R.id.wendu);
        shidu = (TextView) findViewById(R.id.shidu);
        yanwu = (TextView) findViewById(R.id.yanwu);
        pm25 = (TextView) findViewById(R.id.pm25);
        refreshLayout = (ScrollChildSwipeRefreshLayout) findViewById(R.id.refreshlayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData(id);
            }
        });
        findViewById(R.id.backRl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    SceneResult.Sensus datas = new SceneResult.Sensus();


    @Override
    protected void onResume() {
        getData(id);
        super.onResume();
    }

    private void getData(String id){
        RetrofitHelper.getApi().getSceneInfo(id)
                .compose(this.<SceneResult>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SceneResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onNext(SceneResult result) {
                        refreshLayout.setRefreshing(false);

                        if (result!=null){
                            if (result.result!=null){
                                datas = result.result.sensus;
                                refreshView(datas);
                            }
                        }
                    }
                });
    }

    private void refreshView(SceneResult.Sensus datas) {
        wendu.setText(datas.wendu+"℃");
        shidu.setText(datas.shidu+"%");
        yanwu.setText(datas.yanwu+"ppm");
        pm25.setText(datas.pm25+"ppm");
    }
}
